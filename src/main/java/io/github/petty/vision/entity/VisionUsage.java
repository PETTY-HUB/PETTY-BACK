package io.github.petty.vision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vision_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "usage_date"}))
@Getter
@Setter
@NoArgsConstructor
public class VisionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "daily_limit", nullable = false)
    private Integer dailyLimit = 3; // 기본값 3회

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public VisionUsage(UUID userId, LocalDate usageDate, Integer dailyLimit) {
        this.userId = userId;
        this.usageDate = usageDate;
        this.dailyLimit = dailyLimit;
        this.usageCount = 0;
    }

    // 사용량 증가
    public void incrementUsage() {
        this.usageCount++;
        this.updatedAt = LocalDateTime.now();
    }

    // 사용 가능 여부 확인
    public boolean canUse() {
        return this.usageCount < this.dailyLimit;
    }

    // 남은 사용량
    public int getRemainingUsage() {
        return Math.max(0, this.dailyLimit - this.usageCount);
    }
}