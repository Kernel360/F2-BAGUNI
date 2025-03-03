package baguni.infra.infrastructure.folder;

import static baguni.infra.model.folder.QFolder.*;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import baguni.infra.model.folder.Folder;
import baguni.infra.model.folder.FolderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FolderQuery {

	private final JPAQueryFactory jpaQueryFactory;

	public Folder findRoot(Long userId) {
		return findByFolderType(userId, FolderType.ROOT);
	}

	public Folder findUnclassified(Long userId) {
		return findByFolderType(userId, FolderType.UNCLASSIFIED);
	}

	public Folder findRecycleBin(Long userId) {
		return findByFolderType(userId, FolderType.RECYCLE_BIN);
	}

	private Folder findByFolderType(Long userId, FolderType folderType) {
		return jpaQueryFactory
			.selectFrom(folder)
			.where(
				userEqCondition(userId),
				folderTypeEqCondition(folderType)
			)
			.fetchOne();
	}

	private BooleanExpression userEqCondition(Long userId) {
		return folder.user.id.eq(userId);
	}

	private BooleanExpression folderTypeEqCondition(FolderType folderType) {
		return folder.folderType.eq(folderType);
	}
}