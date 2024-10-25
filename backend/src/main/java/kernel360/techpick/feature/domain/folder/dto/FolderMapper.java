package kernel360.techpick.feature.domain.folder.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import kernel360.techpick.core.model.folder.Folder;
import kernel360.techpick.core.model.user.User;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface FolderMapper {

	@Mapping(source = "id", target = "folderId")
	@Mapping(source = "parentFolder.id", target = "destinationFolderId")
	@Mapping(source = "user.id", target = "userId")
	FolderResult toResult(Folder folder);

	@Mapping(target = "folderType", constant = "GENERAL")
	@Mapping(source = "command.name", target = "name")
	@Mapping(target = "childFolderOrderList", expression = "java(new ArrayList<>())")
	@Mapping(target = "childPickOrderList", expression = "java(new ArrayList<>())")
	Folder toEntity(FolderCommand.Create command, User user, Folder parentFolder);

}
