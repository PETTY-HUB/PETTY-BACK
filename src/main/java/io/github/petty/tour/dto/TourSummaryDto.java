package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관광 정보 요약 목록에 사용될 데이터 전송 객체(DTO)입니다.
 * 주로 검색 결과 목록 표시에 필요한 최소한의 정보를 담습니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class TourSummaryDto {
    /** 콘텐츠 고유 ID */
    private Long contentId;

    /** 콘텐츠 제목 */
    private String title;

    /** 주소 (기본 주소) */
    private String addr1;

    /** 콘텐츠 타입 ID [12=관광지, 14=문화시설, 15 = 축제, 28=레포츠, 32=숙박, 38=쇼핑, 39=음식점] */
    private Integer contentTypeId;

    /** 대표 이미지 URL (썸네일) */
    private String firstImage;

    /** 대분류 카테고리 코드 */
    private String cat1;

    /** 위치 기반 검색 시 현재 위치로부터의 거리 (미터 단위) */
    private Double distanceMeters;
}