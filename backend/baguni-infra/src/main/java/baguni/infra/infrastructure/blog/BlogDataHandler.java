package baguni.infra.infrastructure.blog;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import baguni.infra.model.blog.Blog;

@Component
@RequiredArgsConstructor
public class BlogDataHandler {

	private final BlogRepository blogRepository;

	@Transactional(readOnly = true)
	public List<Blog> getAllBlogs() {
		return blogRepository.findAll();
	}
}
