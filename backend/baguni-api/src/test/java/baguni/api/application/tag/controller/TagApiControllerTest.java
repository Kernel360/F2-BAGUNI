package baguni.api.application.tag.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import baguni.api.application.tag.dto.TagApiMapper;
import baguni.api.application.tag.dto.TagApiMapperImpl;
import baguni.api.application.tag.dto.TagApiRequest;
import baguni.api.service.tag.service.TagService;
import baguni.api.service.user.service.UserService;
import baguni.infra.infrastructure.tag.dto.TagCommand;
import baguni.infra.infrastructure.tag.dto.TagResult;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc // MockMvc 사용을 위한 설정
@Import(TagApiMapperImpl.class) // @SpyBean 구현체 인식을 못하여 추가
@DisplayName("태그 컨트롤러 - 슬라이스 테스트")
class TagApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SecurityFilterChain securityFilterChain; // 실제 시큐리티 구현체 사용하지 않기 위함.

	@SpyBean
	private TagApiMapper tagApiMapper;

	@MockBean
	private TagService tagService;

	@MockBean
	private UserService userService;

	Long userId, tagId, otherTagId;
	IDToken idToken;
	UserInfo userInfo;

	@BeforeEach
	void setUp() {
		userId = 1L;
		tagId = 1L;
		otherTagId = 2L;

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
	@DisplayName("사용자 태그 조회")
	void get_tag() throws Exception {
		// given
		var result = List.of(
			new TagResult(tagId, "태그1", 0, userId),
			new TagResult(otherTagId, "태그2", 0, userId)
		);

		given(tagService.getUserTagList(userId)).willReturn(result);

		// when
		mockMvc.perform(get("/api/tags")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(tagService).should(times(1)).getUserTagList(userId);
		then(tagApiMapper).should(times(result.size())).toReadResponse(any());

		tagApiMapper.toReadResponse(null);
	}

	@Test
	@DisplayName("태그 추가")
	void create_tag() throws Exception {
		// given
		var request = new TagApiRequest.Create("태그", 0);
		var command = new TagCommand.Create(userId, request.name(), request.colorNumber());
		var result = new TagResult(tagId, command.name(), command.colorNumber(), userId);

		given(tagService.saveTag(command)).willReturn(result);

		// when
		mockMvc.perform(post("/api/tags")
				   .content(objectMapper.writeValueAsString(request))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(tagApiMapper).should(times(1)).toCreateCommand(userId, request);
		then(tagService).should(times(1)).saveTag(command);
		then(tagApiMapper).should(times(1)).toCreateResponse(result);

		tagApiMapper.toCreateCommand(null, null);
		tagApiMapper.toCreateCommand(userId, null);
		tagApiMapper.toCreateCommand(null, request);
		tagApiMapper.toCreateResponse(null);
	}

	@Test
	@DisplayName("태그 수정")
	void update_tag() throws Exception {
		// given
		var request = new TagApiRequest.Update(tagId, "태그 수정", 1);
		var command = new TagCommand.Update(userId, request.id(), request.name(), request.colorNumber());

		// when
		mockMvc.perform(patch("/api/tags")
				   .content(objectMapper.writeValueAsString(request))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isNoContent());

		// then
		then(tagApiMapper).should(times(1)).toUpdateCommand(userId, request);
		then(tagService).should(times(1)).updateTag(command);

		tagApiMapper.toUpdateCommand(null, null);
		tagApiMapper.toUpdateCommand(userId, null);
		tagApiMapper.toUpdateCommand(null, request);
	}

	@Test
	@DisplayName("태그 이동")
	void move_tag() throws Exception {
		// given
		var request = new TagApiRequest.Move(tagId, 0);
		var command = new TagCommand.Move(userId, request.id(), request.orderIdx());

		// when
		mockMvc.perform(patch("/api/tags/location")
				   .content(objectMapper.writeValueAsString(request))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isNoContent());

		// then
		then(tagApiMapper).should(times(1)).toMoveCommand(userId, request);
		then(tagService).should(times(1)).moveUserTag(command);

		tagApiMapper.toMoveCommand(null, null);
		tagApiMapper.toMoveCommand(userId, null);
		tagApiMapper.toMoveCommand(null, request);
	}

	@Test
	@DisplayName("태그 삭제")
	void delete_tag() throws Exception {
		// given
		var request = new TagApiRequest.Delete(tagId);
		var command = new TagCommand.Delete(userId, request.id());

		// when
		mockMvc.perform(delete("/api/tags")
				   .content(objectMapper.writeValueAsString(request))
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isNoContent());

		// then
		then(tagApiMapper).should(times(1)).toDeleteCommand(userId, request);
		then(tagService).should(times(1)).deleteTag(command);

		tagApiMapper.toDeleteCommand(null, null);
		tagApiMapper.toDeleteCommand(userId, null);
		tagApiMapper.toDeleteCommand(null, request);
	}
}