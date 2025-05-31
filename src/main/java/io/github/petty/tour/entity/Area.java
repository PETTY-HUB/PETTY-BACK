package io.github.petty.tour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor; // 기본 생성자 추가 (JPA 프레임워크 요구사항 고려)
import lombok.Setter;

/**
 * 지역(시/도) 정보를 나타내는 JPA 엔티티 클래스입니다.
 */
@Entity
@Table(name = "area")
@Getter
@Setter
@NoArgsConstructor
public class Area {

    /** 지역 코드 (기본 키) (예: 1 (서울), 2 (인천), 31 (경기도) 등) */
    @Id
    @Column(name = "areacode")
    private Integer areaCode;

    /** 지역 이름 (예: "서울", "인천", "경기도" 등) */
    @Column(name = "areaname", nullable = false, length = 20)
    private String areaName;
}