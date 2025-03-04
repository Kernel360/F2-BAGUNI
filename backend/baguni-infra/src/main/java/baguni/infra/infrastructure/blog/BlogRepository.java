package baguni.infra.infrastructure.blog;

import org.springframework.data.jpa.repository.JpaRepository;

import baguni.infra.model.blog.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
