package io.github.petty.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostReviewListResponse {
    private Long id;
    private String title;
    private String petName;
    private String petType;     // enum의 label 값
    private String region;
    private String writer;      // 작성자 닉네임
    private String imageUrl;    // 대표 이미지 URL
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
}
