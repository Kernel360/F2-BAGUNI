package baguni.domain.infrastructure.blog;

import org.springframework.data.jpa.repository.JpaRepository;

import baguni.domain.model.blog.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
