package kernel360.techpick.feature.structure.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kernel360.techpick.core.model.folder.StructureJson;
import kernel360.techpick.feature.folder.service.FolderStructureService;
import kernel360.techpick.feature.pick.service.PickStructureService;
import kernel360.techpick.feature.structure.model.StructureJsonProvider;
import kernel360.techpick.feature.structure.model.StructureMapper;
import kernel360.techpick.feature.structure.service.dto.StructureDeleteRequest;
import kernel360.techpick.feature.structure.service.dto.StructureMoveRequest;
import kernel360.techpick.feature.structure.service.node.client.ClientNode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StructureService {

	private final FolderStructureService folderStructureService;
	private final PickStructureService pickStructureService;
	private final StructureJsonProvider provider;
	private final StructureMapper mapper;

	public String getStructureByUserId(Long userId) {

		StructureJson structureJson = provider.findByUserId(userId);

		return Structure.fromJson(structureJson.getStructure(), ClientNode.class)
			.serialize();
	}

	@Transactional
	public void moveFolder(StructureMoveRequest request) {

		folderStructureService.moveFolder(mapper.toFolderMoveDto(request));

		// TODO: validate serverNodeStructure
		// validator.validate(request.getStructure());

		provider.updateStructureJsonByUserIdAndStructure(
			request.getUserId(),
			request.getStructure()
		);
	}

	@Transactional
	public void deleteFolder(StructureDeleteRequest request) {

		folderStructureService.deleteFolder(mapper.toFolderDeleteDto(request));

		// TODO: validate serverNodeStructure
		// validator.validate(serverNodeStructure);

		provider.updateStructureJsonByUserIdAndStructure(
			request.getUserId(),
			request.getStructure()
		);
	}

	@Transactional
	public void movePick(StructureMoveRequest request) {

		pickStructureService.movePick(mapper.toPickMoveDto(request));

		// TODO: validate serverNodeStructure
		// validator.validate(serverNodeStructure);

		provider.updateStructureJsonByUserIdAndStructure(
			request.getUserId(),
			request.getStructure()
		);
	}

	@Transactional
	public void deletePick(StructureDeleteRequest request) {

		pickStructureService.deletePick(mapper.toPickDeleteDto(request));

		// TODO: validate serverNodeStructure
		// validator.validate(serverNodeStructure);

		provider.updateStructureJsonByUserIdAndStructure(
			request.getUserId(),
			request.getStructure()
		);
	}

}