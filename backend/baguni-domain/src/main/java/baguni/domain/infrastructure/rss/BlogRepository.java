package baguni.domain.infrastructure.rss;

import org.springframework.data.jpa.repository.JpaRepository;

import baguni.domain.model.rss.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
