package io.github.petty.tour.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지역(Area) 또는 시군구(Sigungu) 등의 코드와 이름 정보를 전달하기 위한 공통 DTO입니다.
 * 주로 UI의 선택 목록(드롭다운) 등에 사용됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeNameDto {
    /** 코드 값 (예: 지역 코드, 시군구 코드) */
    private Integer code;

    /** 코드에 해당하는 이름 (예: 지역명, 시군구명) */
    private String name;
}