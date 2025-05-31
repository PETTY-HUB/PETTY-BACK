package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관광 콘텐츠의 추가 텍스트 정보(안내 정보 등)를 담는 DTO입니다.
 * 예: 이용 시간, 입장료 안내 등.
 */
@Setter
@Getter
@NoArgsConstructor
public class DetailInfoDto {
    /** 정보 항목의 이름 (예: "이용시간", "입장료") */
    private String infoName;
    /** 정보 항목의 내용 (텍스트 또는 HTML) */
    private String infoText;
}