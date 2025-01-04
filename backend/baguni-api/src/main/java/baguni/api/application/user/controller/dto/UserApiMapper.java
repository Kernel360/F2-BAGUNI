package baguni.api.application.user.controller.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import baguni.api.service.user.dto.UserInfo;

@Mapper(
	componentModel = "spring",
	injectionStrategy = InjectionStrategy.CONSTRUCTOR,
	unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserApiMapper {

	@Mapping(expression = "java(userInfo.idToken().toString())", target = "idToken")
	UserInfoApiResponse from(UserInfo userInfo);
}
