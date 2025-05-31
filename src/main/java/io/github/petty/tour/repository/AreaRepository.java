package io.github.petty.tour.repository;

import io.github.petty.tour.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link Area} 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리 인터페이스입니다.
 * {@link JpaRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete) 기능을 자동으로 제공받습니다.
 */
@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {
}