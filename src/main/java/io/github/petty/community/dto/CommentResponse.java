package io.github.petty.community.dto;

import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String writer;
    private String content;
    private LocalDateTime createdAt;
}
