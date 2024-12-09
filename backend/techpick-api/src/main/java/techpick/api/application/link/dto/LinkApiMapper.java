package techpick.api.application.link.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import techpick.api.domain.link.dto.LinkInfo;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface LinkApiMapper {

	LinkApiResponse toLinkResponse(LinkInfo linkInfo);
}
