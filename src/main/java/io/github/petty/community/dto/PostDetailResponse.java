package io.github.petty.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {

    private Long id;                      // 게시글 ID
    private String title;                 // 제목
    private String content;               // 내용
    private String writer;                // 작성자 닉네임
    private String postType;             // 게시물 유형
    private String petType;              // 반려동물 종류 (한글 라벨)
    private String petName;              // 반려동물 이름 (REVIEW에서만 사용)
    private String region;               // 지역 정보 (REVIEW에서만 사용)
    private Boolean isResolved;          // QnA 게시판 여부 (QNA 전용)
    private int likeCount;               // 좋아요 수
    private int commentCount;            // 댓글 수
    private List<PostImageResponse> images;
    private LocalDateTime createdAt;     // 생성일자
}
