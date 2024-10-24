package kernel360.techpick.feature.infrastructure.tag;

import java.util.List;

import kernel360.techpick.core.model.tag.Tag;
import kernel360.techpick.feature.domain.tag.dto.TagCommand;

public interface TagAdaptor {

	Tag getTag(Long tagId);

	List<Tag> getTagList(Long userId);

	Tag saveTag(Long userId, TagCommand.Create command);

	Tag updateTag(TagCommand.Update command);

	List<Long> moveTag(Long userId, TagCommand.Move command);

	void deleteTag(Long userId, TagCommand.Delete command);

	boolean checkDuplicateTagName(Long userId, String name);
}
