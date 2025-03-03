package baguni.api.service.pick.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.service.ranking.service.RankingService;
import baguni.infra.infrastructure.folder.FolderDataHandler;
import baguni.infra.infrastructure.link.LinkDataHandler;
import baguni.infra.infrastructure.pick.PickDataHandler;
import baguni.infra.infrastructure.pick.dto.PickMapper;
import baguni.infra.infrastructure.tag.TagDataHandler;

@DisplayName("픽 서비스 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
public class PickServiceUnitTest {

	@Mock
	private TagDataHandler tagDataHandler;

	@Mock
	private PickDataHandler pickDataHandler;

	@Mock
	private PickMapper pickMapper;

	@Mock
	private FolderDataHandler folderDataHandler;

	@Mock
	private RankingService rankingService;

	@Mock
	private LinkDataHandler linkDataHandler;

	@InjectMocks
	private PickService pickService;

}
