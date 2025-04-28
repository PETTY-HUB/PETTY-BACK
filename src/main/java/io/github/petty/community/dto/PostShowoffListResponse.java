package io.github.petty.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostShowoffListResponse {
    private Long id;
    private String title;
    private String petType;     // 한글 라벨
    private String writer;      // 작성자 닉네임
    private String imageUrl;    // 대표 이미지 URL
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
}
