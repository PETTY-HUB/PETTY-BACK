package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 관광 콘텐츠의 반려동물 동반 관련 상세 정보를 나타내는 JPA 엔티티 클래스입니다.
 * {@link Content} 엔티티와 1:1 관계를 가집니다.
 */
@Getter
@Setter
@Entity
@Table(name = "pet_tour_info")
@NoArgsConstructor
@ToString(exclude = "content") // 양방향 연관관계에서 무한 루프 방지를 위해 'content' 필드 제외
public class PetTourInfo {

    /**
     * 콘텐츠 ID (기본 키, {@link Content#contentId}와 동일한 값을 가짐).
     * 이 필드는 {@link Content} 엔티티의 ID를 공유하며, 동시에 외래 키 역할도 합니다.
     */
    @Id
    @Column(name = "contentid", nullable = false)
    private Long contentId;

    /**
     * 이 반려동물 정보에 해당하는 {@link Content} 엔티티 (1:1 관계의 주인이 아님).
     * {@link MapsId}를 사용하여 'contentId' 필드가 Content 엔티티의 ID 값을 직접 사용하도록 매핑합니다.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false) // 1:1 관계, LAZY 로딩, 부모(Content)는 필수
    @MapsId // 'contentId' 필드를 Content 엔티티의 ID와 매핑
    @JoinColumn(name = "contentid") // 외래 키 컬럼 지정
    private Content content;

    /**
     * 반려동물 동반 시 발생 가능한 사고 및 안전 관련 주의사항.
     * 내용이 길 수 있으므로 @Lob 어노테이션을 사용하여 CLOB 또는 TEXT 타입으로 매핑합니다.
     */
    @Lob
    @Column(name = "rela_acdnt_risk_mtr")
    private String relaAcdntRiskMtr;

    /**
     * 동반 가능 반려동물 유형 또는 조건 코드.
     * 예: "가능", "소형견만 가능", "케이지 필수" 등 구체적인 정책을 나타내는 문자열 또는 코드.
     */
    @Column(name = "acmpy_type_cd", length = 30)
    private String acmpyTypeCd;

    /** 반려동물 관련 보유 시설 정보 (예: 전용 놀이터, 음수대 등) */
    @Lob
    @Column(name = "rela_poses_fclty")
    private String relaPosesFclty;

    /** 기본으로 비치되어 있는 반려동물 관련 용품 목록. (예: 배변패드, 물그릇 등) */
    @Lob
    @Column(name = "rela_frnsh_prdlst")
    private String relaFrnshPrdlst;

    /** 기타 반려동물 동반 관련 특이사항 또는 추가 정보. */
    @Lob
    @Column(name = "etc_acmpy_info")
    private String etcAcmpyInfo;

    /** 현장에서 구매 가능한 반려동물 관련 용품 목록. */
    @Lob
    @Column(name = "rela_purc_prdlst")
    private String relaPurcPrdlst;

    /** 동반 가능한 반려동물의 크기 또는 무게 제한 등에 대한 설명. */
    @Lob
    @Column(name = "acmpy_psbl_cpam")
    private String acmpyPsblCpam;

    /** 현장에서 대여 가능한 반려동물 관련 용품 목록. */
    @Lob
    @Column(name = "rela_rntl_prdlst")
    private String relaRntlPrdlst;

    /** 반려동물 동반 시 보호자가 반드시 준비해야 하는 물품이나 사항. (예: 목줄, 입마개, 배변봉투 등) */
    @Lob
    @Column(name = "acmpy_need_mtr")
    private String acmpyNeedMtr;
}