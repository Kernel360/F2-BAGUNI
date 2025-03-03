package baguni.domain.infrastructure.folder.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import baguni.domain.model.folder.Folder;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface FolderMapper {

	@Mapping(source = "folder.parentFolder.id", target = "parentFolderId")
	FolderResult toResult(Folder folder);
}
