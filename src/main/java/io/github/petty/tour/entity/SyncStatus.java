package io.github.petty.tour.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 데이터 동기화 상태 정보를 나타내는 JPA 엔티티 클래스입니다.
 * 주로 외부 API로부터 데이터를 주기적으로 가져올 때 마지막 동기화 시점 등을 기록하는 데 사용될 수 있습니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor // JPA 프레임워크는 엔티티에 기본 생성자를 필요로 합니다.
@AllArgsConstructor
public class SyncStatus {

    /**
     * 상태 레코드의 고정된 기본 키 ID입니다.
     * 일반적으로 이 테이블에는 하나의 레코드만 존재하게 됩니다.
     */
    @Id
    private Long id;

    /**
     * 마지막으로 성공한 데이터 동기화의 대상 날짜입니다.
     * 이 날짜를 기준으로 다음 동기화 대상을 결정할 수 있습니다.
     */
    private LocalDate lastSyncDate;

    /**
     * 이 엔티티의 기본 ID로 사용될 상수 값입니다.
     */
    public static final Long DEFAULT_ID = 1L;
}