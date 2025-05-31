package io.github.petty.tour.repository;

import io.github.petty.tour.dto.TourSummaryProjection;
import io.github.petty.tour.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * {@link Content} 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리 인터페이스입니다.
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {


    /**
     * 주어진 contentId 목록에 해당하는 Content 및 연관된 모든 데이터(Cascade)를 삭제합니다.
     * @param contentIds 삭제할 contentId 리스트
     * @return 삭제된 레코드 수 (반환 타입은 void 또는 Long 등 조정 가능)
     */
    Long deleteAllByContentIdIn(List<Long> contentIds);

    // 테스트용 20개 추출
    List<Content> findTop20ByOrderByContentIdAsc();

    /**
     * 지역(areaCode), 시군구(sigunguCode), 콘텐츠 타입(contentTypeId)을 기준으로
     * 관광 정보 요약 목록을 페이징 처리하여 조회합니다.
     * 결과는 {@link TourSummaryProjection} 인터페이스를 통해 필요한 컬럼만 선택적으로 가져옵니다.
     *
     * @param areaCode 지역 코드 (필수)
     * @param sigunguCode 시군구 코드 (null 가능, null인 경우 해당 지역 전체 시군구 대상)
     * @param contentTypeId 콘텐츠 타입 ID (null 가능, null인 경우 모든 콘텐츠 타입 대상)
     * @param pageable 페이징 및 정렬 정보
     * @return {@link TourSummaryProjection}의 {@link Page} 객체
     */
    @Query( value = "SELECT c.contentId, c.title, c.addr1, c.contentTypeId, c.firstImage, c.cat1 FROM content c WHERE c.areaCode = :areaCode " +
                    "AND (:sigunguCode IS NULL OR c.sigunguCode = :sigunguCode) " +
                    "AND (:contentTypeId IS NULL OR c.contentTypeId = :contentTypeId)",
            nativeQuery = true)
    Page<TourSummaryProjection> findByAreaCriteria(@Param("areaCode") Integer areaCode,
                                                   @Param("sigunguCode") Integer sigunguCode,
                                                   @Param("contentTypeId") Integer contentTypeId,
                                                   Pageable pageable);

    /**
     * 지정된 좌표(mapX, mapY)와 반경(radius) 내에 있는 관광 정보 요약 목록을
     * 페이징 처리하여 조회합니다. 네이티브 SQL 쿼리를 사용하며, 거리 계산(ST_Distance_Sphere 함수 등)을 포함합니다.
     * 결과는 {@link TourSummaryProjection} 인터페이스를 통해 필요한 컬럼만 선택적으로 가져옵니다.
     * 참고: ST_Distance_Sphere 함수는 MySQL/MariaDB 기준이며, 다른 DBMS 사용 시 함수명이 다를 수 있습니다. (예: PostgreSQL의 ST_Distance)
     * SRID 4326 (WGS84) 좌표계를 가정합니다.
     *
     * @param mapX 기준점의 X좌표 (경도)
     * @param mapY 기준점의 Y좌표 (위도)
     * @param radius 검색 반경 (미터 단위)
     * @param contentTypeId 콘텐츠 타입 ID (null 가능, null인 경우 모든 콘텐츠 타입 대상)
     * @param pageable 페이징 및 정렬 정보
     * @return {@link TourSummaryProjection}의 {@link Page} 객체 (거리 정보 포함)
     */
    @Query( value = "SELECT c.contentid, c.title, c.addr1, c.contentTypeId, c.firstImage, c.cat1, " +
                    "ST_Distance_Sphere(c.location, ST_SRID(POINT(:mapX, :mapY), 4326)) as distance " +
                    "FROM content c " +
                    "WHERE ST_Distance_Sphere(c.location, ST_SRID(POINT(:mapX, :mapY), 4326)) <= :radius " +
                    "AND (:contentTypeId IS NULL OR c.contenttypeid = :contentTypeId) " +
                    "ORDER BY distance ASC",
            countQuery =
                    "SELECT count(*) FROM content c " +
                    "WHERE ST_Distance_Sphere(c.location, ST_SRID(POINT(:mapX, :mapY), 4326)) <= :radius " +
                    "AND (:contentTypeId IS NULL OR c.contenttypeid = :contentTypeId)",
            nativeQuery = true)
    Page<TourSummaryProjection> findByLocationNative(
                                                      @Param("mapX") BigDecimal mapX,
                                                      @Param("mapY") BigDecimal mapY,
                                                      @Param("radius") int radius,
                                                      @Param("contentTypeId") Integer contentTypeId,
                                                      Pageable pageable);


    /**
     * 콘텐츠 ID로 {@link Content} 엔티티를 조회하되, 연관된 모든 상세 정보
     * (이미지, 추가정보, 소개정보, 객실정보, 반려동물정보 등)를 함께 즉시 로딩(EAGER Fetch)합니다.
     * JPQL의 FETCH JOIN을 사용하여 N+1 문제를 방지하고 성능을 최적화합니다.
     *
     * @param contentId 조회할 콘텐츠의 ID
     * @return 연관 정보가 모두 포함된 {@link Content} 엔티티를 담은 {@link Optional} 객체. 해당 ID의 콘텐츠가 없으면 Optional.empty() 반환.
     */
    @Query("SELECT c FROM Content c " +
            "LEFT JOIN FETCH c.contentImages ci " +
            "LEFT JOIN FETCH c.contentInfos cinfo " +
            "LEFT JOIN FETCH c.roomInfos ri " +
            "LEFT JOIN FETCH c.contentIntro cintro " +
            "LEFT JOIN FETCH c.petTourInfo pti " +
            "WHERE c.contentId = :contentId")
    Optional<Content> findByIdFetchingAllDetails(@Param("contentId") Long contentId);

}
