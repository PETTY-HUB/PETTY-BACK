package io.github.petty.tour.repository;

import io.github.petty.tour.entity.ContentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * {@link ContentImage} 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리 인터페이스입니다.
 */
@Repository
public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {

     /**
      * 특정 콘텐츠 ID(contentId)에 속하는 모든 이미지 목록을 조회합니다.
      * 메서드 이름 규칙(쿼리 메서드)에 따라 Spring Data JPA가 자동으로 쿼리를 생성합니다.
      * 'Content' 엔티티 내의 'contentId' 필드를 기준으로 검색합니다.
      *
      * @param contentId 조회할 부모 콘텐츠의 ID
      * @return 해당 콘텐츠 ID에 연관된 {@link ContentImage} 엔티티 목록
      */
     List<ContentImage> findByContent_ContentId(Long contentId);
}