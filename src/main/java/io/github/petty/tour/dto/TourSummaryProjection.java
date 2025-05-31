package io.github.petty.tour.dto;

/**
 * 데이터베이스 네이티브 쿼리(예: 위치 기반 검색)의 결과를 매핑하기 위한 프로젝션 인터페이스입니다.
 * 각 getter 메서드는 SELECT 절의 컬럼명 또는 별칭(alias)과 일치해야 합니다.
 * 이를 통해 전체 엔티티를 로드하지 않고 필요한 데이터만 효율적으로 조회할 수 있습니다.
 */
public interface TourSummaryProjection {
    /** 콘텐츠 고유 ID */
    Long getContentId();
    /** 콘텐츠 제목 */
    String getTitle();
    /** 기본 주소 */
    String getAddr1();
    /** 콘텐츠 타입 ID */
    Integer getContentTypeId();
    /** 대표 이미지 URL (네이티브 쿼리에서 'firstImage' 별칭 사용 가능) */
    String getFirstImage();
    /** 대분류 카테고리 코드 */
    String getCat1();
    /** 계산된 거리 값 (미터 단위, 위치 기반 검색 시 사용) */
    Double getDistance();
}