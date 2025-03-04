package baguni.infra.infrastructure.pick;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.FolderErrorCode;
import baguni.common.exception.error_code.PickErrorCode;
import baguni.common.exception.error_code.TagErrorCode;
import baguni.common.exception.error_code.UserErrorCode;
import baguni.infra.infrastructure.folder.FolderQuery;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.infrastructure.pick.dto.PickMapper;
import baguni.infra.model.folder.Folder;
import baguni.infra.infrastructure.folder.FolderRepository;
import baguni.infra.model.link.Link;
import baguni.infra.infrastructure.link.LinkRepository;
import baguni.infra.model.pick.Pick;
import baguni.infra.model.pick.PickTag;
import baguni.infra.model.tag.Tag;
import baguni.infra.infrastructure.tag.TagRepository;
import baguni.infra.model.user.User;
import baguni.infra.infrastructure.user.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PickDataHandler {

	private final PickMapper pickMapper;
	private final PickRepository pickRepository;
	private final PickTagRepository pickTagRepository;
	private final UserRepository userRepository;
	private final FolderRepository folderRepository;
	private final FolderQuery folderQuery;
	private final LinkRepository linkRepository;
	private final TagRepository tagRepository;

	@WithSpan
	@Transactional(readOnly = true)
	public Pick getPick(Long pickId) {
		return pickRepository.findById(pickId)
							 .orElseThrow(() -> new ServiceException(PickErrorCode.PICK_NOT_FOUND));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Pick getPickUrl(Long userId, String url) {
		return pickRepository.findByUserIdAndLinkUrl(userId, url)
							 .orElseThrow(() -> new ServiceException(PickErrorCode.PICK_NOT_FOUND));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Optional<Pick> findPickUrl(Long userId, String url) {
		return pickRepository.findByUserIdAndLinkUrl(userId, url);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Pick> getPickList(List<Long> pickIdList) {
		List<Pick> pickList = pickRepository.findAllById_JoinLink(pickIdList);
		// 조회 리스트에 존재하지 않는 픽이 있으면 예외 발생
		if (pickList.size() != pickIdList.size()) {
			throw new ServiceException(PickErrorCode.PICK_NOT_FOUND);
		}
		return pickList;
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Pick> getPickListPreservingOrder(List<Long> pickIdList) {
		List<Pick> pickList = pickRepository.findAllById_JoinLink(pickIdList);
		// 조회리스트에 존재하지 않는 픽이 있으면 예외 발생
		if (pickList.size() != pickIdList.size()) {
			throw new ServiceException(PickErrorCode.PICK_NOT_FOUND);
		}
		pickList.sort(Comparator.comparing(pick -> pickIdList.indexOf(pick.getId())));
		return pickList;
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<PickTag> getPickTagList(Long pickId) {
		return pickTagRepository.findAllByPickId(pickId);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public boolean existsByUserIdAndLink(Long userId, Link link) {
		return pickRepository.existsByUserIdAndLink(userId, link);
	}

	/**
	 * 이 메서드는 사이트에서 픽 추가할 때만 사용하는 메서드
	 * 현재 사용하고 있지 않은 상태
	 * 아예 사용안하면 제거해도 괜찮을듯
	 *
	 * 사이트에서 픽 추가 못하도록 막았던 이유 : Url 검증이 까다로워서 (익스텐션에서만 하기로 변경)
	 */
	@WithSpan
	@Transactional
	public Pick savePick(PickCommand.Create command) {
		User user = userRepository.findById(command.userId())
								  .orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		Folder folder = folderRepository.findById(command.parentFolderId())
										.orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));
		Link link = linkRepository.findByUrl(command.linkInfo().url())
								  .orElseGet(() -> linkRepository.save(Link.createLink(command.linkInfo().url())));

		Pick savedPick = pickRepository.save(pickMapper.toEntity(command, user, folder, link));
		Folder parentFolder = savedPick.getParentFolder();
		attachPickToParentFolder(savedPick, parentFolder);

		List<PickTag> pickTagList = tagRepository.findAllById(command.tagIdOrderedList())
												 .stream()
												 .map(tag -> PickTag.of(savedPick, tag))
												 .toList();
		pickTagRepository.saveAll(pickTagList);
		return savedPick;
	}

	/**
	 * @author sangwon
	 * 익스텐션에서 픽 생성하는 메서드
	 * 태그, 부모 폴더까지 설정
	 */
	@WithSpan
	@Transactional
	public Pick savePickFromExtension(PickCommand.CreateFromExtension command) {
		User user = userRepository.findById(command.userId())
								  .orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		Folder folder = folderRepository.findById(command.parentFolderId())
										.orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));
		Link link = linkRepository.findByUrl(command.url())
								  .orElseGet(() -> linkRepository.save(Link.createLink(command.url(),
									  command.linkTitle())));

		Pick savedPick = pickRepository.save(pickMapper.toEntity(command, user, folder, link));
		Folder parentFolder = savedPick.getParentFolder();
		attachPickToParentFolder(savedPick, parentFolder);

		List<PickTag> pickTagList = tagRepository.findAllById(command.tagIdOrderedList())
												 .stream()
												 .map(tag -> PickTag.of(savedPick, tag))
												 .toList();
		pickTagRepository.saveAll(pickTagList);
		return savedPick;
	}

	/**
	 * @author sangwon
	 * 익스텐션에서 사용하지 않는 경우, 제거 예정
	 */
	@WithSpan
	@Transactional
	public Pick savePickToUnclassified(PickCommand.Extension command) {
		User user = userRepository
			.findById(command.userId())
			.orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		Folder unclassified = folderQuery.findUnclassified(user.getId());
		Link link = linkRepository
			.findByUrl(command.url())
			.orElseGet(() -> linkRepository.save(Link.createLink(command.url(), command.title())));

		Pick pick = pickMapper.toEntity(command.title(), new ArrayList<>(), user, unclassified, link);
		Pick savedPick = pickRepository.save(pick);
		attachPickToParentFolder(savedPick, unclassified);
		return savedPick;
	}

	/**
	 * 부모 폴더 픽 리스트에서 pick 제거 후
	 * 이동하는 폴더 픽 리스트에 pick 추가
	 */
	@WithSpan
	@Transactional
	public Pick updatePick(PickCommand.Update command) {
		Pick pick = pickRepository.findById(command.id())
								  .orElseThrow(() -> new ServiceException(PickErrorCode.PICK_NOT_FOUND));
		pick.updateTitle(command.title());

		Folder parentFolder = pick.getParentFolder();

		if (Objects.nonNull(command.parentFolderId()) &&
			isDifferentFolder(parentFolder, command)
		) {
			Folder destinationFolder = folderRepository.findById(command.parentFolderId())
													   .orElseThrow(() -> new ServiceException(
														   FolderErrorCode.FOLDER_NOT_FOUND));
			detachPickFromParentFolder(pick, parentFolder);
			attachPickToParentFolder(pick, destinationFolder);
			updatePickParentFolder(pick, destinationFolder);
		}

		if (command.tagIdOrderedList() != null) {
			updateNewTagIdList(pick, command.tagIdOrderedList());
		}
		return pick;
	}

	@WithSpan
	@Transactional
	public void movePickToCurrentFolder(PickCommand.Move command) {
		List<Long> pickIdList = command.idList();
		Folder folder = folderRepository.findById(command.destinationFolderId())
										.orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));
		movePickListToDestinationFolder(pickIdList, folder, command.orderIdx());
	}

	@WithSpan
	@Transactional
	public void movePickToOtherFolder(PickCommand.Move command) {
		List<Long> pickIdList = command.idList();
		Folder destinationFolder = folderRepository.findById(command.destinationFolderId())
												   .orElseThrow(
													   () -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));

		List<Pick> pickList = pickRepository.findAllById(pickIdList);
		pickList.forEach(pick -> {
			detachPickFromParentFolder(pick, pick.getParentFolder());
			updatePickParentFolder(pick, destinationFolder);
		});

		movePickListToDestinationFolder(pickIdList, destinationFolder, command.orderIdx());
	}

	@WithSpan
	@Transactional
	public void movePickListToRecycleBin(Long userId, List<Long> pickIdList) {
		Folder recycleBin = folderQuery.findRecycleBin(userId);

		// 픽들의 부모를 휴지통으로 변경
		List<Pick> pickList = pickRepository.findAllById(pickIdList);
		pickList.forEach(pick -> {
			attachPickToParentFolder(pick, recycleBin);
			updatePickParentFolder(pick, recycleBin);
		});
	}

	@WithSpan
	@Transactional
	public void deletePickList(PickCommand.Delete command) {
		List<Long> pickIdList = command.idList();
		List<Pick> pickList = pickRepository.findAllById(pickIdList);

		pickList.forEach(pick -> {
			detachPickFromParentFolder(pick, pick.getParentFolder());
			pickTagRepository.deleteByPick(pick);
			pickRepository.delete(pick);
		});
	}

	@WithSpan
	@Transactional
	public void deletePickFromRecycleBin(Long userId) {
		Folder recycleBin = folderQuery.findRecycleBin(userId);
		List<Long> pickIdList = recycleBin.getChildPickIdOrderedList();

		pickIdList.forEach(pickId -> {
			pickTagRepository.deleteByPickId(pickId);
			pickRepository.deleteById(pickId);
		});
		pickIdList.clear();
	}

	@WithSpan
	@Transactional
	public void attachTagToPickTag(Pick pick, Long tagId) {
		Tag tag = tagRepository.findById(tagId)
							   .orElseThrow(() -> new ServiceException(TagErrorCode.TAG_NOT_FOUND));
		PickTag pickTag = PickTag.of(pick, tag);
		pickTagRepository.save(pickTag);
	}

	@WithSpan
	@Transactional
	public void detachTagFromPickTag(Pick pick, Long tagId) {
		pickTagRepository.findByPickAndTagId(pick, tagId)
						 .ifPresent(pickTagRepository::delete);
	}

	// 부모 폴더의 픽 리스트에 추가
	private void attachPickToParentFolder(Pick pick, Folder folder) {
		folder.addChildPickIdOrdered(pick.getId());
	}

	// 부모 폴더의 픽 리스트에서 제거
	private void detachPickFromParentFolder(Pick pick, Folder folder) {
		folder.removeChildPickIdOrdered(pick.getId());
	}

	// 픽의 부모 폴더 변경
	private void updatePickParentFolder(Pick pick, Folder folder) {
		pick.updateParentFolder(folder);
	}

	// 픽 리스트 순서를 유지한 채 목적지 폴더로 이동
	private void movePickListToDestinationFolder(List<Long> pickIdList, Folder folder, int orderIdx) {
		folder.updateChildPickIdOrderedList(pickIdList, orderIdx);
	}

	private void updateNewTagIdList(Pick pick, List<Long> newTagOrderList) {
		// 1. 기존 태그와 새로운 태그를 비교하여 없어진 태그를 PickTag 테이블에서 제거
		pick.getTagIdOrderedList().stream()
			.filter(tagId -> !newTagOrderList.contains(tagId))
			.forEach(tagId -> detachTagFromPickTag(pick, tagId));

		// 2. 새로운 태그 중 기존에 없는 태그를 PickTag 테이블에 추가
		newTagOrderList.stream()
					   .filter(tagId -> !pick.getTagIdOrderedList().contains(tagId))
					   .forEach(tagId -> attachTagToPickTag(pick, tagId));

		pick.updateTagOrderList(newTagOrderList);
	}

	private boolean isDifferentFolder(Folder parentFolder, PickCommand.Update command) {
		return !Objects.equals(parentFolder.getId(), command.parentFolderId());
	}

}