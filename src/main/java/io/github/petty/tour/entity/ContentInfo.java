package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 관광 콘텐츠의 추가적인 안내 정보(텍스트 기반)를 나타내는 JPA 엔티티 클래스입니다.
 * 예: 이용 시간, 휴무일, 입장료, 시설 안내 등.
 * 하나의 {@link Content} 엔티티는 여러 개의 ContentInfo를 가질 수 있습니다 (1:N 관계).
 */
@Getter
@Setter
@Entity
@Table(name = "content_info")
@NoArgsConstructor
@ToString(exclude = "content") // 양방향 연관관계에서 무한 루프 방지를 위해 'content' 필드 제외
public class ContentInfo {

    /** 추가 정보 항목의 고유 ID (기본 키, 자동 생성) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "info_id", nullable = false)
    private Long id;

    /**
     * 이 정보가 속한 {@link Content} 엔티티 (N:1 관계).
     * 'contentid' 외래 키 컬럼을 통해 매핑됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contentid", nullable = false)
    private Content content;

    /**
     * 정보 필드 구분자 또는 그룹 코드
     */
    @Column(name = "fldgubun", length = 10)
    private String fldGubun;

    /** 정보 항목의 이름 (제목) (예: "이용시간", "입장료 안내", "주차시설") */
    @Column(name = "infoname", length = 100)
    private String infoName;

    /** 정보 항목의 상세 내용 (텍스트 또는 HTML) */
    @Lob
    @Column(name = "infotext")
    private String infoText;

    /** 정보 항목의 일련번호 또는 정렬 순서 */
    @Column(name = "serialnum", length = 10)
    private String serialNum;
}