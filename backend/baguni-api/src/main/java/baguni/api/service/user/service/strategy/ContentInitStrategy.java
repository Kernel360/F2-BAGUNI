package baguni.api.service.user.service.strategy;

import baguni.infra.infrastructure.user.dto.UserInfo;

public interface ContentInitStrategy {
	void initContent(UserInfo info, Long folderId);
}
