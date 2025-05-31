package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 관광 콘텐츠 정보를 나타내는 JPA 엔티티 클래스입니다.
 * 각 관광지, 문화시설, 숙박, 음식점 등의 상세 정보를 포함합니다.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
public class Content {

    /** 콘텐츠 고유 ID (기본 키) */
    @Id
    @Column(name = "contentid", nullable = false)
    private Long contentId;

    /** 콘텐츠 타입 ID [12=관광지, 14=문화시설, 15 = 축제, 28=레포츠, 32=숙박, 38=쇼핑, 39=음식점] */
    @Column(name = "contenttypeid", nullable = false)
    private Integer contentTypeId;

    /** 콘텐츠 제목 */
    @Column(name = "title", nullable = false)
    private String title;

    /** 주소 */
    @Column(name = "addr1")
    private String addr1;

    /** 상세 주소 (예: 123, 4층) */
    @Column(name = "addr2", length = 100)
    private String addr2;

    /** 지역 코드 */
    @Column(name = "areacode")
    private Integer areaCode;

    /** 시군구 코드 */
    @Column(name = "sigungucode")
    private Integer sigunguCode;

    /** 카테고리 대분류 (예: A01=자연, A02=인문 등) */
    @Column(name = "cat1", nullable = false, length = 10)
    private String cat1;

    /** 카테고리 중분류 */
    @Column(name = "cat2", length = 10)
    private String cat2;

    /** 카테고리 소분류 */
    @Column(name = "cat3", length = 10)
    private String cat3;

    /** 콘텐츠 최초 등록 시각 (UTC) */
    @Column(name = "createdtime", nullable = false)
    private Instant createdTime;

    /** 콘텐츠 최종 수정 시각 (UTC) */
    @Column(name = "modifiedtime", nullable = false)
    private Instant modifiedTime;

    /** 대표 이미지 원본 URL */
    @Column(name = "firstimage", length = 2048) // URL 길이를 고려하여 충분한 길이 할당
    private String firstImage;

    /** 대표 이미지 썸네일 URL */
    @Column(name = "firstimage2", length = 2048)
    private String firstImage2;

    /** 저작권 유형 코드 (예: Type1, Type3 등) */
    @Column(name = "cpyrhtdivcd", length = 20)
    private String cpyrhtDivCd;

    /** X좌표 (경도, WGS84) */
    @Column(name = "mapx", precision = 13, scale = 10) // 소수점 포함 전체 자릿수, 소수점 이하 자릿수
    private BigDecimal mapX;

    /** Y좌표 (위도, WGS84) */
    @Column(name = "mapy", precision = 13, scale = 10)
    private BigDecimal mapY;

    /** 지도 확대 레벨 (1~14) */
    @Column(name = "mlevel")
    private Integer mlevel;

    /** 전화번호 */
    @Column(name = "tel", length = 30)
    private String tel;

    /** 전화번호 제공처 명칭 */
    @Column(name = "telname", length = 50)
    private String telName;

    /** 홈페이지 주소 */
    @Lob
    @Column(name = "homepage")
    private String homepage;

    /** 콘텐츠 개요 정보 */
    @Lob
    @Column(name = "overview")
    private String overview;

    /** 우편번호 */
    @Column(name = "zipcode", length = 10)
    private String zipcode;

    /**
     * 공간 좌표 정보 (위치 기반 검색을 위함)
     * 데이터베이스의 POINT 타입과 매핑되며, SRID 4326 (WGS84) 좌표계를 사용합니다.
     */
    @Column(columnDefinition = "POINT SRID 4326")
    private Point location;

    // --- 연관 관계 매핑 ---

    /**
     * 추가 이미지 목록 (1:N 관계)
     * ContentImage 엔티티의 'content' 필드에 의해 매핑됩니다.
     * LAZY 로딩, 모든 변경사항 전파(cascade), 고아 객체 자동 삭제(orphanRemoval) 설정.
     */
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ContentImage> contentImages = new LinkedHashSet<>();

    /**
     * 추가 정보(안내) 목록 (1:N 관계)
     */
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ContentInfo> contentInfos = new LinkedHashSet<>();

    /**
     * 숙박(contentTypeId=32)일 경우, 객실 정보 목록 (1:N 관계)
     */
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RoomInfo> roomInfos = new LinkedHashSet<>();

    /**
     * 소개 정보 (1:1 관계)
     * ContentIntro 엔티티의 'content' 필드에 의해 매핑됩니다.
     */
    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContentIntro contentIntro;

    /**
     * 반려동물 동반 가능 정보 (1:1 관계)
     */
    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private PetTourInfo petTourInfo;
}