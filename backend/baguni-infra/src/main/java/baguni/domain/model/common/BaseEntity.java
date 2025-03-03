package baguni.domain.model.common;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

	// 생성 시간 자동 부여
	@CreatedDate
	@Column(name = "created_at", updatable = false, nullable = false)
	protected LocalDateTime createdAt;

	// 수정 시간 자동 부여
	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	protected LocalDateTime updatedAt;
}
