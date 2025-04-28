package io.github.petty.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostQnaListResponse {
    private Long id;
    private String title;
    private String petType;      // 한글 라벨 (예: "강아지")
    private boolean isResolved;  // 해결 여부
    private String writer;       // 작성자 닉네임
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
}
