package baguni.api.application.pick.dto;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.infrastructure.pick.dto.PickResult;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PickApiMapper {

	PickCommand.ReadList toReadListCommand(Long userId, List<Long> folderIdList);

	PickCommand.ReadPagination toReadPaginationCommand(Long userId, Long folderId, Long cursor, Integer size);

	PickCommand.SearchPagination toSearchPaginationCommand(Long userId, List<Long> folderIdList,
		List<String> searchTokenList, List<Long> tagIdList, Long cursor, Integer size);

	PickCommand.Create toCreateCommand(Long userId, PickApiRequest.Create request);

	PickCommand.Extension toExtensionCommand(Long userId, String title, String url);

	PickCommand.Update toUpdateCommand(Long userId, PickApiRequest.UpdateFromExtension request);

	@Mapping(target = "parentFolderId", ignore = true)
	PickCommand.Update toUpdateCommand(Long userId, PickApiRequest.Update request);

	PickCommand.Move toMoveCommand(Long userId, PickApiRequest.Move request);

	PickCommand.Delete toDeleteCommand(Long userId, PickApiRequest.Delete request);

	PickApiResponse.Pick toApiResponse(PickResult.Pick pickResult);

	PickApiResponse.Extension toApiExtensionResponse(PickResult.Extension pickResult);

	PickApiResponse.Exist toApiExistResponse(Boolean exist);

	PickApiResponse.FolderPickList toApiFolderPickList(PickResult.FolderPickList folderPickLists);

	default PickSliceResponse<PickApiResponse.Pick> toSliceApiResponse(Slice<PickResult.Pick> source) {
		List<PickApiResponse.Pick> convertedContent = source.getContent().stream()
															.map(this::toApiResponse)
															.toList();
		SliceImpl<PickApiResponse.Pick> pickSlice = new SliceImpl<>(convertedContent, source.getPageable(),
			source.hasNext());

		return new PickSliceResponse<>(pickSlice);
	}
}
