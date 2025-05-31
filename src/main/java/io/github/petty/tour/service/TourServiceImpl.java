package io.github.petty.tour.service;

import io.github.petty.tour.dto.CodeNameDto;
import io.github.petty.tour.dto.DetailCommonDto;
import io.github.petty.tour.dto.TourSummaryDto;
import io.github.petty.tour.dto.TourSummaryProjection;
import io.github.petty.tour.entity.Content;
import io.github.petty.tour.exception.ResourceNotFoundException;
import io.github.petty.tour.mapper.ContentMapper;
import io.github.petty.tour.repository.AreaRepository;
import io.github.petty.tour.repository.ContentRepository;
import io.github.petty.tour.repository.SigunguRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 관광 정보 조회 및 검색 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 모든 public 메서드는 기본적으로 읽기 전용 트랜잭션(@Transactional(readOnly = true))으로 동작합니다.
 * 데이터 변경이 필요한 메서드에는 별도로 @Transactional을 선언해야 합니다.
 */
@Slf4j
@Service
@Transactional(readOnly = true) // 클래스 레벨에서 읽기 전용 트랜잭션 기본값으로 설정
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class TourServiceImpl implements TourService {

    private final AreaRepository areaRepository;
    private final SigunguRepository sigunguRepository;
    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper; // MapStruct 기반 DTO-Entity 매퍼

    /**
     * 지역(시/도) 코드 목록 또는 특정 지역의 시/군/구 코드 목록을 조회합니다.
     * 이 메서드는 페이징을 사용하지 않고 모든 관련 코드를 반환합니다.
     *
     * @param areaCode 조회할 상위 지역(시/도) 코드.
     * null이거나 제공되지 않으면 전체 시/도 목록을 반환합니다.
     * 값이 제공되면 해당 시/도의 시/군/구 목록을 반환합니다.
     * @return 지역 또는 시/군/구의 코드와 이름 정보를 담은 {@link CodeNameDto} 리스트
     */
    @Override
    public List<CodeNameDto> getAreas(Integer areaCode) {
        log.debug("서비스: 지역/시군구 코드 목록 조회 시작 - 요청 areaCode: {}", areaCode == null ? "전체 시/도" : areaCode);
        List<CodeNameDto> codes;
        if (areaCode == null) {
            log.info("서비스: 전체 시/도 코드 목록을 조회합니다.");
            codes = contentMapper.areasToCodeNameDtos(areaRepository.findAll());
        } else {
            log.info("서비스: areaCode {}에 해당하는 시/군/구 코드 목록을 조회합니다.", areaCode);
            codes = contentMapper.sigungusToCodeNameDtos(sigunguRepository.findByAreaCode(areaCode));
        }
        log.info("서비스: {}개의 코드를 반환합니다. (요청 areaCode: {})", codes.size(), areaCode == null ? "전체 시/도" : areaCode);
        return codes;
    }

    /**
     * 특정 관광 콘텐츠 ID에 해당하는 상세 정보를 조회합니다.
     *
     * @param contentId 조회할 콘텐츠의 고유 ID
     * @return 해당 콘텐츠의 상세 정보를 담은 {@link DetailCommonDto} 객체
     * @throws ResourceNotFoundException 해당 ID의 콘텐츠를 찾을 수 없는 경우 발생합니다.
     */
    @Override
    public DetailCommonDto getContentDetailById(Long contentId) {
        log.info("서비스: 콘텐츠 상세 정보 조회 시작 - ID: {}", contentId);

        // ContentRepository에서 모든 연관 상세 정보를 즉시 로딩(fetch)하는 메서드를 호출합니다.
        // orElseThrow를 사용하여 Optional이 비어있을 경우(즉, ID에 해당하는 콘텐츠가 없을 경우)
        // ResourceNotFoundException을 발생시킵니다.
        Content content = contentRepository.findByIdFetchingAllDetails(contentId)
                .orElseThrow(() -> {
                    log.warn("서비스: ID '{}'에 해당하는 콘텐츠를 찾을 수 없어 ResourceNotFoundException을 발생시킵니다.", contentId);
                    return new ResourceNotFoundException("ID '" + contentId + "'에 해당하는 관광 정보를 찾을 수 없습니다.");
                });

        // 조회된 Content 엔티티를 DetailCommonDto로 변환합니다.
        DetailCommonDto detailDto = contentMapper.contentToDetailCommonDto(content);

        log.info("서비스: 콘텐츠 상세 정보 조회 성공 - ID: {}", contentId);
        return detailDto;
    }

    /**
     * 지역(시/도, 시/군/구)을 기준으로 관광 정보를 검색합니다.
     *
     * @param areaCode      검색할 지역(시/도) 코드
     * @param sigunguCode   검색할 시/군/구 코드 (선택 사항)
     * @param contentTypeId 검색할 콘텐츠 타입 ID (선택 사항)
     * @param pageable      페이징 및 정렬 정보
     * @return 검색 조건에 맞는 관광 정보 요약({@link TourSummaryDto})의 {@link Page} 객체
     */
    @Override
    public Page<TourSummaryDto> searchByArea(Integer areaCode, Integer sigunguCode, Integer contentTypeId, Pageable pageable) {
        log.info("서비스: 지역 기반 검색 시작 - areaCode: {}, sigunguCode: {}, contentTypeId: {}, pageable: {}",
                areaCode, sigunguCode, contentTypeId, pageable);

        // ContentRepository에서 조건에 맞는 데이터를 TourSummaryProjection 형태로 조회합니다.
        // TourSummaryProjection은 SELECT 절에서 필요한 컬럼만 선택하여 성능을 최적화하기 위한 인터페이스 기반 프로젝션입니다.
        Page<TourSummaryProjection> projectionPage = contentRepository.findByAreaCriteria(areaCode, sigunguCode, contentTypeId, pageable);

        // 조회된 Projection 페이지를 실제 DTO인 TourSummaryDto 페이지로 변환합니다.
        Page<TourSummaryDto> dtoPage = projectionPage.map(contentMapper::projectionToTourSummaryDto);

        log.info("서비스: 지역 기반 검색 완료. {}건 조회됨 (페이지: {}/{}, 총 {}건)",
                dtoPage.getNumberOfElements(), dtoPage.getNumber() + 1, dtoPage.getTotalPages(), dtoPage.getTotalElements());
        return dtoPage;
    }

    /**
     * 지리적 위치(좌표 및 반경)를 기준으로 관광 정보를 검색합니다.
     *
     * @param mapX          검색 중심의 X좌표 (경도)
     * @param mapY          검색 중심의 Y좌표 (위도)
     * @param radius        검색 반경 (미터 단위)
     * @param contentTypeId 검색할 콘텐츠 타입 ID (선택 사항)
     * @param pageable      페이징 및 정렬 정보
     * @return 검색 조건에 맞는 관광 정보 요약({@link TourSummaryDto})의 {@link Page} 객체
     */
    @Override
    public Page<TourSummaryDto> searchByLocation(BigDecimal mapX, BigDecimal mapY, Integer radius, Integer contentTypeId, Pageable pageable) {
        log.info("서비스: 위치 기반 검색 시작 - mapX: {}, mapY: {}, radius: {}m, contentTypeId: {}, pageable: {}",
                mapX, mapY, radius, contentTypeId, pageable);

        // ContentRepository에서 네이티브 쿼리를 사용하여 위치 기반 검색을 수행하고, 결과를 TourSummaryProjection으로 받습니다.
        Page<TourSummaryProjection> projectionPage = contentRepository.findByLocationNative(mapX, mapY, radius, contentTypeId, pageable);

        // 조회된 Projection 페이지를 실제 DTO인 TourSummaryDto 페이지로 변환합니다.
        Page<TourSummaryDto> dtoPage = projectionPage.map(contentMapper::projectionToTourSummaryDto);

        log.info("서비스: 위치 기반 검색 완료. {}건 조회됨 (페이지: {}/{}, 총 {}건)",
                dtoPage.getNumberOfElements(), dtoPage.getNumber() + 1, dtoPage.getTotalPages(), dtoPage.getTotalElements());
        return dtoPage;
    }
}