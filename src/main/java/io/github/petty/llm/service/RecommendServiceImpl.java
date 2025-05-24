package io.github.petty.llm.service;

import io.github.petty.llm.dto.GeminiRerankRequestDTO;
import io.github.petty.llm.dto.GeminiRerankResponseDTO;
import io.qdrant.client.grpc.PointsInternalService;
import lombok.extern.slf4j.Slf4j;
import io.github.petty.llm.common.AreaCode;
import io.github.petty.llm.dto.RecommendResponseDTO;
import io.github.petty.tour.dto.DetailPetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import io.github.petty.tour.entity.Content;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {
    // RecommendController를 RecommendService로 분리
    private final VectorStoreService vectorStoreService;
    private final ContentService contentService;
    private final GeminiRerankingService geminiRerankingService;


    @Override
    public RecommendResponseDTO recommend(Map<String, String> promptMap) {
        try {
            // 사용자 입력 기반 프롬프트 구성
            String userPrompt = buildPrompt(promptMap);

            // 지역 기반 필터 생성
            String location = promptMap.getOrDefault("location", "");
            Filter.Expression filterExpression = buildRegion(location);

            // 벡터 검색 실행
            List<Document> docs = vectorStoreService.findSimilarWithFilter(userPrompt, 10, filterExpression);
            if (docs.isEmpty()) {
                log.warn("검색 결과가 없습니다: {}", userPrompt);
                // 검색 결과가 없는 경우 처리 (빈 결과 반환 또는 기본값 등)
                return new RecommendResponseDTO(new ArrayList<>());
            }

            // Gemini 리랭킹
            GeminiRerankResponseDTO rerank = geminiRerankingService.rerankGemini(userPrompt, docs);
            List<RecommendResponseDTO.PlaceRecommend> finalRecommendations = buildRecommendResponse(rerank);
            return new RecommendResponseDTO(finalRecommendations);

        } catch (Exception e) {
            log.error("추천 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("추천 생성 실패: " + e.getMessage(), e);
        }
    }

    // 사용자 입력 기반 프롬프트 생성
    private String buildPrompt(Map<String, String> promptMap) {
        StringBuilder sb = new StringBuilder();

        String species = promptMap.get("species");
        String weight = promptMap.get("weight");
        String isDanger = promptMap.get("is_danger");
        String location = promptMap.get("location");

        sb.append(String.format("%s 정도의 몸무게를 가진 %s 종이에요.\n", weight, species));

        if (isDanger.equals("true")) {
            sb.append("맹견이에요\n");
        } else {
            sb.append("소형견, 중형견이에요\n");
        }

        sb.append(String.format("위치는 %s 근처이고, \n", location));

        String info = promptMap.get("info");
        if (info != null) {
            sb.append("추가로 이 장소의 설명은: ").append(info).append("\n");
        }
        return sb.toString();
    }


    // 지역 필터 조건 생성
    private Filter.Expression buildRegion(String location) {
        // String이 아닌 FilterExpression API를 이용해서 QureryDSL 그대로 반환
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        if (location == null || location.isBlank())
            return b.eq("areaCode", 0).build();

        String[] parts = location.trim().split(" ");
        if (parts.length == 0)
            return b.eq("areaCode", 0).build();

        String areaName = parts[0];
        AreaCode areaCode = AreaCode.fromName(areaName);

        // 기본 필터: areaCode
        FilterExpressionBuilder.Op areaExpr = b.eq("areaCode", areaCode.getCode());

        log.info(areaExpr.toString());
        // 시군구까지 필터링
        if (parts.length > 1) {
            String sigungu = parts[1];
            return b.and(areaExpr, b.eq("sigungu", sigungu)).build();
        }
        return areaExpr.build();
    }

    // 유사도 검색
    private List<RecommendResponseDTO.PlaceRecommend> buildRecommendResponse(GeminiRerankResponseDTO rerankResult) {
        Set<String> seenIds = new HashSet<>();

        List<RecommendResponseDTO.PlaceRecommend> resultList = rerankResult.rankedPlaces().stream()
                .map(ranked -> {
                    String contentId = ranked.contentId();

                    // contentId 중복 검사
                    if (!seenIds.add(contentId)) {
                        return null;
                    }

                    try {
                        Optional<Content> contentOpt = contentService.findByContentId(contentId);
                        if (contentOpt.isPresent()) {
                            Content content = contentOpt.get();
                            String imageUrl = contentService.getImageUrl(contentId);

                            String acmpyTypeCd = "";
                            String acmpyPsblCpam = "";
                            String acmpyNeedMtr = "";

                            if (content.getPetTourInfo() != null) {
                                acmpyTypeCd = Optional.ofNullable(content.getPetTourInfo().getAcmpyTypeCd()).orElse("");
                                acmpyPsblCpam = Optional.ofNullable(content.getPetTourInfo().getAcmpyPsblCpam()).orElse("");
                                acmpyNeedMtr = Optional.ofNullable(content.getPetTourInfo().getAcmpyNeedMtr()).orElse("");
                            }

                            return new RecommendResponseDTO.PlaceRecommend(
                                    contentId,
                                    Optional.ofNullable(content.getTitle()).orElse(""),
                                    Optional.ofNullable(content.getAddr1()).orElse(""),
                                    Optional.ofNullable(content.getOverview()).orElse(""),
                                    imageUrl,
                                    acmpyTypeCd,
                                    acmpyPsblCpam,
                                    acmpyNeedMtr,
                                    ranked.recommendReason()
                            );
                        }
                    } catch (Exception e) {
                        log.error("contentId {}에 대한 정보 조회 중 오류 발생: {}", contentId, e.getMessage());
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return resultList;
    }
}
