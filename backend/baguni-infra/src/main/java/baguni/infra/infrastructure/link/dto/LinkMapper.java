package baguni.infra.infrastructure.link.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import baguni.infra.model.link.Link;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface LinkMapper {

	@Mapping(target = "title", source = "title", defaultValue = "")
	@Mapping(target = "description", source = "description", defaultValue = "")
	@Mapping(target = "imageUrl", source = "imageUrl", defaultValue = "")
	@Mapping(target = "isRss", ignore = true)
	@Mapping(target = "publishedAt", ignore = true)
	Link of(LinkInfo linkInfo);

	LinkInfo of(Link link);

	LinkResult toLinkResult(Link link);

	@Named("toBlogLinkInfoList")
	BlogLinkInfo toBlogLinkInfo(Link link);
}
