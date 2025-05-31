package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 관광 콘텐츠의 추가 이미지 정보를 나타내는 JPA 엔티티 클래스입니다.
 * 하나의 {@link Content} 엔티티는 여러 개의 ContentImage를 가질 수 있습니다 (1:N 관계).
 */
@Getter
@Setter
@Entity
@Table(name = "content_image")
@NoArgsConstructor
@ToString(exclude = "content") // 양방향 연관관계에서 무한 루프 방지를 위해 'content' 필드 제외
public class ContentImage {

    /** 이미지 정보의 고유 ID (기본 키, 자동 생성) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Long id;

    /**
     * 이 이미지가 속한 {@link Content} 엔티티 (N:1 관계).
     * 'contentid' 외래 키 컬럼을 통해 매핑됩니다.
     * 지연 로딩(FetchType.LAZY)을 사용하여, ContentImage 조회 시 연관된 Content를 즉시 로딩하지 않습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // N:1 관계, LAZY 로딩, 부모(Content)는 필수
    @JoinColumn(name = "contentid", nullable = false) // DB의 외래 키 컬럼명 및 null 비허용 설정
    private Content content;

    /** 이미지 설명 또는 파일명 */
    @Column(name = "imgname")
    private String imgName;

    /** 원본 이미지 URL */
    @Column(name = "originimgurl", length = 2048) // URL 길이를 고려한 충분한 길이
    private String originImgUrl;

    /**
     * 이미지 일련번호 또는 정렬 순서
     * 여러 이미지가 있을 경우 표시 순서 등을 지정하는 데 사용될 수 있습니다.
     */
    @Column(name = "serialnum", length = 20)
    private String serialNum;

    /** 작은 이미지(썸네일) URL */
    @Column(name = "smallimageurl", length = 2048)
    private String smallImageUrl;

    /** 이미지 저작권 구분 코드 (예: "Type1"(공공누리 제1유형), "Type3"(공공누리 제3유형) 등) */
    @Column(name = "cpyrhtdivcd", length = 20)
    private String cpyrhtDivCd; // Copyright Division Code
}