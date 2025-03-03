package baguni.api.application.folder.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import baguni.api.application.folder.dto.FolderApiMapper;
import baguni.api.application.folder.dto.FolderApiMapperImpl;
import baguni.api.application.folder.dto.FolderApiRequest;
import baguni.api.service.folder.service.FolderService;
import baguni.api.service.sharedFolder.service.SharedFolderService;
import baguni.api.service.user.service.UserService;
import baguni.infra.infrastructure.folder.dto.FolderCommand;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.folder.FolderType;
import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc // MockMvc 사용을 위한 설정
@Import(FolderApiMapperImpl.class) // @SpyBean 구현체 인식을 못하여 추가
@DisplayName("폴더 컨트롤러 - 슬라이스 테스트")
class FolderApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private FolderService folderService;

	@MockBean
	private SharedFolderService sharedFolderService;

	@MockBean
	private UserService userService;

	@SpyBean
	private FolderApiMapper folderApiMapper;

	@MockBean
	private SecurityFilterChain securityFilterChain; // 실제 시큐리티 구현체 사용하지 않기 위함.

	Long userId, folderId, otherFolderId, rootId, unclassifiedId, recycleBinId, sharedFolderId;
	String uuidToken;
	IDToken idToken;
	UserInfo userInfo;

	@BeforeEach
	void setUp() {
		userId = 1L;
		rootId = 1L; unclassifiedId = 2L; recycleBinId = 3L; folderId = 4L; otherFolderId = 5L;
		sharedFolderId = 1L;
		uuidToken = UUID.randomUUID().toString();

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
	@DisplayName("루트 폴더와 하위 리스트 조회")
	void all_root_folder() throws Exception {
		// given
		List<FolderResult> folderResultList = List.of(
			new FolderResult(rootId, "root", FolderType.ROOT, null, List.of(folderId, otherFolderId), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
			new FolderResult(folderId, "폴더1", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
			new FolderResult(otherFolderId, "폴더2", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now())
		);

		for (FolderResult folderResult : folderResultList) {
			String folderAccessToken = UUID.randomUUID().toString();
			Optional<String> optionalAccessToken = Optional.of(folderAccessToken);
			given(sharedFolderService.findFolderAccessTokenByFolderId(folderResult.id())).willReturn(optionalAccessToken);
		}

		given(folderService.getAllRootFolderList(userId)).willReturn(folderResultList);

		// when
		mockMvc.perform(get("/api/folders")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(sharedFolderService).should(times(3)).findFolderAccessTokenByFolderId(any());
		then(folderService).should(times(1)).getAllRootFolderList(userId);
		then(folderApiMapper).should(times(3)).toApiResponse(any(), any());

		folderApiMapper.toApiResponse(null, null);
		folderApiMapper.toApiResponse(new FolderResult(folderId, "폴더1", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), null);
		folderApiMapper.toApiResponse(null, uuidToken);
		folderApiMapper.toApiResponse(new FolderResult(rootId, "root", FolderType.ROOT, null, null, null, LocalDateTime.now(), LocalDateTime.now()), null);
	}

	@Test
	@DisplayName("기본 폴더 리스트 조회")
	void basic_folder() throws Exception {
		// given
		List<FolderResult> folderResultList = List.of(
			new FolderResult(rootId, "root", FolderType.ROOT, null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
			new FolderResult(unclassifiedId, "unclassified", FolderType.UNCLASSIFIED, null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
			new FolderResult(recycleBinId, "recycleBin", FolderType.RECYCLE_BIN, null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now())
		);

		given(folderService.getBasicFolderList(userId)).willReturn(folderResultList);

		// when
		mockMvc.perform(get("/api/folders/basic")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(folderApiMapper).should(times(3)).toApiResponse(any());
		then(folderService).should(times(1)).getBasicFolderList(userId);

		folderApiMapper.toApiResponse(null);
		folderApiMapper.toApiResponse(new FolderResult(rootId, "root", FolderType.ROOT, null, null, null, LocalDateTime.now(), LocalDateTime.now()));
	}

	@Test
	@DisplayName("폴더 생성")
	void create_folder() throws Exception {
		// given
		var request = new FolderApiRequest.Create("새 폴더", rootId);
		var command = new FolderCommand.Create(userId, request.name(), request.parentFolderId());
		var result = new FolderResult(folderId, command.name(), FolderType.GENERAL, command.parentFolderId(), new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());

		given(folderService.saveFolder(command)).willReturn(result);

		// when
		mockMvc.perform(post("/api/folders")
				   .content(objectMapper.writeValueAsString(request))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(folderApiMapper).should(times(1)).toCreateCommand(userId, request);
		then(folderService).should(times(1)).saveFolder(command);
		then(folderApiMapper).should(times(1)).toApiResponse(result);

		folderApiMapper.toCreateCommand(null, null);
		folderApiMapper.toCreateCommand(null, request);
		folderApiMapper.toCreateCommand(userId, null);
	}

	@Test
	@DisplayName("폴더 수정")
	void update_folder() throws Exception {
		// given
		var request = new FolderApiRequest.Update(folderId, "변경");
		var command = new FolderCommand.Update(userId, request.id(), request.name());

		// when
		mockMvc.perform(patch("/api/folders")
				   .content(objectMapper.writeValueAsString(request))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isNoContent());

		// then
		then(folderApiMapper).should(times(1)).toUpdateCommand(userId, request);
		then(folderService).should(times(1)).updateFolder(command);

		folderApiMapper.toUpdateCommand(null, null);
		folderApiMapper.toUpdateCommand(null, request);
		folderApiMapper.toUpdateCommand(userId, null);
	}

	@Test
	@DisplayName("폴더 이동")
	void move_folder() throws Exception {
		// given
		List<Long> folderIdList = List.of(folderId, otherFolderId);
		var request = new FolderApiRequest.Move(folderIdList, rootId, rootId, 0);
		var command = new FolderCommand.Move(userId, folderIdList, rootId, rootId, 0);

		// when
		mockMvc.perform(patch("/api/folders/location")
				   .content(objectMapper.writeValueAsString(request))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isNoContent());

		// then
		then(folderApiMapper).should(times(1)).toMoveCommand(userId, request);
		then(folderService).should(times(1)).moveFolder(command);

		folderApiMapper.toMoveCommand(null, null);
		folderApiMapper.toMoveCommand(null, request);
		folderApiMapper.toMoveCommand(userId, null);
		folderApiMapper.toMoveCommand(userId, new FolderApiRequest.Move(null, rootId, rootId, 0));
	}

	@Test
	@DisplayName("폴더 삭제")
	void delete_folder() throws Exception {
		// given
		List<Long> folderIdList = List.of(folderId);
		var request = new FolderApiRequest.Delete(folderIdList);
		var command = new FolderCommand.Delete(userId, folderIdList);

		// when
		mockMvc.perform(delete("/api/folders")
				   .content(objectMapper.writeValueAsString(request))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isNoContent());

		// then
		then(folderApiMapper).should(times(1)).toDeleteCommand(userId, request);
		then(folderService).should(times(1)).deleteFolder(command);

		folderApiMapper.toDeleteCommand(null, null);
		folderApiMapper.toDeleteCommand(null, request);
		folderApiMapper.toDeleteCommand(userId, null);
		folderApiMapper.toDeleteCommand(userId, new FolderApiRequest.Delete(null));
	}
}