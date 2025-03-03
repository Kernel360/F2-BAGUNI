package baguni.api.application.sharedFolder.controller;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import baguni.BaguniApiApplication;
import baguni.api.application.sharedFolder.dto.SharedFolderApiMapper;
import baguni.api.application.sharedFolder.dto.SharedFolderApiMapperImpl;
import baguni.api.service.sharedFolder.service.SharedFolderService;
import baguni.api.service.user.service.UserService;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.infra.infrastructure.sharedFolder.dto.SharedFolderResult;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.folder.FolderType;
import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc // MockMvc 사용을 위한 설정
@Import(SharedFolderApiMapperImpl.class) // @SpyBean 구현체 인식을 못하여 추가
@DisplayName("공유 폴더 컨트롤러 - 슬라이스 테스트")
class SharedFolderApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SecurityFilterChain securityFilterChain; // 실제 시큐리티 구현체 사용하지 않기 위함.

	@SpyBean
	private SharedFolderApiMapper sharedFolderApiMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private SharedFolderService sharedFolderService;

	Long userId, folderId, otherFolderId, rootId;
	UUID uuid;
	String folderAccessToken;
	IDToken idToken;
	UserInfo userInfo;

	@BeforeEach
	void setUp() {
		userId = 1L;
		rootId = 1L;
		folderId = 2L;
		otherFolderId = 3L;
		uuid = UUID.randomUUID();
		folderAccessToken = UUID.randomUUID().toString();

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

	@Test
	@DisplayName("공유 폴더 등록")
	void create_shared_folder() throws Exception {
		// given
		var result = new SharedFolderResult.Create(folderAccessToken);

		given(sharedFolderService.createSharedFolder(userId, folderId)).willReturn(result);

		// when
		mockMvc.perform(post("/api/shared")
				   .content(objectMapper.writeValueAsString(folderId))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(sharedFolderService).should(times(1)).createSharedFolder(userId, folderId);
		then(sharedFolderApiMapper).should(times(1)).toCreateResponse(result);

		sharedFolderApiMapper.toCreateResponse(null);
	}

	@Test
	@DisplayName("공유 폴더 리스트 조회")
	void get_shared_folder_list() throws Exception {
		// given
		String otherAccessToken = UUID.randomUUID().toString();
		var result = List.of(
			new SharedFolderResult.Read(new FolderResult(folderId, "폴더1", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), folderAccessToken),
			new SharedFolderResult.Read(new FolderResult(otherFolderId, "폴더2", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), otherAccessToken)
		);

		given(sharedFolderService.getSharedFolderListByUserId(userId)).willReturn(result);

		// when
		mockMvc.perform(get("/api/shared")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(sharedFolderService).should(times(1)).getSharedFolderListByUserId(userId);
		then(sharedFolderApiMapper).should(times(1)).toReadResponseList(result);

		sharedFolderApiMapper.toReadResponseList(null);
	}


	@Test
	@DisplayName("공유 폴더 리스트 조회 - null 체크")
	void get_shared_folder_list_null_test() throws Exception {
		// given
		var result = List.of(
			new SharedFolderResult.Read(null, null),
			new SharedFolderResult.Read(new FolderResult(1L, "폴더1", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), null), UUID.randomUUID().toString()),
			new SharedFolderResult.Read(new FolderResult(2L, "폴더2", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), null, LocalDateTime.now()), UUID.randomUUID().toString()),
			new SharedFolderResult.Read(new FolderResult(3L, null, FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), UUID.randomUUID().toString()),
			new SharedFolderResult.Read(new FolderResult(null, "폴더3", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), UUID.randomUUID().toString()),
			new SharedFolderResult.Read(new FolderResult(4L, "폴더4", FolderType.GENERAL, null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), UUID.randomUUID().toString()),
			new SharedFolderResult.Read(new FolderResult(5L, "폴더4", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), UUID.randomUUID().toString())
		);

		given(sharedFolderService.getSharedFolderListByUserId(userId)).willReturn(result);

		// when
		mockMvc.perform(get("/api/shared")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(sharedFolderService).should(times(1)).getSharedFolderListByUserId(userId);
		then(sharedFolderApiMapper).should(times(1)).toReadResponseList(result);
	}

	@Test
	@DisplayName("공유 폴더 취소")
	void delete_shared_folder() throws Exception {
		// given

		// when
		mockMvc.perform(delete("/api/shared/{sourceFolderId}", folderId)
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isNoContent());

		// then
		then(sharedFolderService).should(times(1)).deleteSharedFolder(userId, folderId);
	}

	@Test
	@DisplayName("공유 폴더 조회")
	void get_shared_folder_by_id() throws Exception {
		// given
		var result = new SharedFolderResult.SharedFolderInfo("폴더", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());

		given(sharedFolderService.getSharedFolderInfo(uuid)).willReturn(result);

		// when
		mockMvc.perform(get("/api/shared/{uuid}", uuid)
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(sharedFolderService).should(times(1)).getSharedFolderInfo(uuid);
		then(sharedFolderApiMapper).should(times(1)).toReadFolderFullResponse(result);

		sharedFolderApiMapper.toReadFolderFullResponse(null);
		sharedFolderApiMapper.toReadFolderFullResponse(new SharedFolderResult.SharedFolderInfo("폴더", LocalDateTime.now(), LocalDateTime.now(), null, new ArrayList<>()));
		sharedFolderApiMapper.toReadFolderFullResponse(new SharedFolderResult.SharedFolderInfo("폴더", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), null));
	}
}