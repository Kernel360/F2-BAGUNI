package baguni.domain.infrastructure.sharedFolder.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import baguni.domain.infrastructure.folder.dto.FolderMapper;
import baguni.domain.model.sharedFolder.SharedFolder;
import baguni.domain.model.tag.Tag;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR,
	uses = FolderMapper.class
)
public interface SharedFolderMapper {

	@Mapping(expression = "java(sharedFolder.getId().toString())", target = "folderAccessToken")
	SharedFolderResult.Create toCreateResult(SharedFolder sharedFolder);

	@Mapping(source = "folder", target = "sourceFolder")
	@Mapping(expression = "java(sharedFolder.getId().toString())", target = "folderAccessToken")
	SharedFolderResult.Read toReadResult(SharedFolder sharedFolder);

	SharedFolderResult.SharedTagInfo toSharedTagInfo(Tag tag);
}
