package techpick.api.infrastructure.pick;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import techpick.api.domain.folder.exception.ApiFolderException;
import techpick.api.domain.link.dto.LinkMapper;
import techpick.api.domain.pick.dto.PickCommand;
import techpick.api.domain.pick.dto.PickMapper;
import techpick.api.domain.pick.exception.ApiPickException;
import techpick.api.domain.tag.exception.ApiTagException;
import techpick.api.domain.user.exception.ApiUserException;
import techpick.core.model.folder.Folder;
import techpick.core.model.folder.FolderRepository;
import techpick.core.model.link.Link;
import techpick.core.model.link.LinkRepository;
import techpick.core.model.pick.Pick;
import techpick.core.model.pick.PickRepository;
import techpick.core.model.pick.PickTag;
import techpick.core.model.pick.PickTagRepository;
import techpick.core.model.tag.Tag;
import techpick.core.model.tag.TagRepository;
import techpick.core.model.user.User;
import techpick.core.model.user.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PickDataHandler {

	private final PickMapper pickMapper;
	private final PickRepository pickRepository;
	private final PickTagRepository pickTagRepository;
	private final UserRepository userRepository;
	private final FolderRepository folderRepository;
	private final LinkRepository linkRepository;
	private final LinkMapper linkMapper;
	private final TagRepository tagRepository;

	@Transactional(readOnly = true)
	public Pick getPick(Long pickId) {
		return pickRepository.findById(pickId).orElseThrow(ApiPickException::PICK_NOT_FOUND);
	}

	@Transactional(readOnly = true)
	public Pick getPickUrl(Long userId, String url) {
		return pickRepository.findByUserIdAndLinkUrl(userId, url)
			.orElseThrow(ApiPickException::PICK_NOT_FOUND);
	}

	@Transactional(readOnly = true)
	public List<Pick> getPickList(List<Long> idList) {
		return pickRepository.findAllById(idList);
	}

	@Transactional(readOnly = true)
	public List<Pick> getPickListPreservingOrder(List<Long> idList) {
		List<Pick> pickList = pickRepository.findAllById(idList);
		pickList.sort(Comparator.comparing(pick -> idList.indexOf(pick.getId())));
		return pickList;
	}

	@Transactional(readOnly = true)
	public List<PickTag> getPickTagList(Long pickId) {
		return pickTagRepository.findAllByPickId(pickId);
	}

	@Transactional
	public Pick savePick(PickCommand.Create command) throws ApiPickException {
		User user = userRepository.findById(command.userId()).orElseThrow(ApiUserException::USER_NOT_FOUND);
		Folder folder = folderRepository.findById(command.parentFolderId())
			.orElseThrow(ApiFolderException::FOLDER_NOT_FOUND);
		Link link = linkRepository.findByUrl(command.linkInfo().url())
			.map(existLink -> {
				existLink.updateMetadata(command.linkInfo().title(), command.linkInfo().description(),
					command.linkInfo().imageUrl());
				return existLink;
			})
			.orElseGet(() -> linkRepository.save(linkMapper.of(command.linkInfo())));

		// 픽 존재 여부 검증
		pickRepository.findByUserAndLink(user, link)
			.ifPresent((__) -> {
				throw ApiPickException.PICK_MUST_BE_UNIQUE_FOR_A_URL();
			});

		// 태그 존재 여부 검증
		validateTagIdList(command.tagIdOrderedList());

		Pick savedPick = pickRepository.save(pickMapper.toEntity(command, user, folder, link));
		savedPick.getParentFolder().addChildPickIdOrdered(savedPick.getId());
		return savedPick;
	}

	@Transactional
	public PickTag savePickTag(Pick pick, Tag tag) {
		return pickTagRepository.save(PickTag.of(pick, tag));
	}

	@Transactional
	public Pick updatePick(PickCommand.Update command) {
		Pick pick = getPick(command.id());
		pick.updateTitle(command.title());

		// 제목만 수정하는 경우
		if (ObjectUtils.isNotEmpty(command.title()) && Objects.isNull(command.tagIdOrderedList())) {
			return pick;
		}

		// 태그 존재 여부 검증
		validateTagIdList(command.tagIdOrderedList());
		updateNewTagIdList(pick, command.tagIdOrderedList());
		return pick;
	}

	@Transactional
	public void movePickToCurrentFolder(PickCommand.Move command) {
		List<Long> pickIdList = command.idList();
		Folder folder = folderRepository.findById(command.destinationFolderId())
			.orElseThrow(ApiFolderException::FOLDER_NOT_FOUND);
		folder.updateChildPickIdOrderedList(pickIdList, command.orderIdx());
	}

	@Transactional
	public void movePickToOtherFolder(PickCommand.Move command) {
		List<Long> pickIdList = command.idList();
		Folder destinationFolder = folderRepository.findById(command.destinationFolderId())
			.orElseThrow(ApiFolderException::FOLDER_NOT_FOUND);
		destinationFolder.updateChildPickIdOrderedList(pickIdList, command.orderIdx());

		for (Long pickId : pickIdList) {
			Pick pick = getPick(pickId);
			pick.getParentFolder().removeChildPickOrder(pickId);
			pick.updateParentFolder(destinationFolder);
		}
	}

	@Transactional
	public void deletePickList(PickCommand.Delete command) {
		List<Long> pickIdList = command.idList();
		for (Long pickId : pickIdList) {
			Pick pick = getPick(pickId);
			pick.getParentFolder().removeChildPickOrder(pickId);
			pickTagRepository.deleteByPick(pick);
			pickRepository.delete(pick);
		}
	}

	@Transactional
	public void attachTagToPick(Pick pick, Long tagId) {
		Tag tag = tagRepository.findById(tagId)
			.orElseThrow(ApiTagException::TAG_NOT_FOUND);
		PickTag pickTag = PickTag.of(pick, tag);
		pickTagRepository.save(pickTag);
	}

	@Transactional
	public void detachTagFromPick(Pick pick, Long tagId) {
		pickTagRepository.deleteByPickAndTagId(pick, tagId);
	}

	@Transactional
	public void detachTagFromEveryPick(Long tagId) {
		pickTagRepository.deleteByTagId(tagId);
	}

	private void updateNewTagIdList(Pick pick, List<Long> newTagOrderList) {
		// 1. 기존 태그와 새로운 태그를 비교하여 없어진 태그를 PickTag 테이블에서 제거
		pick.getTagIdOrderedList().stream()
			.filter(tagId -> !newTagOrderList.contains(tagId))
			.forEach(tagId -> detachTagFromPick(pick, tagId));

		// 2. 새로운 태그 중 기존에 없는 태그를 PickTag 테이블에 추가
		newTagOrderList.stream()
			.filter(tagId -> !pick.getTagIdOrderedList().contains(tagId))
			.forEach(tagId -> attachTagToPick(pick, tagId));

		pick.updateTagOrderList(newTagOrderList);
	}

	private void validateTagIdList(List<Long> tagIdOrderedList) {
		tagIdOrderedList
			.forEach(tagId -> tagRepository.findById(tagId)
				.orElseThrow(ApiTagException::TAG_NOT_FOUND));
	}

}

