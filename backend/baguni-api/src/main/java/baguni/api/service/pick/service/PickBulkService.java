package baguni.api.service.pick.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.infra.infrastructure.folder.FolderDataHandler;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.pick.PickBulkDataHandler;
import baguni.infra.infrastructure.pick.PickDataHandler;
import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.pick.Pick;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PickBulkService {

	private final FolderDataHandler folderDataHandler;
	private final PickDataHandler pickDataHandler;
	private final PickBulkDataHandler pickBulkDataHandler;

	@Transactional
	public void saveBulkPick(Long userId, Long parentFolderId) {
		List<PickCommand.Create> pickList = new ArrayList<>();
		Folder parentFolder = folderDataHandler.getFolder(parentFolderId);

		for (int i = 0; i < 100; i++) {
			LinkInfo linkInfo = new LinkInfo("test" + i, "링크 제목", "링크 설명", "");
			PickCommand.Create command = new PickCommand.Create(userId, "테스트 제목", new ArrayList<>(),
				parentFolderId, linkInfo);
			pickList.add(command);
		}
		pickBulkDataHandler.bulkInsertPick(pickList);

		for (PickCommand.Create command : pickList) {
			Pick pick = pickDataHandler.getPickUrl(userId, command.linkInfo().url());
			parentFolder.addChildPickIdOrdered(pick.getId());
		}
	}
}