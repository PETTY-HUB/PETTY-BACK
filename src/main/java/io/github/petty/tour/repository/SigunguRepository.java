package io.github.petty.tour.repository;

import io.github.petty.tour.entity.Sigungu;
import io.github.petty.tour.entity.SigunguId; // 복합 키 클래스
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * {@link Sigungu} 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리 인터페이스입니다.
 * 복합 키 {@link SigunguId}를 사용합니다.
 */
@Repository
public interface SigunguRepository extends JpaRepository<Sigungu, SigunguId> {

    /**
     * 특정 지역 코드(areaCode)에 속하는 모든 시군구 목록을 조회합니다.
     * 메서드 이름 규칙에 따라 Spring Data JPA가 자동으로 쿼리를 생성합니다.
     *
     * @param areaCode 조회할 상위 지역의 코드
     * @return 해당 지역 코드에 연관된 {@link Sigungu} 엔티티 목록
     */
    List<Sigungu> findByAreaCode(Integer areaCode);
}