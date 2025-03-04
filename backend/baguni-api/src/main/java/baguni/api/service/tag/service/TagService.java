package baguni.api.service.tag.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.TagErrorCode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import baguni.infra.annotation.LoginUserIdDistributedLock;
import baguni.infra.infrastructure.tag.dto.TagCommand;
import baguni.infra.infrastructure.tag.dto.TagMapper;
import baguni.infra.infrastructure.tag.dto.TagResult;
import baguni.infra.infrastructure.tag.TagDataHandler;
import baguni.infra.model.tag.Tag;

@Service
@RequiredArgsConstructor
public class TagService {

	private final TagDataHandler tagDataHandler;
	private final TagMapper tagMapper;

	@WithSpan
	@Transactional(readOnly = true)
	public TagResult getTag(TagCommand.Read command) {
		Tag tag = tagDataHandler.getTag(command.id());
		assertUserIsTagOwner(command.userId(), tag);
		return tagMapper.toResult(tag);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<TagResult> getUserTagList(Long userId) {
		return tagDataHandler.getTagList(userId).stream()
							 .map(tagMapper::toResult).toList();
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public TagResult saveTag(TagCommand.Create command) {
		assertTagNameIsUnique(command.userId(), command.name());
		return tagMapper.toResult(tagDataHandler.saveTag(command.userId(), command));
	}

	@WithSpan
	@Transactional
	public TagResult updateTag(TagCommand.Update command) {
		Tag tag = tagDataHandler.getTag(command.id());
		assertUserIsTagOwner(command.userId(), tag);
		assertTagNameIsUnique(command.userId(), command.name());
		return tagMapper.toResult(tagDataHandler.updateTag(command));
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public void moveUserTag(TagCommand.Move command) {
		Tag tag = tagDataHandler.getTag(command.id());
		assertUserIsTagOwner(command.userId(), tag);
		tagDataHandler.moveTag(command.userId(), command);
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public void deleteTag(TagCommand.Delete command) {
		Tag tag = tagDataHandler.getTag(command.id());
		assertUserIsTagOwner(command.userId(), tag);
		tagDataHandler.deleteTag(command.userId(), command);
	}

	private void assertUserIsTagOwner(Long userId, Tag tag) {
		if (!userId.equals(tag.getUser().getId())) {
			throw new ServiceException(TagErrorCode.UNAUTHORIZED_TAG_ACCESS);
		}
	}

	private void assertTagNameIsUnique(Long userId, String name) {
		if (tagDataHandler.checkDuplicateTagName(userId, name)) {
			throw new ServiceException(TagErrorCode.TAG_ALREADY_EXIST);
		}
	}
}
