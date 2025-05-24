package io.github.petty.llm.dto;

import java.util.List;

// 추천 응답 반환
public record RecommendResponseDTO(
        List<PlaceRecommend> recommend
) {
    public record PlaceRecommend(
            String contentId,
            String title,
            String addr,
            String description,
            String imageUrl,
            String acmpyTypeCd,
            String acmpyPsblCpam,
            String acmpyNeedMtr,
            String recommendReason
    ) {
    }
}