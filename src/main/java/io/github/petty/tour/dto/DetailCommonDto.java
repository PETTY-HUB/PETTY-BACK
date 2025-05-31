package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 관광 콘텐츠의 공통 상세 정보를 담는 DTO입니다.
 * 다양한 타입의 콘텐츠(관광지, 문화시설, 음식점 등)의 상세 정보를 표현합니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class DetailCommonDto {
    /** 콘텐츠 고유 ID */
    private Long contentId;
    /** 콘텐츠 타입 ID (예: 12=관광지, 39=음식점) */
    private Integer contentTypeId;
    /** 콘텐츠 제목 */
    private String title;
    /** 기본 주소 */
    private String addr1;
    /** 상세 주소 */
    private String addr2;
    /** 지역 코드 (숫자) */
    private Integer areaCode;
    /** 시군구 코드 (숫자) */
    private Integer sigunguCode;
    /** 콘텐츠 최초 등록 시각 (UTC) */
    private Instant createdTime;
    /** 콘텐츠 최종 수정 시각 (UTC) */
    private Instant modifiedTime;
    /** 대표 이미지 원본 URL */
    private String firstImage;
    /** 대표 이미지 썸네일 URL */
    private String firstImage2;
    /** X좌표 (경도, WGS84) */
    private BigDecimal mapX;
    /** Y좌표 (위도, WGS84) */
    private BigDecimal mapY;
    /** 지도 확대 레벨 */
    private Integer mlevel;
    /** 전화번호 */
    private String tel;
    /** 전화번호 제공처 명칭 */
    private String telName;
    /** 홈페이지 주소 */
    private String homepage;
    /** 콘텐츠 개요 정보 (HTML 포함 가능) */
    private String overview;

    /** 추가 이미지 정보 목록 */
    private List<DetailImageDto> images;
    /** 추가 텍스트 정보 목록 (안내 정보 등) */
    private List<DetailInfoDto> infos;
    /** 객실 정보 목록 (주로 숙박 타입 콘텐츠에 해당) */
    private List<RoomInfoDto> rooms;
    /** 소개 정보 (콘텐츠 타입별 특화 정보) */
    private DetailIntroDto introDetails;
    /** 반려동물 동반 관광 정보 */
    private DetailPetDto petTourInfo;
}