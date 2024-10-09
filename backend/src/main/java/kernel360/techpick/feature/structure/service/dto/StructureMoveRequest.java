package kernel360.techpick.feature.structure.service.dto;

import kernel360.techpick.feature.structure.service.Structure;
import kernel360.techpick.feature.structure.service.node.server.ServerNode;

public record StructureMoveRequest(
	Long targetId, // folder or pick
	Long parentFolderId, // destination
	Structure<ServerNode> structure
) {}
