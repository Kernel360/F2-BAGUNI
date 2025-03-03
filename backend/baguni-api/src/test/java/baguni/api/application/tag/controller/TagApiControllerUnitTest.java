package baguni.api.application.tag.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.application.tag.dto.TagApiMapper;
import baguni.api.application.tag.dto.TagApiRequest;
import baguni.api.application.tag.dto.TagApiResponse;
import baguni.api.service.tag.service.TagService;
import baguni.infra.exception.tag.ApiTagException;
import baguni.infra.infrastructure.tag.dto.TagCommand;
import baguni.infra.infrastructure.tag.dto.TagResult;

@DisplayName("태그 컨트롤러 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class TagApiControllerUnitTest {

	@Mock
	private TagService tagService;

	@Mock
	private TagApiMapper tagApiMapper;

	@InjectMocks
	private TagApiController tagApiController;

	Long userId, tagId, otherTagId;

	@BeforeEach
	void setUp() {
		userId = 1L;
		tagId = 1L;
		otherTagId = 2L;
	}

	@Test
	@DisplayName("사용자 태그 조회")
	void get_tag() {
		// given
		var result = List.of(
			new TagResult(tagId, "태그1", 0, userId),
			new TagResult(otherTagId, "태그2", 0, userId)
		);

		given(tagService.getUserTagList(userId)).willReturn(result);
		for (TagResult tagResult : result) {
			var response = new TagApiResponse.Read(tagResult.id(), tagResult.name(), tagResult.colorNumber());
			given(tagApiMapper.toReadResponse(tagResult)).willReturn(response);
		}

		// when
		tagApiController.getAllUserTag(userId);

		// then
		then(tagService).should(times(1)).getUserTagList(userId);
		then(tagApiMapper).should(times(result.size())).toReadResponse(any());
	}

	@Test
	@DisplayName("태그 추가")
	void create_tag() {
		// given
		var request = new TagApiRequest.Create("태그", 0);
		var command = new TagCommand.Create(userId, request.name(), request.colorNumber());
		var result = new TagResult(tagId, command.name(), command.colorNumber(), userId);
		var response = new TagApiResponse.Create(result.id(), result.name(), result.colorNumber());

		given(tagApiMapper.toCreateCommand(userId, request)).willReturn(command);
		given(tagService.saveTag(command)).willReturn(result);
		given(tagApiMapper.toCreateResponse(result)).willReturn(response);

		// when
		tagApiController.createTag(userId, request);

		// then
		then(tagApiMapper).should(times(1)).toCreateCommand(userId, request);
		then(tagService).should(times(1)).saveTag(command);
		then(tagApiMapper).should(times(1)).toCreateResponse(result);
	}

	@Test
	@DisplayName("태그 추가 - 200자 이상")
	void create_tag_length() {
		// given
		var request = new TagApiRequest.Create("Test".repeat(51), 0);

		// when, then
		assertThatThrownBy(() -> tagApiController.createTag(userId, request))
			.isInstanceOf(ApiTagException.class)
			.hasMessageStartingWith(ApiTagException.TAG_NAME_TOO_LONG().getMessage());
	}

	@Test
	@DisplayName("태그 수정")
	void update_tag() {
		// given
		var request = new TagApiRequest.Update(tagId, "태그 수정", 1);
		var command = new TagCommand.Update(userId, request.id(), request.name(), request.colorNumber());

		given(tagApiMapper.toUpdateCommand(userId, request)).willReturn(command);

		// when
		tagApiController.updateTag(userId, request);

		// then
		then(tagApiMapper).should(times(1)).toUpdateCommand(userId, request);
		then(tagService).should(times(1)).updateTag(command);
	}

	@Test
	@DisplayName("태그 수정 - 200자 이상")
	void update_tag_length() {
		// given
		var request = new TagApiRequest.Update(tagId, "Test".repeat(51), 1);

		// when, then
		assertThatThrownBy(() -> tagApiController.updateTag(userId, request))
			.isInstanceOf(ApiTagException.class)
			.hasMessageStartingWith(ApiTagException.TAG_NAME_TOO_LONG().getMessage());
	}

	@Test
	@DisplayName("태그 이동")
	void move_tag() {
		// given
		var request = new TagApiRequest.Move(tagId, 0);
		var command = new TagCommand.Move(userId, request.id(), request.orderIdx());

		given(tagApiMapper.toMoveCommand(userId, request)).willReturn(command);

		// when
		tagApiController.moveTag(userId, request);

		// then
		then(tagApiMapper).should(times(1)).toMoveCommand(userId, request);
		then(tagService).should(times(1)).moveUserTag(command);
	}

	@Test
	@DisplayName("태그 삭제")
	void delete_tag() {
		// given
		var request = new TagApiRequest.Delete(tagId);
		var command = new TagCommand.Delete(userId, request.id());

		given(tagApiMapper.toDeleteCommand(userId, request)).willReturn(command);

		// when
		tagApiController.deleteTag(userId, request);

		// then
		then(tagApiMapper).should(times(1)).toDeleteCommand(userId, request);
		then(tagService).should(times(1)).deleteTag(command);
	}
}