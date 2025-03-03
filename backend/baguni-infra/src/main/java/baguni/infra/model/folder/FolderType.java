package baguni.infra.model.folder;

import java.util.EnumSet;

public enum FolderType {

	UNCLASSIFIED("미분류"),
	RECYCLE_BIN("휴지통"),
	ROOT("루트"),
	GENERAL("일반"),
	;

	private final String label;

	FolderType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static EnumSet<FolderType> getBasicFolderTypes() {
		return EnumSet.of(UNCLASSIFIED, RECYCLE_BIN, ROOT);
	}

	public static EnumSet<FolderType> getUnclassifiedFolderTypes() {
		return EnumSet.of(UNCLASSIFIED);
	}
}
