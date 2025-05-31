package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor; // 기본 생성자 추가
import lombok.Setter;

/**
 * 시군구 정보를 나타내는 JPA 엔티티 클래스입니다.
 * {@link SigunguId}를 복합 기본 키로 사용합니다.
 */
@Entity
@Table(name = "sigungu")
@IdClass(SigunguId.class) // 복합 키 클래스 지정
@Getter
@Setter
@NoArgsConstructor
public class Sigungu {

    /** 지역 코드 (복합 키의 일부)
     * {@link Area#areaCode}와 연결됩니다.
     */
    @Id // 이 필드가 복합 기본 키의 일부
    @Column(name = "areacode")
    private Integer areaCode;

    /** 시군구 코드 (복합 키의 일부)
     * areaCode와 함께 유일성을 가짐
     */
    @Id // 이 필드가 복합 기본 키의 일부
    @Column(name = "sigungucode")
    private Integer sigunguCode;

    /**
     * 시군구 이름
     * 예: "강남구", "중구", "수원시" 등
     */
    @Column(name = "sigunguname", nullable = false, length = 20) // null 비허용, 최대 길이 20
    private String sigunguName;
}