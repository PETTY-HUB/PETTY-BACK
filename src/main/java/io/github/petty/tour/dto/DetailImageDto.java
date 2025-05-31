package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관광 콘텐츠의 추가 이미지 상세 정보를 담는 DTO입니다.
 */
@NoArgsConstructor
@Setter
@Getter
public class DetailImageDto {
    /** 이미지 설명 또는 이름 */
    private String imgName;
    /** 원본 이미지 URL */
    private String originImgUrl;
    /** 작은 이미지(썸네일) URL */
    private String smallImageUrl;
}