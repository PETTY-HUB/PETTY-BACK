package io.github.petty.tour.mapper;


import io.github.petty.tour.dto.*;
import io.github.petty.tour.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

/**
 * 엔티티 객체와 DTO 객체 간의 변환을 처리하는 MapStruct 매퍼 인터페이스입니다.
 * Spring의 컴포넌트로 등록되어 의존성 주입을 통해 사용됩니다 (`componentModel = "spring"`).
 */
@Mapper(componentModel = "spring")
public interface ContentMapper {

    // --- Content 엔티티 -> DetailCommonDto 변환 관련 ---

    /**
     * {@link Content} 엔티티를 {@link DetailCommonDto}로 변환합니다.
     * 연관된 컬렉션(images, infos, rooms) 및 단일 객체(introDetails, petTourInfo)도 함께 매핑합니다.
     *
     * @param content 변환할 Content 엔티티 객체
     * @return 변환된 DetailCommonDto 객체
     */
    @Mapping(source = "contentImages", target = "images")
    @Mapping(source = "contentInfos", target = "infos")
    @Mapping(source = "roomInfos", target = "rooms")
    @Mapping(source = "contentIntro", target = "introDetails")
    DetailCommonDto contentToDetailCommonDto(Content content);

    /**
     * {@link ContentImage} 엔티티를 {@link DetailImageDto}로 변환합니다.
     * ({@code contentToDetailCommonDto} 내부에서 리스트 요소 변환 시 사용됨)
     *
     * @param image 변환할 ContentImage 엔티티 객체
     * @return 변환된 DetailImageDto 객체
     */
    DetailImageDto contentImageToDetailImageDto(ContentImage image);

    /**
     * {@link ContentInfo} 엔티티를 {@link DetailInfoDto}로 변환합니다.
     * ({@code contentToDetailCommonDto} 내부에서 리스트 요소 변환 시 사용됨)
     *
     * @param info 변환할 ContentInfo 엔티티 객체
     * @return 변환된 DetailInfoDto 객체
     */
    DetailInfoDto contentInfoToDetailInfoDto(ContentInfo info);

    /**
     * {@link ContentIntro} 엔티티를 {@link DetailIntroDto}로 변환합니다.
     * {@code ContentIntro.introDetails (Map<String, Object>)} 필드가 {@code DetailIntroDto.introDetails}로 직접 매핑됩니다.
     *
     * @param intro 변환할 ContentIntro 엔티티 객체
     * @return 변환된 DetailIntroDto 객체
     */
    DetailIntroDto contentIntroToDetailIntroDto(ContentIntro intro);

    /**
     * {@link PetTourInfo} 엔티티를 {@link DetailPetDto}로 변환합니다.
     *
     * @param petInfo 변환할 PetTourInfo 엔티티 객체
     * @return 변환된 DetailPetDto 객체
     */
    DetailPetDto petTourInfoToDetailPetDto(PetTourInfo petInfo);

    /**
     * {@link RoomInfo} 엔티티를 {@link RoomInfoDto}로 변환합니다.
     * 숫자(BigDecimal) 타입의 요금 정보를 문자열로, Boolean 타입의 시설 유무 정보를 "Y"/"N" 문자열로 변환하는
     * 커스텀 매핑 로직({@code qualifiedByName})을 사용합니다.
     *
     * @param room 변환할 RoomInfo 엔티티 객체
     * @return 변환된 RoomInfoDto 객체
     */
    @Mapping(source = "roomOffSeasonMinFee1", target = "roomOffSeasonMinFee1", qualifiedByName = "bigDecimalToString")
    @Mapping(source = "roomOffSeasonMinFee2", target = "roomOffSeasonMinFee2", qualifiedByName = "bigDecimalToString")
    @Mapping(source = "roomPeakSeasonMinFee1", target = "roomPeakSeasonMinFee1", qualifiedByName = "bigDecimalToString")
    @Mapping(source = "roomPeakSeasonMinFee2", target = "roomPeakSeasonMinFee2", qualifiedByName = "bigDecimalToString")
    @Mapping(source = "roomBathFacility", target = "roomBathFacility", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomBath", target = "roomBath", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomHomeTheater", target = "roomHomeTheater", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomAirCondition", target = "roomAirCondition", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomTv", target = "roomTv", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomPc", target = "roomPc", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomCable", target = "roomCable", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomInternet", target = "roomInternet", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomRefrigerator", target = "roomRefrigerator", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomToiletries", target = "roomToiletries", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomSofa", target = "roomSofa", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomCook", target = "roomCook", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomTable", target = "roomTable", qualifiedByName = "booleanToYN")
    @Mapping(source = "roomHairdryer", target = "roomHairdryer", qualifiedByName = "booleanToYN")
    RoomInfoDto roomInfoToRoomInfoDto(RoomInfo room);


    // --- TourSummaryProjection -> TourSummaryDto 변환 --

    /**
     * {@link TourSummaryProjection} 인터페이스(또는 객체)를 {@link TourSummaryDto}로 변환합니다.
     * 주로 네이티브 쿼리 결과를 DTO로 매핑할 때 사용됩니다.
     * 'distance' 필드를 'distanceMeters' 필드로 매핑합니다.
     *
     * @param projection 변환할 TourSummaryProjection 객체
     * @return 변환된 TourSummaryDto 객체
     */
    @Mapping(source = "distance", target = "distanceMeters")
    TourSummaryDto projectionToTourSummaryDto(TourSummaryProjection projection);


    // --- Area 엔티티 -> CodeNameDto 변환 ---

    /**
     * {@link Area} 엔티티를 {@link CodeNameDto}로 변환합니다.
     * 'areaCode'를 'code'로, 'areaName'을 'name'으로 매핑합니다.
     *
     * @param area 변환할 Area 엔티티 객체
     * @return 변환된 CodeNameDto 객체
     */
    @Mapping(source = "areaCode", target = "code")
    @Mapping(source = "areaName", target = "name")
    CodeNameDto areaToCodeNameDto(Area area);

    /**
     * {@link Area} 엔티티 리스트를 {@link CodeNameDto} 리스트로 변환합니다.
     * (내부적으로 {@code areaToCodeNameDto} 메서드를 사용)
     *
     * @param areas 변환할 Area 엔티티 리스트
     * @return 변환된 CodeNameDto 리스트
     */
    List<CodeNameDto> areasToCodeNameDtos(List<Area> areas);


    // --- Sigungu 엔티티 -> CodeNameDto 변환 ---

    /**
     * {@link Sigungu} 엔티티를 {@link CodeNameDto}로 변환합니다.
     * 'sigunguCode'를 'code'로, 'sigunguName'을 'name'으로 매핑합니다.
     *
     * @param sigungu 변환할 Sigungu 엔티티 객체
     * @return 변환된 CodeNameDto 객체
     */
    @Mapping(source = "sigunguCode", target = "code")
    @Mapping(source = "sigunguName", target = "name")
    CodeNameDto sigunguToCodeNameDto(Sigungu sigungu);

    /**
     * {@link Sigungu} 엔티티 리스트를 {@link CodeNameDto} 리스트로 변환합니다.
     * (내부적으로 {@code sigunguToCodeNameDto} 메서드를 사용)
     *
     * @param sigungus 변환할 Sigungu 엔티티 리스트
     * @return 변환된 CodeNameDto 리스트
     */
    List<CodeNameDto> sigungusToCodeNameDtos(List<Sigungu> sigungus);


    // --- 커스텀 매핑 헬퍼 메서드 (MapStruct에서 @Named 어노테이션으로 참조) ---

    /**
     * {@link BigDecimal} 타입의 숫자 값을 평문자열(plain string) 형태로 변환합니다.
     * 변환 시 불필요한 후행 0을 제거하고 (예: 10.00 -> "10"),
     * 지수 표현(예: 1E+1) 대신 일반 숫자 문자열(예: "10")로 변환합니다.
     * 값이 null이면 null을 반환합니다.
     *
     * @param value 변환할 BigDecimal 값
     * @return 변환된 문자열 또는 null
     */
    @Named("bigDecimalToString")
    default String bigDecimalToString(BigDecimal value) {
        return (value != null) ? value.stripTrailingZeros().toPlainString() : null;
    }

    /**
     * {@link Boolean} 타입의 값을 "Y" 또는 "N" 문자열로 변환합니다.
     * true는 "Y"로, false는 "N"으로 변환됩니다.
     * 입력 값이 null이면 null을 반환합니다. (요구사항에 따라 "N" 또는 빈 문자열 등으로 처리 변경 가능)
     *
     * @param value 변환할 Boolean 값
     * @return "Y", "N" 또는 null
     */
    @Named("booleanToYN")
    default String booleanToYN(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "Y" : "N"; // true이면 "Y", false이면 "N"
    }
}