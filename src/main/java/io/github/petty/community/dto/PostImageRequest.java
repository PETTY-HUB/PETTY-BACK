package io.github.petty.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostImageRequest {
    private Long id; // 기존 이미지 수정/삭제할 때 필요 (신규는 null)
    private String imageUrl;
    private Integer ordering; // 0~4 중 위치 지정
    private Boolean isDeleted; // 삭제 요청인지 표시
}