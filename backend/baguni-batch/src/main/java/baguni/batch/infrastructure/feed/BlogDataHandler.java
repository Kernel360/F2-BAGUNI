package baguni.batch.infrastructure.feed;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import baguni.domain.model.rss.Blog;
import baguni.domain.infrastructure.rss.BlogRepository;

@Component
@RequiredArgsConstructor
public class BlogDataHandler {

	private final BlogRepository blogRepository;

	@Transactional(readOnly = true)
	public List<Blog> getAllBlogs() {
		return blogRepository.findAll();
	}
}
