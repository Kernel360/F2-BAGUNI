package baguni.domain.infrastructure.tag;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.TagErrorCode;
import baguni.common.exception.error_code.UserErrorCode;
import baguni.domain.infrastructure.pick.PickRepository;
import baguni.domain.infrastructure.pick.PickTagRepository;
import baguni.domain.model.tag.Tag;
import baguni.domain.model.user.User;
import baguni.domain.infrastructure.user.UserRepository;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.domain.infrastructure.tag.dto.TagCommand;
import baguni.domain.infrastructure.tag.dto.TagMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagDataHandler {

	private final TagRepository tagRepository;
	private final PickRepository pickRepository;
	private final PickTagRepository pickTagRepository;
	private final UserRepository userRepository;
	private final TagMapper tagMapper;

	@WithSpan
	@Transactional(readOnly = true)
	public Tag getTag(Long tagId) {
		return tagRepository.findById(tagId).orElseThrow(() -> new ServiceException(TagErrorCode.TAG_NOT_FOUND));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Tag> getTagList(Long userId) {
		User user = userRepository
			.findById(userId)
			.orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		List<Long> tagOrderList = user.getTagOrderList();
		List<Tag> tagList = tagRepository.findAllByUserId(userId);
		tagList.sort(Comparator.comparing(tag -> tagOrderList.indexOf(tag.getId())));

		return tagList;
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Tag> getTagList(List<Long> tagOrderList) {
		List<Tag> tagList = tagRepository.findAllById(tagOrderList);
		// 조회리스트에 존재하지 않는 태그id가 존재하면 예외 발생
		if (tagList.size() != tagOrderList.size()) {
			throw new ServiceException(TagErrorCode.TAG_NOT_FOUND);
		}
		return tagList;
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Tag> getTagListPreservingOrder(List<Long> tagOrderList) {
		List<Tag> tagList = tagRepository.findAllById(tagOrderList);
		// 조회리스트에 존재하지 않는 태그id가 존재하면 예외 발생
		if (tagList.size() != tagOrderList.size()) {
			throw new ServiceException(TagErrorCode.TAG_NOT_FOUND);
		}
		tagList.sort(Comparator.comparing(tag -> tagOrderList.indexOf(tag.getId())));
		return tagList;
	}

	@WithSpan
	@Transactional
	public Tag saveTag(Long userId, TagCommand.Create command) {
		User user = userRepository
			.findById(userId)
			.orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		Tag tag = tagRepository.save(tagMapper.toEntity(command, user));
		user.getTagOrderList().add(tag.getId());
		return tag;
	}

	@WithSpan
	@Transactional
	public Tag updateTag(TagCommand.Update command) {
		log.info("TagDataHandler: tag id={}", command.id()); // for debug
		Tag tag = tagRepository
			.findById(command.id())
			.orElseThrow(() -> new ServiceException(TagErrorCode.TAG_NOT_FOUND));
		tag.updateTagName(command.name());
		tag.updateColorNumber(command.colorNumber());
		return tag;
	}

	@WithSpan
	@Transactional
	public void moveTag(Long userId, TagCommand.Move command) {
		User user = userRepository
			.findById(userId)
			.orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		user.updateTagOrderList(command.id(), command.orderIdx());
	}

	@WithSpan
	@Transactional
	public void deleteTag(Long userId, TagCommand.Delete command) {
		User user = userRepository
			.findById(userId)
			.orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		Long tagId = command.id();
		user.getTagOrderList().remove(tagId);
		pickTagRepository.findAllByTagId(tagId).stream()
						 .map(pickTag -> pickRepository.findById(pickTag.getPick().getId())
													   .orElseThrow(
														   () -> new ServiceException(TagErrorCode.TAG_NOT_FOUND)))
						 .forEach(pick -> {
							 pick.getTagIdOrderedList().remove(tagId);
						 });
		pickTagRepository.deleteByTagId(tagId);
		tagRepository.deleteById(tagId);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public boolean checkDuplicateTagName(Long userId, String name) {
		return tagRepository.existsByUserIdAndName(userId, name);
	}
}
