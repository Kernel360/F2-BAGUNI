package baguni.infra.infrastructure.pick;

import static baguni.infra.model.folder.QFolder.*;
import static baguni.infra.model.pick.QPick.*;
import static baguni.infra.model.pick.QPickTag.*;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.pick.dto.PickResult;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PickQuery {

	private final JPAQueryFactory jpaQueryFactory;

	// 폴더 리스트 내 픽 조회 시 java sort vs querydsl 시간 측정 용도
	// Java sort에 비해 속도가 느린 것을 확인. 참고를 위해 코드 유지
	public List<PickResult.Pick> getPickList(Long userId, List<Long> folderIdList) {
		if (folderIdList == null || folderIdList.isEmpty()) {
			return List.of();
		}

		List<Long> pickIdList = folderIdList.stream()
											.flatMap(folderId -> getChildPickIdOrderedList(folderId).stream())
											.toList();

		if (pickIdList.isEmpty()) {
			return List.of();
		}

		return jpaQueryFactory
			.select(pickResultFields())
			.from(pick)
			.where(
				userEqCondition(userId)
			)
			.orderBy(pickOrderSpecifier(pickIdList))
			.fetch();
	}

	/**
	 * 픽 id 순으로 검색 결과가 출력
	 */
	public Slice<PickResult.Pick> searchPickPagination(
		Long userId, List<Long> folderIdList, List<String> searchTokenList,
		List<Long> tagIdList, Long cursor, int size
	) {

		List<PickResult.Pick> pickList = jpaQueryFactory
			.select(pickResultFields()) // dto로 반환
			.from(pick)
			.leftJoin(pickTag).on(pick.id.eq(pickTag.pick.id))
			.where(
				userEqCondition(userId), // 본인 pick 조회
				folderIdListCondition(folderIdList), // 폴더에 해당 하는 pick 조회
				searchTokenListCondition(searchTokenList), // 제목 검색 조건
				tagIdListCondition(tagIdList), // 태그 검색 조건
				cursorIdCondition(cursor) // 페이지네이션 조건
			)
			.distinct()
			.limit(size + 1)
			.fetch();

		/**
		 * 다음 페이지 존재 여부 확인 (true: 있음, false: 없음)
		 * 다음 페이지가 있는지 확인하기 위해 limit에 size + 1
		 * 다음 페이지가 존재한다면, 초과된 데이터 1개 제거
		 */
		boolean hasNext = false;
		if (pickList.size() > size) {
			pickList.remove(size);
			hasNext = true;
		}

		return new SliceImpl<>(pickList, PageRequest.ofSize(size), hasNext);
	}

	/**
	 * 폴더 내에 있는 픽 리스트 순서 보장 때문에 생긴 문제
	 * 폴더 엔티티의 childPickList가 String으로 저장되므로 DB에서 limit으로 잘라서 가져올 수 없음.
	 * 전체 리스트 가져온 후 Java의 subList로 잘라야 함.
	 * 결국 근본적인 원인은 관계 테이블이 없다는 문제, 관계 테이블이 있었다면 쉽게 잘라서 가져올 수 있음.
	 *
	 * 개선 방법 : 관계 테이블을 두고, sequence라는 순서 필드를 생성
	 * 각 요소마다 간격을 100 또는 1000을 둔다.
	 * 맨 앞에 삽입하는 경우 (0+100) / 2 = 50
	 * 중간에 삽입하는 경우 (100+200) / 2 = 150
	 * 이런 식으로 계속 반복하다가 더 이상 넣을 수 없는 경우 sequence 재설정 -> 첫 번째 100, 두 번째 200 ...
	 */
	public Slice<PickResult.Pick> getFolderChildPickPagination(Long userId, Long folderId, Long cursor, int size) {
		List<Long> pickIdList = getChildPickIdOrderedList(folderId);

		if (pickIdList == null || pickIdList.isEmpty()) {
			return new SliceImpl<>(Collections.emptyList(), PageRequest.ofSize(size), false);
		}

		// cursor 기반 subList 뽑기
		int cursorIndex = (cursor == null || cursor == 0) ? 0 : pickIdList.indexOf(cursor) + 1;

		// cursor가 맨 마지막 요소인 경우 빈 리스트 반환
		if (cursorIndex >= pickIdList.size()) {
			return new SliceImpl<>(Collections.emptyList(), PageRequest.ofSize(size), false);
		}

		// 리스트 index 넘어가지 않도록 하기 위함.
		int maxCount = size + 1;
		int endIdx = Math.min(cursorIndex + maxCount, pickIdList.size());

		List<Long> subPickIdList = pickIdList.subList(cursorIndex, endIdx);

		boolean hasNext = false;
		if (subPickIdList.size() > size) {
			hasNext = true;
			subPickIdList = subPickIdList.subList(0, size);
		}

		List<PickResult.Pick> pickList = jpaQueryFactory
			.select(pickResultFields()) // dto로 반환
			.from(pick)
			.where(
				userEqCondition(userId), // 본인 pick 조회
				pickIdListCondition(subPickIdList) // 픽 idList에 포함되는 id
			)
			.orderBy(pickOrderSpecifier(subPickIdList))
			.fetch();

		return new SliceImpl<>(pickList, PageRequest.ofSize(size), hasNext);
	}

	private OrderSpecifier<Integer> pickOrderSpecifier(List<Long> pickIdList) {
		String orderListStr = pickIdList.stream()
										.map(String::valueOf)
										.collect(Collectors.joining(", "));

		Expression<Integer> orderByField = Expressions.template(Integer.class,
			"FIELD({0}, " + orderListStr + ")", pick.id);

		return new OrderSpecifier<>(Order.ASC, orderByField);
	}

	private ConstructorExpression<PickResult.Pick> pickResultFields() {
		return Projections.constructor(
			PickResult.Pick.class,
			pick.id,
			pick.title,
			Projections.constructor(
				LinkInfo.class,
				pick.link.url,
				pick.link.title,
				pick.link.description,
				pick.link.imageUrl
			),
			pick.parentFolder.id,
			pick.tagIdOrderedList,
			pick.createdAt,
			pick.updatedAt
		);
	}

	private BooleanExpression userEqCondition(Long userId) {
		return pick.user.id.eq(userId);
	}

	private BooleanExpression cursorIdCondition(Long cursorId) {
		return cursorId == null ? null : pick.id.gt(cursorId);
	}

	private List<Long> getChildPickIdOrderedList(Long folderId) {
		return jpaQueryFactory
			.select(folder.childPickIdOrderedList)
			.from(folder)
			.where(folderIdCondition(folderId))
			.fetchOne();
	}

	private BooleanExpression pickIdListCondition(List<Long> pickIdList) {
		return pick.id.in(pickIdList);
	}

	private BooleanExpression folderIdCondition(Long folderId) {
		return folder.id.eq(folderId);
	}

	private BooleanExpression folderIdListCondition(List<Long> folderIdList) {
		if (folderIdList == null || folderIdList.isEmpty()) {
			return null;
		}
		return pick.parentFolder.id.in(folderIdList);
	}

	private BooleanExpression searchTokenListCondition(List<String> searchTokenList) {
		if (searchTokenList == null || searchTokenList.isEmpty()) {
			return null;
		}

		return searchTokenList.stream()
							  .map(token -> {
								  StringTokenizer stringTokenizer = new StringTokenizer(token);
								  BooleanExpression combinedCondition = null;
								  while (stringTokenizer.hasMoreTokens()) {
									  String part = stringTokenizer.nextToken().toLowerCase();
									  BooleanExpression condition = pick.title.lower().like("%" + part + "%");
									  combinedCondition =
										  (combinedCondition == null) ? condition : combinedCondition.and(condition);
								  }
								  return combinedCondition;
							  })
							  .reduce(BooleanExpression::and)
							  .orElse(null);
	}

	private BooleanExpression tagIdListCondition(List<Long> tagIdList) {
		if (tagIdList == null || tagIdList.isEmpty()) {
			return null;
		}

		return jpaQueryFactory
			.selectFrom(pickTag)
			.where(
				pickTag.pick.id.eq(pick.id)
							   .and(pickTag.tag.id.in(tagIdList)))
			.groupBy(pickTag.pick.id)
			.having(pickTag.tag.id.count().eq((long)tagIdList.size()))
			.exists();
	}
}
