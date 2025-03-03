package baguni.domain.infrastructure.pick.dto;

import java.util.List;

import baguni.domain.infrastructure.link.dto.LinkInfo;

public class PickCommand {

	public record Read(Long userId, Long id) {
	}

	public record ReadList(Long userId, List<Long> folderIdList) {
	}

	public record ReadPagination(Long userId, Long folderId, Long cursor, int size) {
	}

	public record SearchPagination(Long userId, List<Long> folderIdList, List<String> searchTokenList,
								   List<Long> tagIdList, Long cursor, int size) {
	}

	public record Create(Long userId, String title, List<Long> tagIdOrderedList, Long parentFolderId,
						 LinkInfo linkInfo) {
	}

	public record CreateFromExtension(Long userId, String title, List<Long> tagIdOrderedList, Long parentFolderId, String url, String linkTitle) {
	}

	public record Extension(Long userId, String title, String url) {
	}

	public record Update(Long userId, Long id, String title, Long parentFolderId, List<Long> tagIdOrderedList) {
	}

	public record Move(Long userId, List<Long> idList, Long destinationFolderId, int orderIdx) {
	}

	public record Delete(Long userId, List<Long> idList) {
	}
}
