package io.github.petty.tour.service;

import io.github.petty.tour.dto.CodeNameDto;
import io.github.petty.tour.dto.DetailCommonDto;
import io.github.petty.tour.dto.TourSummaryDto;
import io.github.petty.tour.exception.ResourceNotFoundException; // 사용자 정의 예외
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 관광 정보 조회 및 검색 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 */
public interface TourService {

    /**
     * 지역(시/도) 코드 목록 또는 특정 지역의 시/군/구 코드 목록을 조회합니다.
     * 이 메서드는 페이징을 사용하지 않고 모든 관련 코드를 반환합니다.
     *
     * @param areaCode 조회할 상위 지역(시/도) 코드.
     * null이거나 제공되지 않으면 전체 시/도 목록을 반환합니다.
     * 값이 제공되면 해당 시/도의 시/군/구 목록을 반환합니다.
     * @return 지역 또는 시/군/구의 코드와 이름 정보를 담은 {@link CodeNameDto} 리스트.
     * 결과가 없을 경우 빈 리스트를 반환할 수 있습니다.
     */
    List<CodeNameDto> getAreas(Integer areaCode);

    /**
     * 특정 관광 콘텐츠 ID에 해당하는 상세 정보를 조회합니다.
     *
     * @param contentId 조회할 콘텐츠의 고유 ID.
     * @return 해당 콘텐츠의 상세 정보를 담은 {@link DetailCommonDto} 객체.
     * @throws ResourceNotFoundException 해당 ID의 콘텐츠를 찾을 수 없는 경우 발생합니다.
     */
    DetailCommonDto getContentDetailById(Long contentId);

    /**
     * 지역(시/도, 시/군/구)을 기준으로 관광 정보를 검색합니다.
     *
     * @param areaCode      검색할 지역(시/도) 코드 (필수).
     * @param sigunguCode   검색할 시/군/구 코드 (선택 사항, null 가능).
     * @param contentTypeId 검색할 콘텐츠 타입 ID (선택 사항, null 가능).
     * @param pageable      페이징 및 정렬 정보.
     * @return 검색 조건에 맞는 관광 정보 요약({@link TourSummaryDto})의 {@link Page} 객체.
     * 결과가 없을 경우 빈 페이지를 반환할 수 있습니다.
     */
    Page<TourSummaryDto> searchByArea(Integer areaCode, Integer sigunguCode, Integer contentTypeId, Pageable pageable);

    /**
     * 지리적 위치(좌표 및 반경)를 기준으로 관광 정보를 검색합니다.
     *
     * @param mapX          검색 중심의 X좌표 (경도, 필수).
     * @param mapY          검색 중심의 Y좌표 (위도, 필수).
     * @param radius        검색 반경 (미터 단위, 필수).
     * @param contentTypeId 검색할 콘텐츠 타입 ID (선택 사항, null 가능).
     * @param pageable      페이징 및 정렬 정보 (결과는 일반적으로 거리순으로 정렬될 수 있음).
     * @return 검색 조건에 맞는 관광 정보 요약({@link TourSummaryDto})의 {@link Page} 객체.
     * 결과에 거리 정보가 포함될 수 있으며, 결과가 없을 경우 빈 페이지를 반환할 수 있습니다.
     */
    Page<TourSummaryDto> searchByLocation(BigDecimal mapX, BigDecimal mapY, Integer radius, Integer contentTypeId, Pageable pageable);
}