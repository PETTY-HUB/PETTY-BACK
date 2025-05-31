package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 숙박 시설의 객실 상세 정보를 나타내는 JPA 엔티티 클래스입니다.
 * 하나의 {@link Content} 엔티티(숙박 시설)는 여러 개의 RoomInfo를 가질 수 있습니다 (1:N 관계).
 * 주로 콘텐츠 타입 ID(contentTypeId)가 32(숙박)인 경우에 사용됩니다.
 */
@Getter
@Setter
@Entity
@Table(name = "room_info")
@NoArgsConstructor
@ToString(exclude = "content") // 양방향 연관관계에서 무한 루프 방지를 위해 'content' 필드 제외
public class RoomInfo {

    /** 객실 정보의 고유 ID (기본 키, 자동 생성) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false)
    private Long id;

    /**
     * 이 객실 정보가 속한 {@link Content} 엔티티 (N:1 관계).
     * 'contentid' 외래 키 컬럼을 통해 매핑됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contentid", nullable = false)
    private Content content;

    /** 객실 정보 번호 또는 고유 코드 (숙박 시설 내부 관리용) */
    @Column(name = "roominfono", length = 50)
    private String roomInfoNo;

    /** 객실명 또는 객실 타입명. (예: "디럭스룸", "스탠다드 트윈") */
    @Column(name = "roomtitle", length = 150)
    private String roomTitle;

    /**
     * 객실 크기 정보 (텍스트 설명).
     * 예: "23평형", "76㎡" (roomsize2 필드에 숫자+단위 조합으로도 저장될 수 있음)
     */
    @Column(name = "roomsize1", length = 20)
    private String roomSize1;

    /** 해당 타입의 총 객실 수. */
    @Column(name = "roomcount")
    private Integer roomCount;

    /** 객실 기준 숙박 인원. */
    @Column(name = "roombasecount")
    private Integer roomBaseCount;

    /** 객실 최대 숙박 가능 인원. */
    @Column(name = "roommaxcount")
    private Integer roomMaxCount;

    /**
     * 비수기 주중 객실 최저 요금.
     * {@link java.math.BigDecimal} 타입을 사용하여 정확한 금액을 표현합니다.
     */
    @Column(name = "roomoffseasonminfee1", precision = 10, scale = 0) // 예: 정수 금액 (소수점 없음 가정)
    private BigDecimal roomOffSeasonMinFee1;

    /** 비수기 주말 객실 최저 요금. */
    @Column(name = "roomoffseasonminfee2", precision = 10, scale = 0)
    private BigDecimal roomOffSeasonMinFee2;

    /** 성수기 주중 객실 최저 요금. */
    @Column(name = "roompeakseasonminfee1", precision = 10, scale = 0)
    private BigDecimal roomPeakSeasonMinFee1;

    /** 성수기 주말 객실 최저 요금. */
    @Column(name = "roompeakseasonminfee2", precision = 10, scale = 0)
    private BigDecimal roomPeakSeasonMinFee2;

    /**
     * 객실 소개 및 상세 설명.
     * 내용이 길 수 있으므로 @Lob 어노테이션을 사용합니다.
     */
    @Lob
    @Column(name = "roomintro")
    private String roomIntro;

    /**
     * 객실 내 욕실 시설 구비 여부.
     * (true: 있음, false: 없음, null: 정보 없음)
     */
    @Column(name = "roombathfacility")
    private Boolean roomBathFacility;

    /** 객실 내 욕조 구비 여부. */
    @Column(name = "roombath")
    private Boolean roomBath;

    /** 객실 내 홈씨어터 구비 여부. */
    @Column(name = "roomhometheater")
    private Boolean roomHomeTheater;

    /** 객실 내 에어컨 구비 여부. */
    @Column(name = "roomaircondition")
    private Boolean roomAirCondition;

    /** 객실 내 TV 구비 여부. */
    @Column(name = "roomtv")
    private Boolean roomTv;

    /** 객실 내 PC 구비 여부. */
    @Column(name = "roompc")
    private Boolean roomPc;

    /** 객실 내 케이블 방송 시청 가능 여부. */
    @Column(name = "roomcable")
    private Boolean roomCable;

    /** 객실 내 인터넷 사용 가능 여부. */
    @Column(name = "roominternet")
    private Boolean roomInternet;

    /** 객실 내 냉장고 구비 여부. */
    @Column(name = "roomrefrigerator")
    private Boolean roomRefrigerator;

    /** 객실 내 세면도구 제공 여부. */
    @Column(name = "roomtoiletries")
    private Boolean roomToiletries;

    /** 객실 내 소파 구비 여부. */
    @Column(name = "roomsofa")
    private Boolean roomSofa;

    /** 객실 내 취사 가능 여부. */
    @Column(name = "roomcook")
    private Boolean roomCook;

    /** 객실 내 테이블 구비 여부. */
    @Column(name = "roomtable")
    private Boolean roomTable;

    /** 객실 내 헤어드라이어 구비 여부. */
    @Column(name = "roomhairdryer")
    private Boolean roomHairdryer;

    /**
     * 객실 크기 (제곱미터 단위).
     * 예: "76", "102.5" (단위는 제외하고 숫자 또는 숫자 문자열)
     */
    @Column(name = "roomsize2", length = 30)
    private String roomSize2;

    /** 객실 대표 이미지 1 URL */
    @Column(name = "roomimg1", length = 2048)
    private String roomImg1;
    /** 객실 대표 이미지 1 설명 */
    @Column(name = "roomimg1alt")
    private String roomImg1Alt;

    /** 객실 대표 이미지 2 URL */
    @Column(name = "roomimg2", length = 2048)
    private String roomImg2;
    /** 객실 대표 이미지 2 설명 */
    @Column(name = "roomimg2alt")
    private String roomImg2Alt;

    /** 객실 대표 이미지 3 URL */
    @Column(name = "roomimg3", length = 2048)
    private String roomImg3;
    /** 객실 대표 이미지 3 설명 */
    @Column(name = "roomimg3alt")
    private String roomImg3Alt;

    /** 객실 대표 이미지 4 URL */
    @Column(name = "roomimg4", length = 2048)
    private String roomImg4;
    /** 객실 대표 이미지 4 설명 */
    @Column(name = "roomimg4alt")
    private String roomImg4Alt;

    /** 객실 대표 이미지 5 URL */
    @Column(name = "roomimg5", length = 2048)
    private String roomImg5;
    /** 객실 대표 이미지 5 설명 */
    @Column(name = "roomimg5alt")
    private String roomImg5Alt;
}