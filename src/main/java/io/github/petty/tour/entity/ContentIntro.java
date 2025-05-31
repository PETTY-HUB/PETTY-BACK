package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * 관광 콘텐츠의 타입별 소개 정보를 나타내는 JPA 엔티티 클래스입니다.
 * 콘텐츠 타입에 따라 다양한 정보를 유연하게 저장하기 위해 JSON 타입을 활용할 수 있습니다.
 * {@link Content} 엔티티와 1:1 관계를 가집니다.
 */
@Getter
@Setter
@Entity
@Table(name = "content_intro")
@NoArgsConstructor
@ToString(exclude = "content") // 양방향 연관관계에서 무한 루프 방지를 위해 'content' 필드 제외
public class ContentIntro {

    /**
     * 콘텐츠 ID (기본 키, {@link Content#contentId}와 동일한 값을 가짐)
     * 이 필드는 {@link Content} 엔티티의 ID를 공유하며, 동시에 외래 키 역할도 합니다.
     */
    @Id
    @Column(name = "contentid", nullable = false)
    private Long contentId;

    /**
     * 이 소개 정보에 해당하는 {@link Content} 엔티티 (1:1 관계의 주인이 아님).
     * {@link MapsId}를 사용하여 'contentId' 필드가 Content 엔티티의 ID 값을 직접 사용하도록 매핑합니다.
     * 즉, ContentIntro의 ID는 연관된 Content의 ID와 항상 동일합니다.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId // 'contentId' 필드를 Content 엔티티의 ID와 매핑
    @JoinColumn(name = "contentid") // 외래 키 컬럼 지정 (Content 테이블의 PK를 참조)
    private Content content;

    /**
     * 콘텐츠 타입별 상세 소개 정보.
     * 다양한 정보를 키-값 형태로 저장하기 위해 Map<String, Object> 타입을 사용하며,
     * 데이터베이스에는 JSON 타입으로 저장됩니다.
     * 예: {"eventstartdate": "20250101", "eventenddate": "20250103", "playtime": "상시"}
     * {@link JdbcTypeCode}와 {@link SqlTypes#JSON}을 사용하여 Hibernate가 이 필드를 JSON으로 처리하도록 지정합니다.
     */
    @Column(name = "intro_details")
    @JdbcTypeCode(SqlTypes.JSON) // 이 필드를 DB의 JSON 타입으로 매핑
    private Map<String, Object> introDetails;
}