package baguni.api.application.sharedFolder.dto;

import java.util.List;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import baguni.infra.infrastructure.sharedFolder.dto.SharedFolderResult;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SharedFolderApiMapper {

	// 공유 폴더 생성
	SharedFolderApiResponse.Create toCreateResponse(SharedFolderResult.Create result);

	// 내 공유된 폴더 목록 획득
	@Named("singlePartialReadMapping")
	@Mapping(source = "sourceFolder.createdAt", target = "sourceFolderCreatedAt")
	@Mapping(source = "sourceFolder.updatedAt", target = "sourceFolderUpdatedAt")
	@Mapping(source = "sourceFolder.name", target = "sourceFolderName")
	@Mapping(source = "sourceFolder.id", target = "sourceFolderId")
	SharedFolderApiResponse.ReadFolderPartial toReadResponse(SharedFolderResult.Read result);

	@IterableMapping(qualifiedByName = "singlePartialReadMapping")
	List<SharedFolderApiResponse.ReadFolderPartial> toReadResponseList(List<SharedFolderResult.Read> result);

	SharedFolderApiResponse.ReadFolderFull toReadFolderFullResponse(SharedFolderResult.SharedFolderInfo result);
}
