package io.github.petty.tour.controller;

import io.github.petty.tour.dto.CodeNameDto;
import io.github.petty.tour.dto.DetailCommonDto;
import io.github.petty.tour.dto.TourSummaryDto;
import io.github.petty.tour.service.TourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 관광 정보 조회, 검색 및 추천 관련 웹 요청을 처리하는 컨트롤러입니다.
 * HTML 뷰를 반환하는 메서드와 JSON API 응답을 반환하는 메서드를 포함합니다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService; // 관광 정보 서비스 의존성 주입

    /**
     * 관광지 추천 입력 폼을 보여주는 페이지로 이동합니다.
     *
     * @return "recommend" 뷰 템플릿 이름
     */
    @GetMapping("/recommend")
    public String recommendForm() {
        log.info("GET /recommend - 관광지 추천 폼 페이지 요청");
        return "recommend";
    }

    /**
     * 특정 관광 콘텐츠의 상세 정보 페이지를 반환합니다.
     * - {@link io.github.petty.tour.exception.ResourceNotFoundException} 발생 시:
     * Spring Boot의 기본 에러 처리 메커니즘에 의해 /error/404.html 또는 /error.html 등의 뷰가 렌더링됩니다.
     * (해당 경로에 Thymeleaf 템플릿 파일이 존재해야 합니다.)
     * - 기타 예외 발생 시:
     * 에러 메시지를 모델에 담아 "content_detail" 뷰를 반환하여, 해당 뷰에서 에러를 표시하도록 합니다.
     *
     * @param contentId 조회할 관광 콘텐츠의 고유 ID
     * @param model 뷰에 데이터를 전달하기 위한 Model 객체
     * @return "content_detail" 뷰 템플릿 이름
     */
    @GetMapping("/contents/{contentId}")
    public String contentDetailPage(@PathVariable Long contentId, Model model) {
        log.info("GET /contents/{} - 콘텐츠 상세 페이지 요청", contentId);
        try {
            DetailCommonDto contentDetail = tourService.getContentDetailById(contentId);
            model.addAttribute("contentDetail", contentDetail); // 조회된 정보를 모델에 추가
            log.info("콘텐츠 상세 정보 조회 성공 - ID: {}", contentId);
        } catch (Exception e) {
            // ResourceNotFoundException 이외의 예상치 못한 예외 처리 (예: DB 연결 오류 등)
            log.error("콘텐츠 상세 정보 조회 중 예상치 못한 에러 발생 - ID: {}: {}", contentId, e.getMessage(), e);
            model.addAttribute("errorMessage", "상세 정보를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            // 이 경우에도 content_detail.html을 반환하며, 해당 뷰에서 errorMessage를 적절히 표시해야 합니다.
            // 또는, return "error/general_error_view"; 와 같이 별도의 에러 페이지로 포워딩할 수 있습니다.
        }
        return "content_detail";
    }

    // --- API 엔드포인트 (JSON 응답) ---

    /**
     * 지역(시/도) 코드 또는 특정 지역의 시/군/구 코드 목록을 조회하는 API입니다.
     * - areaCode 파라미터가 제공되지 않으면: 전체 시/도 코드 목록을 반환합니다.
     * - areaCode 파라미터가 제공되면: 해당 시/도의 시/군/구 코드 목록을 반환합니다.
     * 이 API는 페이징을 사용하지 않고 모든 관련 코드를 반환합니다.
     *
     * @param areaCode 조회할 지역의 상위 지역 코드 (선택 사항, Integer)
     * @return HTTP 200 OK 상태와 함께 {@link CodeNameDto} 리스트를 {@link ResponseEntity}로 감싸 반환합니다.
     */
    @GetMapping("/api/tour/codes")
    @ResponseBody // 이 메서드의 반환 값은 HTTP 응답 본문(body)에 직접 작성됩니다.
    public ResponseEntity<List<CodeNameDto>> getAreaCodes(
            @RequestParam(name = "areaCode", required = false) Integer areaCode) {
        if (areaCode == null) {
            log.info("API GET /api/tour/codes - 전체 지역(시/도) 코드 목록 조회 요청");
        } else {
            log.info("API GET /api/tour/codes?areaCode={} - 시/군/구 코드 목록 조회 요청", areaCode);
        }

        List<CodeNameDto> codes = tourService.getAreas(areaCode);

        log.info("API 응답: {}개의 코드 반환 (요청 areaCode: {})", codes.size(), areaCode == null ? "전체" : areaCode);
        return ResponseEntity.ok(codes); // 성공 시 HTTP 200 OK 와 함께 결과 리스트 반환
    }

    /**
     * 지역(시/도, 시/군/구)을 기준으로 관광 정보를 검색하는 API입니다.
     * 페이징을 지원하며, 검색 결과는 {@link TourSummaryDto}의 리스트로 반환됩니다.
     *
     * @param areaCode      검색할 지역(시/도) 코드 (필수, Integer)
     * @param sigunguCode   검색할 시/군/구 코드 (선택 사항, Integer)
     * @param keyword       검색어 (선택 사항, String, 현재 서비스 로직에서는 미반영 상태)
     * @param contentTypeId 검색할 콘텐츠 타입 ID (선택 사항, Integer)
     * @param pageable      페이징 및 정렬 정보 ({@link PageableDefault}로 기본값 설정: 페이지당 10개, 수정 시간 역순 정렬)
     * @return HTTP 200 OK 상태와 함께 {@link TourSummaryDto}의 {@link Page} 객체를 {@link ResponseEntity}로 감싸 반환합니다.
     * `@EnableSpringDataWebSupport`의 `pageSerializationMode = VIA_DTO` 설정에 따라 Page 객체의 직렬화 구조가 기본과 다를 수 있습니다.
     */
    @GetMapping("/api/tour/search/area")
    @ResponseBody
    public ResponseEntity<Page<TourSummaryDto>> searchByArea(
            @RequestParam Integer areaCode,
            @RequestParam(name = "sigunguCode", required = false) Integer sigunguCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer contentTypeId,
            @PageableDefault(size = 10, sort = "modifiedTime", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("API GET /api/tour/search/area - 지역 기반 검색 요청: areaCode={}, sigunguCode={}, keyword='{}', contentTypeId={}, pageable={}",
                areaCode, sigunguCode, keyword, contentTypeId, pageable);

        Page<TourSummaryDto> results = tourService.searchByArea(areaCode, sigunguCode, contentTypeId, pageable);

        log.info("API 응답: 지역 기반 검색 결과 {}건 반환 (페이지: {}/{}, 총 {}건)",
                results.getNumberOfElements(), results.getNumber() + 1, results.getTotalPages(), results.getTotalElements());
        return ResponseEntity.ok(results);
    }

    /**
     * 지리적 위치(좌표 및 반경)를 기준으로 관광 정보를 검색하는 API입니다.
     * 페이징을 지원하며, 검색 결과는 {@link TourSummaryDto}의 리스트로 반환됩니다.
     *
     * @param mapX          검색 중심의 X좌표 (경도, 필수, BigDecimal)
     * @param mapY          검색 중심의 Y좌표 (위도, 필수, BigDecimal)
     * @param radius        검색 반경 (미터 단위, 필수, Integer)
     * @param keyword       검색어 (선택 사항, String, 현재 서비스 로직에서는 미반영 상태)
     * @param contentTypeId 검색할 콘텐츠 타입 ID (선택 사항, Integer)
     * @param pageable      페이징 정보 ({@link PageableDefault}로 기본값 설정: 페이지당 10개)
     * @return HTTP 200 OK 상태와 함께 {@link TourSummaryDto}의 {@link Page} 객체를 {@link ResponseEntity}로 감싸 반환합니다.
     * `@EnableSpringDataWebSupport`의 `pageSerializationMode = VIA_DTO` 설정에 따라 Page 객체의 직렬화 구조가 기본과 다를 수 있습니다.
     */
    @GetMapping("/api/tour/search/location")
    @ResponseBody
    public ResponseEntity<Page<TourSummaryDto>> searchByLocation(
            @RequestParam BigDecimal mapX,
            @RequestParam BigDecimal mapY,
            @RequestParam Integer radius,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer contentTypeId,
            @PageableDefault(size = 10) Pageable pageable) {

        log.info("API GET /api/tour/search/location - 위치 기반 검색 요청: mapX={}, mapY={}, radius={}, keyword='{}', contentTypeId={}, pageable={}",
                mapX, mapY, radius, keyword, contentTypeId, pageable);

        Page<TourSummaryDto> results = tourService.searchByLocation(mapX, mapY, radius, contentTypeId, pageable);

        log.info("API 응답: 위치 기반 검색 결과 {}건 반환 (페이지: {}/{}, 총 {}건)",
                results.getNumberOfElements(), results.getNumber() + 1, results.getTotalPages(), results.getTotalElements());
        return ResponseEntity.ok(results);
    }
}