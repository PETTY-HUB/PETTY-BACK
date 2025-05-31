package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * 관광 콘텐츠의 소개 정보(타입별 특화 정보)를 담는 DTO입니다.
 * 다양한 필드를 가질 수 있어 Map 형태로 유연하게 구성됩니다.
 */
@Setter
@Getter
@NoArgsConstructor
public class DetailIntroDto {
    /**
     * 소개 정보를 담는 Map 객체.
     * key는 정보 항목의 이름(영문 필드명), value는 해당 정보 값입니다.
     * 예: {"eventstartdate": "20250101", "eventenddate": "20250103", "playtime": "상시"}
     */
    private Map<String, Object> introDetails;
}