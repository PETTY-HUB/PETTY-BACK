package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 숙박 시설의 객실 정보를 담는 DTO입니다. (주로 contentTypeId가 32인 경우)
 */
@Setter
@Getter
@NoArgsConstructor
public class RoomInfoDto {
    /** 객실 고유 번호 또는 코드 */
    private String roomInfoNo;
    /** 객실명 */
    private String roomTitle;
    /** 객실 크기 정보 (텍스트, 예: "23평") */
    private String roomSize1;
    /** 해당 타입의 객실 수 */
    private Integer roomCount;
    /** 객실 기준 인원 */
    private Integer roomBaseCount;
    /** 객실 최대 인원 */
    private Integer roomMaxCount;
    /** 비수기 주중 최소 요금 (숫자 문자열) */
    private String roomOffSeasonMinFee1;
    /** 비수기 주말 최소 요금 (숫자 문자열) */
    private String roomOffSeasonMinFee2;
    /** 성수기 주중 최소 요금 (숫자 문자열) */
    private String roomPeakSeasonMinFee1;
    /** 성수기 주말 최소 요금 (숫자 문자열) */
    private String roomPeakSeasonMinFee2;
    /** 객실 소개 */
    private String roomIntro;
    /** 욕실 시설 유무 (Y/N 문자열) */
    private String roomBathFacility;
    /** 욕조 유무 (Y/N 문자열) */
    private String roomBath; 
    /** 홈씨어터 유무 (Y/N 문자열) */
    private String roomHomeTheater; 
    /** 에어컨 유무 (Y/N 문자열) */
    private String roomAirCondition; 
    /** TV 유무 (Y/N 문자열) */
    private String roomTv; 
    /** PC 유무 (Y/N 문자열) */
    private String roomPc; 
    /** 케이블 설치 유무 (Y/N 문자열) */
    private String roomCable; 
    /** 인터넷 가능 유무 (Y/N 문자열) */
    private String roomInternet; 
    /** 냉장고 유무 (Y/N 문자열) */
    private String roomRefrigerator; 
    /** 세면도구 제공 유무 (Y/N 문자열) */
    private String roomToiletries; 
    /** 소파 유무 (Y/N 문자열) */
    private String roomSofa; 
    /** 취사 가능 유무 (Y/N 문자열) */
    private String roomCook;
    /** 식탁 유무 (Y/N 문자열) */
    private String roomTable;
    /** 헤어드라이어 유무 (Y/N 문자열) */
    private String roomHairdryer;
    /** 객실 크기 (제곱미터, 예: "76㎡") */
    private String roomSize2;
    /** 객실 이미지 1 URL */
    private String roomImg1;
    /** 객실 이미지 1 설명 */
    private String roomImg1Alt;
    /** 객실 이미지 2 URL */
    private String roomImg2;
    /** 객실 이미지 2 설명 */
    private String roomImg2Alt;
    /** 객실 이미지 3 URL */
    private String roomImg3;
    /** 객실 이미지 3 설명 */
    private String roomImg3Alt;
    /** 객실 이미지 4 URL */
    private String roomImg4;
    /** 객실 이미지 4 설명 */
    private String roomImg4Alt;
    /** 객실 이미지 5 URL */
    private String roomImg5;
    /** 객실 이미지 5 설명 */
    private String roomImg5Alt;
}