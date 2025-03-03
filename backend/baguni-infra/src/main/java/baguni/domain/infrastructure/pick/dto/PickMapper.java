package baguni.domain.infrastructure.pick.dto;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import baguni.domain.model.folder.Folder;
import baguni.domain.model.link.Link;
import baguni.domain.model.pick.Pick;
import baguni.domain.model.user.User;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PickMapper {

	@Mapping(source = "pick.link", target = "linkInfo")
	@Mapping(source = "pick.parentFolder.id", target = "parentFolderId")
	PickResult.Pick toPickResult(Pick pick);

	@Mapping(source = "pick.link.id", target = "linkId")
	@Mapping(source = "pick.link.url", target = "url")
	@Mapping(source = "pick.parentFolder.id", target = "parentFolderId")
	PickResult.Extension toExtensionResult(Pick pick);

	@Mapping(source = "folderId", target = "folderId")
	@Mapping(source = "pick", target = "pickList")
	PickResult.FolderPickList toPickResultList(Long folderId, List<PickResult.Pick> pick);

	@Mapping(source = "command.title", target = "title")
	@Mapping(source = "command.tagIdOrderedList", target = "tagIdOrderedList")
	@Mapping(source = "parentFolder", target = "parentFolder")
	@Mapping(source = "user", target = "user")
	Pick toEntity(PickCommand.Create command, User user, Folder parentFolder, Link link);

	@Mapping(source = "command.title", target = "title")
	@Mapping(source = "command.tagIdOrderedList", target = "tagIdOrderedList")
	@Mapping(source = "parentFolder", target = "parentFolder")
	@Mapping(source = "user", target = "user")
	Pick toEntity(PickCommand.CreateFromExtension command, User user, Folder parentFolder, Link link);

	@Mapping(source = "parentFolder", target = "parentFolder")
	@Mapping(source = "link", target = "link")
	Pick toEntity(String title, List<Long> tagIdOrderedList, User user, Folder parentFolder, Link link);
}
