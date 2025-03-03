package baguni.api.application.user.controller.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import baguni.infra.infrastructure.user.dto.UserInfo;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserApiMapper {

	@Mapping(expression = "java(userInfo.idToken().value())", target = "idToken")
	UserInfoApiResponse toApiResponse(UserInfo userInfo);
}
