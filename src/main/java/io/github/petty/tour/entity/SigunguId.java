package io.github.petty.tour.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable; // 직렬화 인터페이스 구현

/**
 * {@link Sigungu} 엔티티의 복합 기본 키를 표현하는 클래스입니다.
 * 복합 키 클래스는 {@link Serializable}을 구현해야 하며,
 * 기본 생성자와 {@link Object#equals(Object)}, {@link Object#hashCode()} 메서드를 구현해야 합니다.
 * Lombok 어노테이션을 사용하여 이를 간결하게 처리합니다.
 */
@Getter
@Setter
@NoArgsConstructor // 복합 키 클래스는 기본 생성자가 필요합니다.
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 (편의상 추가)
@EqualsAndHashCode // 필드 기반으로 equals() 및 hashCode() 메서드 자동 생성
public class SigunguId implements Serializable {

    /** Sigungu 엔티티의 areaCode 필드와 매핑됩니다. */
    private Integer areaCode;

    /** Sigungu 엔티티의 sigunguCode 필드와 매핑됩니다. */
    private Integer sigunguCode;
}