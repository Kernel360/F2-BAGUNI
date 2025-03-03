package baguni.api.application.pick.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import baguni.BaguniApiApplication;
import baguni.api.application.pick.dto.PickApiMapper;
import baguni.api.application.pick.dto.PickApiMapperImpl;
import baguni.api.application.pick.dto.PickApiRequest;
import baguni.api.service.pick.service.PickSearchService;
import baguni.api.service.pick.service.PickService;
import baguni.api.service.user.service.UserService;
import baguni.common.event.BookmarkCreateEvent;
import baguni.common.event.EventMessenger;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.infrastructure.pick.dto.PickResult;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author sangwon
 * @WebMvcTest 사용할 수 있었지만, 빈 주입해야 하는 부분이 너무 많아 @SpringBootTest 사용
 * 실제 DB에 데이터가 들어가는 것은 시큐리티 때문에 까다로워서 컨트롤러 부분만 테스트하고자 함.
 *
 * 슬라이스 테스트 : 특정 레이어만 잘라서 테스트하는 기법
 * 컨트롤러만 테스트하는 것
 */
@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc // MockMvc 사용을 위한 설정
@Import(PickApiMapperImpl.class) // @SpyBean 구현체 인식을 못하여 추가
@DisplayName("픽 컨트롤러 - 슬라이스 테스트")
class PickApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PickService pickService;

	@SpyBean
	private PickApiMapper pickApiMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private PickSearchService pickSearchService;

	@MockBean
	private EventMessenger eventMessenger;

	// 반드시 있어야 함.
	@MockBean
	private SecurityFilterChain securityFilterChain; // 실제 시큐리티 구현체 사용하지 않기 위함.

	Long userId, pickId, otherPickId;
	List<Long> folderIdList;
	String folderIdListParam;
	LinkInfo linkInfo, otherLinkInfo;
	IDToken idToken;
	UserInfo userInfo;

	@BeforeEach
	void setUp() {
		userId = 1L;
		pickId = 1L;
		otherPickId = 2L;
		folderIdList = List.of(1L, 2L, 3L);
		folderIdListParam = listToString(folderIdList);
		linkInfo = new LinkInfo("https://example.com", "linkTitle", "description", "imageUrl");
		otherLinkInfo = new LinkInfo("https://other.example.com", "linkTitle", "description", "imageUrl");

		idToken = IDToken.fromString(UUID.randomUUID().toString());
		var authentication = new UsernamePasswordAuthenticationToken(idToken, null,
			List.of(new SimpleGrantedAuthority("ROLE_USER")));

		// @LoginUserId에서 사용하므로 임의의 데이터 세팅
		SecurityContextHolder.getContext().setAuthentication(authentication);

		userInfo = new UserInfo(userId, "user", idToken, "email@example.com");

		// 반드시 있어야 합니다. userInfo 객체를 인식합니다.
		given(userService.getUserInfoByToken(idToken)).willReturn(userInfo);
	}

	@AfterEach
	void cleanUp() {
		// 설정했던 시큐리티 데이터 제거
		SecurityContextHolder.clearContext();
	}

	@Nested
	@DisplayName("픽 조회")
	class FindPickList {

		@Test
		@DisplayName("폴더 리스트 내 픽 리스트 조회")
		void get_folder_child_pick_list() throws Exception {
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
					)),
				new PickResult.FolderPickWithViewCountList(folderIdList.get(1), new ArrayList<>()),
				new PickResult.FolderPickWithViewCountList(folderIdList.get(2), new ArrayList<>())
			);
			given(pickService.getFolderListChildPickList(readList)).willReturn(folderPickList);

			// when
			mockMvc.perform(get("/api/picks")
					   .param("folderIdList", folderIdListParam)
					   .header("Authorization", "test-token")
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());

			// then
			then(pickApiMapper).should(times(1)).toReadListCommand(userId, folderIdList);
			then(pickApiMapper).should(times(3)).toApiFolderPickListWithViewCount(any());

			pickApiMapper.toReadListCommand(null, null);
			pickApiMapper.toReadListCommand(null, folderIdList);
			pickApiMapper.toReadListCommand(userId, null);
		}

		@Test
		@DisplayName("폴더 내 픽 리스트 조회 - folderPickList null")
		void get_folder_child_pick_list_null() throws Exception {
			// given
			var readList = new PickCommand.ReadList(userId, null);

			given(pickService.getFolderListChildPickList(readList)).willReturn(null);

			// when
			mockMvc.perform(get("/api/picks")
					   .param("folderIdList", "") // 빈 배열 전달
					   .header("Authorization", "test-token")
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());

			// then
			var result = pickApiMapper.toApiFolderPickListWithViewCount(null);
			assertThat(result).isNull();
			then(pickApiMapper).should(times(1)).toApiFolderPickListWithViewCount(any());
		}

		@Test
		@DisplayName("링크 픽 여부 조회")
		void exist_pick() throws Exception {
			// given
			given(pickService.existPickByUrl(userId, linkInfo.url())).willReturn(true);

			// when
			mockMvc.perform(get("/api/picks/link")
					   .param("link", linkInfo.url()) // 빈 배열 전달
					   .header("Authorization", "test-token")
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());

			// then
			then(pickService).should(times(1)).existPickByUrl(userId, linkInfo.url());
			then(pickApiMapper).should(times(1)).toApiExistResponse(true);

			pickApiMapper.toApiExistResponse(null);
			pickApiMapper.toApiExistResponse(false);
		}
	}

	@Nested
	@DisplayName("픽 검색")
	class PickSearch {

		@Test
		@DisplayName("픽 리스트 검색")
		void search_pick_list() throws Exception {
			// given
			List<String> searchTokenList = List.of("pick1", "pick2");
			List<Long> tagIdList = List.of(1L, 2L);
			Long cursor = 0L;
			int size = 20;

			var command = new PickCommand.SearchPagination(userId, folderIdList, searchTokenList, tagIdList, cursor,
				size);
			var pickResultList = new SliceImpl<>(
				List.of(
					new PickResult.Pick(pickId, "pick1", linkInfo, folderIdList.get(0), tagIdList, LocalDateTime.now(),
						LocalDateTime.now()),
					new PickResult.Pick(otherPickId, "pick2", otherLinkInfo, folderIdList.get(1), tagIdList,
						LocalDateTime.now(),
						LocalDateTime.now()),
					new PickResult.Pick(3L, "pick3", otherLinkInfo, folderIdList.get(2), tagIdList,
						LocalDateTime.now(),
						LocalDateTime.now())
				)
			);

			given(pickSearchService.searchPickPagination(command)).willReturn(pickResultList);

			// when
			mockMvc.perform(get("/api/picks/search")
					   .param("folderIdList", folderIdListParam)
					   .param("searchTokenList", listToString(searchTokenList))
					   .param("tagIdList", listToString(tagIdList))
					   .param("cursor", String.valueOf(cursor))
					   .param("size", String.valueOf(size))
					   .header("Authorization", "test-token")
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());

			// then
			then(pickApiMapper)
				.should(times(1))
				.toSearchPaginationCommand(userId, folderIdList, searchTokenList, tagIdList, cursor, size);
			then(pickSearchService).should(times(1)).searchPickPagination(command);
			then(pickApiMapper).should(times(1)).toSliceApiResponse(pickResultList);

			// toSearchPaginationCommand 분기 테스트
			pickApiMapper.toSearchPaginationCommand(null, null, null, null, null, null);
			pickApiMapper.toSearchPaginationCommand(userId, null, searchTokenList, tagIdList, cursor, size);
			pickApiMapper.toSearchPaginationCommand(userId, folderIdList, null, tagIdList, cursor, size);
			pickApiMapper.toSearchPaginationCommand(userId, folderIdList, searchTokenList, null, cursor, size);
			pickApiMapper.toSearchPaginationCommand(userId, folderIdList, searchTokenList, tagIdList, cursor, null);
			pickApiMapper.toSearchPaginationCommand(null, folderIdList, searchTokenList, tagIdList, cursor, size);
			pickApiMapper.toSearchPaginationCommand(null, null, searchTokenList, tagIdList, cursor, size);
			pickApiMapper.toSearchPaginationCommand(null, null, null, tagIdList, cursor, size);
			pickApiMapper.toSearchPaginationCommand(null, null, null, null, cursor, size);
			pickApiMapper.toSearchPaginationCommand(null, null, null, null, null, size);
		}
	}

	@Nested
	@DisplayName("픽 생성, 수정, 이동, 삭제")
	class Create {

		@Test
		@DisplayName("픽 생성 테스트")
		void create_pick() throws Exception {
			// given
			var request = new PickApiRequest.Create("pick1", new ArrayList<>(), folderIdList.get(0), linkInfo);
			var command = new PickCommand.Create(userId, "pick1", new ArrayList<>(), folderIdList.get(0), linkInfo);
			var result = new PickResult.Pick(pickId, "pick1", linkInfo, folderIdList.get(0), new ArrayList<>(),
				LocalDateTime.now(), LocalDateTime.now());

			given(pickService.saveNewPick(command)).willReturn(result);

			// when
			mockMvc.perform(post("/api/picks")
					   .content(objectMapper.writeValueAsString(request))
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());

			// then
			then(pickApiMapper).should(times(1)).toCreateCommand(userId, request);
			then(pickService).should(times(1)).saveNewPick(command);
			then(eventMessenger).should(times(1)).send(any(BookmarkCreateEvent.class));
			then(pickApiMapper).should(times(1)).toApiResponse(result);

			// toCreateCommand 분기 테스트
			pickApiMapper.toCreateCommand(null, null);
			pickApiMapper.toCreateCommand(userId, null);
			pickApiMapper.toCreateCommand(null, request);
			pickApiMapper.toCreateCommand(userId,
				new PickApiRequest.Create("pick1", null, folderIdList.get(0), linkInfo));
		}

		@Test
		@DisplayName("픽 수정")
		void update_pick() throws Exception {
			// given
			var request = new PickApiRequest.Update(pickId, "pick", new ArrayList<>());
			var command = new PickCommand.Update(userId, pickId, "pick", null, new ArrayList<>());
			var result = new PickResult.Pick(pickId, "pick", linkInfo, null, new ArrayList<>(), LocalDateTime.now(),
				LocalDateTime.now());

			given(pickService.updatePick(command)).willReturn(result);

			// when
			mockMvc.perform(patch("/api/picks")
					   .content(objectMapper.writeValueAsString(request))
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());

			// then
			then(pickApiMapper).should(times(1)).toUpdateCommand(userId, request);
			then(pickService).should(times(1)).updatePick(command);
			then(pickApiMapper).should(times(1)).toApiResponse(result);

			pickApiMapper.toApiResponse(null);
			pickApiMapper.toApiResponse(
				new PickResult.Pick(pickId, "pick", linkInfo, null, null, LocalDateTime.now(), LocalDateTime.now()));
			pickApiMapper.toUpdateCommand(null, (PickApiRequest.Update)null);
			pickApiMapper.toUpdateCommand(null, request);
			pickApiMapper.toUpdateCommand(userId, (PickApiRequest.Update)null);
			pickApiMapper.toUpdateCommand(userId, new PickApiRequest.Update(pickId, "pick", null));
		}

		@Test
		@DisplayName("픽 이동")
		void move_pick() throws Exception {
			// given
			List<Long> idList = List.of(pickId);
			var request = new PickApiRequest.Move(idList, folderIdList.get(0), 0);
			var command = new PickCommand.Move(userId, idList, folderIdList.get(0), 0);

			// when
			mockMvc.perform(patch("/api/picks/location")
					   .content(objectMapper.writeValueAsString(request))
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isNoContent());

			// then
			then(pickApiMapper).should(times(1)).toMoveCommand(userId, request);
			then(pickService).should(times(1)).movePick(command);

			pickApiMapper.toMoveCommand(null, null);
			pickApiMapper.toMoveCommand(null, request);
			pickApiMapper.toMoveCommand(userId, null);
			pickApiMapper.toMoveCommand(userId, new PickApiRequest.Move(null, folderIdList.get(0), 0));
		}

		@Test
		@DisplayName("픽 삭제")
		void delete_pick() throws Exception {
			// given
			List<Long> idList = List.of(pickId);
			var request = new PickApiRequest.Delete(idList);
			var command = new PickCommand.Delete(userId, idList);

			// when
			mockMvc.perform(delete("/api/picks")
					   .content(objectMapper.writeValueAsString(request))
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isNoContent());

			// then
			then(pickApiMapper).should(times(1)).toDeleteCommand(userId, request);
			then(pickService).should(times(1)).deletePick(command);

			pickApiMapper.toDeleteCommand(null, null);
			pickApiMapper.toDeleteCommand(null, request);
			pickApiMapper.toDeleteCommand(userId, null);
			pickApiMapper.toDeleteCommand(userId, new PickApiRequest.Delete(null));
		}
	}

	@Nested
	@DisplayName("익스텐션 관련")
	class Extension {

		@Test
		@DisplayName("익스텐션 픽 생성")
		void create_extension_pick() throws Exception {
			// given
			var request = new PickApiRequest.CreateFromExtension(linkInfo.url(), linkInfo.title());
			var command = new PickCommand.Extension(userId, linkInfo.title(), linkInfo.url());
			var result = new PickResult.Extension(pickId, "pick1", 1L, linkInfo.url(), folderIdList.get(0),
				new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());

			given(pickService.savePickToUnclassified(command)).willReturn(result);

			// when
			mockMvc.perform(post("/api/picks/extension")
					   .content(objectMapper.writeValueAsString(request))
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());

			// then
			then(pickApiMapper).should(times(1)).toExtensionCommand(userId, linkInfo.title(), linkInfo.url());
			then(pickService).should(times(1)).savePickToUnclassified(command);
			then(eventMessenger).should(times(1)).send(any(BookmarkCreateEvent.class));
			then(pickApiMapper).should(times(1)).toApiExtensionResponse(result);

			pickApiMapper.toExtensionCommand(null, null, null);
			pickApiMapper.toExtensionCommand(null, null, linkInfo.url());
			pickApiMapper.toExtensionCommand(null, linkInfo.title(), null);
			pickApiMapper.toExtensionCommand(null, linkInfo.title(), linkInfo.url());
			pickApiMapper.toApiExtensionResponse(null);
			pickApiMapper.toApiExtensionResponse(
				new PickResult.Extension(pickId, "pick1", 1L, linkInfo.url(), folderIdList.get(0),
					null, LocalDateTime.now(), LocalDateTime.now()));
		}

		@Test
		@DisplayName("익스텐션 픽 수정")
		void update_extension_pick() throws Exception {
			// given
			var request = new PickApiRequest.UpdateFromExtension(pickId, "pick", folderIdList.get(0), List.of(1L, 2L));
			var command = new PickCommand.Update(userId, pickId, "pick", folderIdList.get(0), List.of(1L, 2L));
			var result = new PickResult.Pick(pickId, "pick", linkInfo, folderIdList.get(0), List.of(1L, 2L),
				LocalDateTime.now(), LocalDateTime.now());

			given(pickService.updatePick(command)).willReturn(result);

			// when
			mockMvc.perform(patch("/api/picks/extension")
					   .content(objectMapper.writeValueAsString(request))
					   .contentType(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());

			// then
			then(pickApiMapper).should(times(1)).toUpdateCommand(userId, request);
			then(pickService).should(times(1)).updatePick(command);
			then(pickApiMapper).should(times(1)).toApiResponse(result);

			pickApiMapper.toUpdateCommand(null, (PickApiRequest.UpdateFromExtension)null);
			pickApiMapper.toUpdateCommand(userId, (PickApiRequest.UpdateFromExtension)null);
			pickApiMapper.toUpdateCommand(null, request);
			pickApiMapper.toUpdateCommand(userId, new PickApiRequest.UpdateFromExtension(pickId, "pick", null, null));
		}
	}

	private String listToString(List<?> list) {
		return String.join(",", list.stream().map(String::valueOf).toList());
	}

}