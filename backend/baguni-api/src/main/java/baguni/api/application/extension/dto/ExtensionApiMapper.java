package baguni.api.application.extension.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.infrastructure.pick.dto.PickResult;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ExtensionApiMapper {

	PickCommand.CreateFromExtension toPickCreateCommand(Long userId, ExtensionApiRequest.Create request);

	ExtensionApiResponse.Pick toApiPickResponse(PickResult.Extension result);
}
