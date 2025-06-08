package io.github.petty.vision.service;

import io.github.petty.vision.entity.VisionUsage;
import io.github.petty.vision.repository.VisionUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(transactionManager = "supabaseTransactionManager")
public class VisionUsageService {

    private final VisionUsageRepository visionUsageRepository;

    @Value("${vision.daily-limit:3}")
    private int defaultDailyLimit;

    /**
     * 오늘 사용량 조회 (없으면 새로 생성)
     */
    public VisionUsage getTodayUsage(UUID userId) {
        LocalDate today = LocalDate.now();
        return visionUsageRepository.findByUserIdAndUsageDate(userId, today)
                .orElseGet(() -> createNewUsageRecord(userId, today));
    }

    /**
     * 사용 가능 여부 확인
     */
    public boolean canUseToday(UUID userId) {
        VisionUsage usage = getTodayUsage(userId);
        return usage.canUse();
    }

    /**
     * 사용량 증가 (분석 완료 시 호출)
     */
    public VisionUsage incrementUsage(UUID userId) {
        VisionUsage usage = getTodayUsage(userId);

        if (!usage.canUse()) {
            throw new IllegalStateException(
                    String.format("일일 사용 한도(%d회)를 초과했습니다. 현재 사용량: %d회",
                            usage.getDailyLimit(), usage.getUsageCount())
            );
        }

        usage.incrementUsage();
        VisionUsage saved = visionUsageRepository.save(usage);

        log.info("Vision 사용량 증가: 사용자={}, 날짜={}, 사용량={}/{}",
                userId, usage.getUsageDate(), saved.getUsageCount(), saved.getDailyLimit());

        return saved;
    }

    /**
     * 남은 사용량 조회
     */
    public int getRemainingUsage(UUID userId) {
        VisionUsage usage = getTodayUsage(userId);
        return usage.getRemainingUsage();
    }

    /**
     * 사용량 기록 생성
     */
    private VisionUsage createNewUsageRecord(UUID userId, LocalDate date) {
        VisionUsage newUsage = new VisionUsage(userId, date, defaultDailyLimit);
        VisionUsage saved = visionUsageRepository.save(newUsage);

        log.debug("새로운 Vision 사용량 기록 생성: 사용자={}, 날짜={}, 한도={}",
                userId, date, defaultDailyLimit);

        return saved;
    }

    /**
     * 사용자 사용량 히스토리 조회 (최근 7일)
     */
    public List<VisionUsage> getRecentUsageHistory(UUID userId) {
        LocalDate fromDate = LocalDate.now().minusDays(7);
        return visionUsageRepository.findUsageHistory(userId, fromDate);
    }

    /**
     * 총 사용량 조회
     */
    public Long getTotalUsage(UUID userId) {
        return visionUsageRepository.getTotalUsageByUser(userId);
    }

    /**
     * 오래된 기록 정리 (매일 새벽 2시)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldRecords() {
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        visionUsageRepository.deleteOldUsageRecords(cutoffDate);
        log.info("Vision 사용량 기록 정리 완료: {} 이전 데이터 삭제", cutoffDate);
    }

    /**
     * 관리자용: 사용자 일일 한도 조정
     */
    public VisionUsage updateDailyLimit(UUID userId, int newLimit) {
        if (newLimit < 0) {
            throw new IllegalArgumentException("일일 한도는 0 이상이어야 합니다.");
        }

        VisionUsage usage = getTodayUsage(userId);
        usage.setDailyLimit(newLimit);

        log.info("Vision 일일 한도 변경: 사용자={}, 기존한도={}, 새한도={}",
                userId, usage.getDailyLimit(), newLimit);

        return visionUsageRepository.save(usage);
    }
}