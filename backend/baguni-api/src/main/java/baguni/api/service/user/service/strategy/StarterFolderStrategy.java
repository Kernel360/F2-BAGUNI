package baguni.api.service.user.service.strategy;

import org.springframework.stereotype.Component;

import baguni.infra.infrastructure.user.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.infra.infrastructure.folder.dto.FolderCommand;
import baguni.infra.infrastructure.folder.FolderDataHandler;
import baguni.infra.model.folder.Folder;

@Slf4j
@Component
@RequiredArgsConstructor
public class StarterFolderStrategy {

	private static final String FOLDER_NAME = "시작하기";

	private final FolderDataHandler folderDataHandler;
	private final RankingInitStrategy rankingInitStrategy;
	private final ManualInitStrategy manualInitStrategy;

	/**
	 * @author minkyeu kim
	 * 시작 폴더 생성에 실패해도, 회원 가입은 진행되야 한다.
	 * 따라서 Transaction으로 처리하지 않음.
	 */
	public void initRootFolder(UserInfo info) {
		var starterFolder = createStarterFolder(info);
		rankingInitStrategy.initContent(info, starterFolder.getId());
		manualInitStrategy.initContent(info, starterFolder.getId());
	}

	private Folder createStarterFolder(UserInfo info) {
		var root = folderDataHandler.getRootFolder(info.id());
		var command = new FolderCommand.Create(info.id(), FOLDER_NAME, root.getId());
		return folderDataHandler.saveFolder(command);
	}
}
