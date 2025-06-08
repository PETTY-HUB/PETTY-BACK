package io.github.petty.vision.repository;

import io.github.petty.vision.entity.VisionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisionUsageRepository extends JpaRepository<VisionUsage, UUID> {

    /**
     * 특정 사용자의 특정 날짜 사용량 조회
     */
    Optional<VisionUsage> findByUserIdAndUsageDate(UUID userId, LocalDate usageDate);

    /**
     * 오늘 사용량 조회 (편의 메서드)
     */
    @Query("SELECT v FROM VisionUsage v WHERE v.userId = :userId AND v.usageDate = CURRENT_DATE")
    Optional<VisionUsage> findTodayUsage(@Param("userId") UUID userId);

    /**
     * 특정 사용자의 총 사용량 조회
     */
    @Query("SELECT COALESCE(SUM(v.usageCount), 0) FROM VisionUsage v WHERE v.userId = :userId")
    Long getTotalUsageByUser(@Param("userId") UUID userId);

    /**
     * 오래된 사용량 기록 삭제 (30일 이전)
     */
    @Modifying
    @Query("DELETE FROM VisionUsage v WHERE v.usageDate < :cutoffDate")
    void deleteOldUsageRecords(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * 특정 날짜 이후 사용량 기록 조회
     */
    @Query("SELECT v FROM VisionUsage v WHERE v.userId = :userId AND v.usageDate >= :fromDate ORDER BY v.usageDate DESC")
    java.util.List<VisionUsage> findUsageHistory(@Param("userId") UUID userId, @Param("fromDate") LocalDate fromDate);
}