package baguni.api.application.pick.controller;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.SliceImpl;

import baguni.api.application.pick.dto.PickApiMapper;
import baguni.api.application.pick.dto.PickApiRequest;
import baguni.api.application.pick.dto.PickApiResponse;
import baguni.api.service.pick.service.PickSearchService;
import baguni.api.service.pick.service.PickService;
import baguni.common.event.BookmarkCreateEvent;
import baguni.common.event.EventMessenger;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.infrastructure.pick.dto.PickResult;

/**
 * 컨트롤러 단위 테스트는 해당 클래스 참고 부탁드립니다.
 * given(), then() 사용하려면 BDDMockito.given() 사용해서 static import
 */
@DisplayName("픽 컨트롤러 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PickApiControllerUnitTest {

	@Mock
	private PickService pickService;

	@Mock
	private PickApiMapper pickApiMapper;

	@Mock
	private PickSearchService pickSearchService;

	@Mock
	private EventMessenger eventMessenger;

	@InjectMocks
	private PickApiController pickApiController;

	Long userId, pickId, otherPickId;
	List<Long> folderIdList;
	LinkInfo linkInfo, otherLinkInfo;

	@BeforeEach
	void setUp() {
		userId = 1L;
		pickId = 1L;
		otherPickId = 2L;
		folderIdList = List.of(1L, 2L);
		linkInfo = new LinkInfo("https://example.com", "linkTitle", "description", "imageUrl");
		otherLinkInfo = new LinkInfo("https://other.example.com", "linkTitle", "description", "imageUrl");
	}

	@Test
	@DisplayName("폴더 리스트 내 픽 리스트 조회 테스트")
	void get_folder_child_pick_list() {
		// given
		var readList = new PickCommand.ReadList(userId, folderIdList);
		var folderPickList = List.of(
			new PickResult.FolderPickWithViewCountList(folderIdList.get(0),
				List.of(
					new PickResult.PickWithViewCount(pickId, "픽1",
						linkInfo,
						folderIdList.get(0),
						new ArrayList<>(),
						LocalDateTime.now(),
						LocalDateTime.now(),
						false,
						1L
					),
					new PickResult.PickWithViewCount(otherPickId, "픽2",
						otherLinkInfo,
						folderIdList.get(0),
						new ArrayList<>(),
						LocalDateTime.now(),
						LocalDateTime.now(),
						false,
						1L
					)
				))
		);

		given(pickApiMapper.toReadListCommand(userId, folderIdList)).willReturn(readList);
		given(pickService.getFolderListChildPickList(readList)).willReturn(folderPickList);

		// when
		pickApiController.getFolderChildPickList(userId, folderIdList);

		// then
		then(pickApiMapper).should(times(1)).toReadListCommand(userId, folderIdList);
		then(pickService).should(times(1)).getFolderListChildPickList(readList);
	}

	@Test
	@DisplayName("픽 리스트 검색")
	void pick_search_test() {
		// given
		List<String> searchTokenList = List.of("리액트", "서버");
		List<Long> tagIdList = List.of(1L, 2L);
		Long cursor = 0L;
		int size = 20;

		var command = new PickCommand.SearchPagination(userId, folderIdList, searchTokenList, tagIdList, cursor, size);
		var pickResultList = new SliceImpl<>(
			List.of(
				new PickResult.Pick(pickId, "pick1", linkInfo, folderIdList.get(0), tagIdList, LocalDateTime.now(),
					LocalDateTime.now()),
				new PickResult.Pick(otherPickId, "pick2", otherLinkInfo, folderIdList.get(0), tagIdList,
					LocalDateTime.now(),
					LocalDateTime.now())
			)
		);
		var slicePickApiResponse = new SliceImpl<>(
			List.of(
				new PickApiResponse.Pick(pickId, "pick1", linkInfo, folderIdList.get(0), tagIdList,
					LocalDateTime.now(),
					LocalDateTime.now()),
				new PickApiResponse.Pick(otherPickId, "pick2", otherLinkInfo, folderIdList.get(0), tagIdList,
					LocalDateTime.now(),
					LocalDateTime.now())
			)
		);

		given(pickApiMapper.toSearchPaginationCommand(userId, folderIdList, searchTokenList, tagIdList, cursor, size))
			.willReturn(command);
		given(pickSearchService.searchPickPagination(command)).willReturn(pickResultList);
		given(pickApiMapper.toSliceApiResponse(pickResultList)).willReturn(slicePickApiResponse);

		// when
		pickApiController.searchPickPagination(userId, folderIdList, searchTokenList, tagIdList, cursor, size);

		// then
		then(pickApiMapper)
			.should(times(1))
			.toSearchPaginationCommand(userId, folderIdList, searchTokenList, tagIdList, cursor, size);
		then(pickSearchService).should(times(1)).searchPickPagination(command);
		then(pickApiMapper).should(times(1)).toSliceApiResponse(pickResultList);
	}

	@Test
	@DisplayName("링크 픽 여부 조회")
	void exist_pick_link_test() {
		// given
		String url = "https://example.com";
		PickApiResponse.Exist exist = new PickApiResponse.Exist(true);

		given(pickService.existPickByUrl(userId, url)).willReturn(true);
		given(pickApiMapper.toApiExistResponse(true)).willReturn(exist);

		// when
		pickApiController.existPick(userId, url);

		// then
		then(pickService).should(times(1)).existPickByUrl(userId, url);
		then(pickApiMapper).should(times(1)).toApiExistResponse(true);
	}

	@Test
	@DisplayName("픽 생성")
	void create_pick_test() {
		// given
		var request = new PickApiRequest.Create("pick1", new ArrayList<>(), folderIdList.get(0), linkInfo);
		var command = new PickCommand.Create(userId, "pick1", new ArrayList<>(), folderIdList.get(0), linkInfo);
		var result = new PickResult.Pick(pickId, "pick1", linkInfo, folderIdList.get(0), new ArrayList<>(),
			LocalDateTime.now(), LocalDateTime.now());
		var response = new PickApiResponse.Pick(pickId, "pick1", linkInfo, folderIdList.get(0), new ArrayList<>(),
			LocalDateTime.now(), LocalDateTime.now());

		given(pickApiMapper.toCreateCommand(userId, request)).willReturn(command);
		given(pickService.saveNewPick(command)).willReturn(result);
		given(pickApiMapper.toApiResponse(result)).willReturn(response);

		// when
		pickApiController.savePick(userId, request);

		// then
		then(pickApiMapper).should(times(1)).toCreateCommand(userId, request);
		then(pickService).should(times(1)).saveNewPick(command);
		then(eventMessenger).should(times(1)).send(any(BookmarkCreateEvent.class));
		then(pickApiMapper).should(times(1)).toApiResponse(result);
	}

	@Test
	@DisplayName("익스텐션 픽 생성")
	void extension_save_test() {
		// given
		var request = new PickApiRequest.CreateFromExtension(linkInfo.url(), linkInfo.title());
		var command = new PickCommand.Extension(userId, linkInfo.title(), linkInfo.url());
		var result = new PickResult.Extension(pickId, "pick1", 1L, linkInfo.url(), folderIdList.get(0),
			new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());
		var response = new PickApiResponse.Extension(pickId, "pick1", folderIdList.get(0), new ArrayList<>(),
			LocalDateTime.now(), LocalDateTime.now());

		given(pickApiMapper.toExtensionCommand(userId, linkInfo.title(), linkInfo.url())).willReturn(command);
		given(pickService.savePickToUnclassified(command)).willReturn(result);
		given(pickApiMapper.toApiExtensionResponse(result)).willReturn(response);

		// when
		pickApiController.savePickAsUnclassified(userId, request);

		// then
		then(pickApiMapper).should(times(1)).toExtensionCommand(userId, linkInfo.title(), linkInfo.url());
		then(pickService).should(times(1)).savePickToUnclassified(command);
		then(eventMessenger).should(times(1)).send(any(BookmarkCreateEvent.class));
		then(pickApiMapper).should(times(1)).toApiExtensionResponse(result);
	}

	@Test
	@DisplayName("익스텐션 픽 수정")
	void extension_update_test() {
		// given
		var request = new PickApiRequest.UpdateFromExtension(pickId, "pick", folderIdList.get(0), List.of(1L, 2L));
		var command = new PickCommand.Update(userId, pickId, "pick", folderIdList.get(0), List.of(1L, 2L));
		var result = new PickResult.Pick(pickId, "pick", linkInfo, folderIdList.get(0), List.of(1L, 2L),
			LocalDateTime.now(), LocalDateTime.now());
		var response = new PickApiResponse.Pick(pickId, "pick", linkInfo, folderIdList.get(0), List.of(1L, 2L),
			LocalDateTime.now(), LocalDateTime.now());

		given(pickApiMapper.toUpdateCommand(userId, request)).willReturn(command);
		given(pickService.updatePick(command)).willReturn(result);
		given(pickApiMapper.toApiResponse(result)).willReturn(response);

		// when
		pickApiController.updatePickFromChromeExtension(userId, request);

		// then
		then(pickApiMapper).should(times(1)).toUpdateCommand(userId, request);
		then(pickService).should(times(1)).updatePick(command);
		then(pickApiMapper).should(times(1)).toApiResponse(result);
	}

	@Test
	@DisplayName("픽 수정")
	void pick_update_test() {
		// given
		var request = new PickApiRequest.Update(pickId, "pick", new ArrayList<>());
		var command = new PickCommand.Update(userId, pickId, "pick", folderIdList.get(0), List.of(1L, 2L));
		var result = new PickResult.Pick(pickId, "pick", linkInfo, folderIdList.get(0), List.of(1L, 2L),
			LocalDateTime.now(), LocalDateTime.now());
		var response = new PickApiResponse.Pick(pickId, "pick", linkInfo, folderIdList.get(0), List.of(1L, 2L),
			LocalDateTime.now(), LocalDateTime.now());

		given(pickApiMapper.toUpdateCommand(userId, request)).willReturn(command);
		given(pickService.updatePick(command)).willReturn(result);
		given(pickApiMapper.toApiResponse(result)).willReturn(response);

		// when
		pickApiController.updatePick(userId, request);

		// then
		then(pickApiMapper).should(times(1)).toUpdateCommand(userId, request);
		then(pickService).should(times(1)).updatePick(command);
		then(pickApiMapper).should(times(1)).toApiResponse(result);
	}

	@Test
	@DisplayName("픽 이동")
	void move_pick_test() {
		// given
		List<Long> idList = List.of(pickId);
		var request = new PickApiRequest.Move(idList, folderIdList.get(0), 0);
		var command = new PickCommand.Move(userId, idList, folderIdList.get(0), 0);

		given(pickApiMapper.toMoveCommand(userId, request)).willReturn(command);

		// when
		pickApiController.movePick(userId, request);

		// then
		then(pickApiMapper).should(times(1)).toMoveCommand(userId, request);
		then(pickService).should(times(1)).movePick(command);
	}

	@Test
	@DisplayName("픽 삭제")
	void delete_pick_test() {
		// given
		List<Long> idList = List.of(pickId);
		var request = new PickApiRequest.Delete(idList);
		var command = new PickCommand.Delete(userId, idList);

		given(pickApiMapper.toDeleteCommand(userId, request)).willReturn(command);

		// when
		pickApiController.deletePick(userId, request);

		// then
		then(pickApiMapper).should(times(1)).toDeleteCommand(userId, request);
		then(pickService).should(times(1)).deletePick(command);
	}
}