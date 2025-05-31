package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 반려동물 동반 관광 정보를 담는 DTO입니다.
 */
@Setter
@Getter
@NoArgsConstructor
public class DetailPetDto {
    /** 원본 콘텐츠 ID (참조용) */
    private Long contentId;
    /** 반려동물 동반 시 사고 위험 요소 설명 */
    private String relaAcdntRiskMtr; // (rela_acdnt_risk_mtr) 사고 위험 내용
    /** 동반 가능 반려동물 유형 코드 (예: "가능", "소형견 가능", "불가능") */
    private String acmpyTypeCd;      // (acmpy_type_cd) 동반 구분
    /** 반려동물 관련 편의시설 설명 */
    private String relaPosesFclty;   // (rela_poses_fclty) 보유 시설
    /** 제공되는 반려동물 용품 목록 */
    private String relaFrnshPrdlst;  // (rela_frnsh_prdlst) 비치 품목
    /** 기타 동반 관련 정보 */
    private String etcAcmpyInfo;     // (etc_acmpy_info) 기타 동반 정보
    /** 구매 가능한 반려동물 용품 목록 */
    private String relaPurcPrdlst;   // (rela_purc_prdlst) 구매 품목
    /** 동반 가능한 반려동물 크기/무게 제한 등 */
    private String acmpyPsblCpam;    // (acmpy_psbl_cpam) 동반 가능 크기
    /** 대여 가능한 반려동물 용품 목록 */
    private String relaRntlPrdlst;   // (rela_rntl_prdlst) 대여 품목
    /** 반려동물 동반 시 필수 준비사항 */
    private String acmpyNeedMtr;     // (acmpy_need_mtr) 필요 사항
}