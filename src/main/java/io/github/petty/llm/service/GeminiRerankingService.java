package io.github.petty.llm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.petty.llm.dto.GeminiRequestDTO;
import io.github.petty.llm.dto.GeminiRerankResponseDTO;
import io.github.petty.llm.dto.GeminiResponseDTO;
import io.github.petty.llm.dto.RecommendResponseDTO;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiRerankingService {
    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    public GeminiRerankResponseDTO rerankGemini(String userPrompt, List<Document> documents) {
        log.info("GeminiReranking 프롬프트 실행");
        String prompt = buildRerankingPrompt(userPrompt, documents);
        String response = null;
        try {
            response = callGemini(prompt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return parseGemini(response);
    }

    private String buildRerankingPrompt(String userPrompt, List<Document> documents) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 반려동물 동반 여행지 추천 전문가입니다. 사용자의 요청과 반려동물 정보를 바탕으로 다음 후보지들을 평가하고 순위를 매겨주세요.\n\n");

        sb.append("사용자 요청: \n");
        sb.append(userPrompt).append("\n");

        sb.append("후보 장소들:\n");
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String contentId = (String) doc.getMetadata().get("contentId");
            String title = (String) doc.getMetadata().get("title");
            String addr = (String) doc.getMetadata().get("address");
            String description = doc.getText();

            sb.append(String.format("%d. %s\n", i + 1, title));
            sb.append(String.format("   - 주소: %s\n", addr));
            sb.append(String.format("   - 설명: %s\n", description));
            sb.append(String.format("   - contentId: %s\n\n", contentId));
        }

        sb.append("평가 기준:\n");
        sb.append("1. 사용자의 반려동물 정보(견종, 몸무게, 맹견 여부)와 장소의 동반 조건 일치도\n");
        sb.append("2. 사용자가 원하는 지역과의 근접성\n");
        sb.append("3. 사용자의 추가 요구사항 부합도\n");

        sb.append("요구사항:\n");
        sb.append("1. 사용자 요청사항과 평가 기준에 가장 적합한 순서로 정렬해주세요\n");
        sb.append("2. 각 장소별로 사용자 요청에 맞는 구체적인 추천 이유를 100자 이내로 작성해주세요. 자연스러운 평문으로 추천해주세요.\n");
        sb.append("3. 반려동물 동반이 불가능하거나 사용자 조건에 맞지 않는 장소는 제외해주세요.\n");
        sb.append("4. 사용자가 원하는 관광지/숙소/레저/음식점/쇼핑 등 타입이 입력될 경우, 타입에 일치하는 곳만 포함해주세요. 예를 들어 음식점이라고 입력했을 경우 음식점만 출력되게 해주세요.\n");
        sb.append("5. 반드시 아래 JSON 형식으로만 응답해주세요:\n\n");


        sb.append("{\n");
        sb.append("  \"rankedPlaces\": [\n");
        sb.append("    {\n");
        sb.append("      \"contentId\": \"장소ID(입력 받은 placeContentId)\",\n");
        sb.append("      \"recommendReason\": \"추천 이유 (100자 이내)\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");

        sb.append("다른 설명이나 마크다운 없이 오직 JSON만 응답하세요!");

        return sb.toString();
    }

    private String callGemini(String prompt) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s"
                .formatted(apiKey);

        log.info("[Gemini 리랭킹] 프롬프트 전송 중...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(
                                new GeminiRequestDTO(List.of(
                                        new GeminiRequestDTO.Content(List.of(
                                                new GeminiRequestDTO.Part(prompt)
                                        ))
                                ))
                        )
                ))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Gemini API 응답 상태코드: {}", response.statusCode());
        log.info("Gemini API 응답 본문: {}", response.body()); // 이 줄 추가

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API 호출 실패: " + response.statusCode());
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API 호출 실패: " + response.statusCode());
        }

        GeminiResponseDTO resp = objectMapper.readValue(response.body(), GeminiResponseDTO.class);
        return resp.candidates().get(0).content().parts().get(0).text();
    }


    /**
     * Gemini 응답 파싱
     */
    private GeminiRerankResponseDTO parseGemini(String response) {
        try {
            // JSON 부분만 추출 (혹시 다른 텍스트가 포함되어 있을 경우)
            String jsonPart = extractJsonFromResponse(response);
            return objectMapper.readValue(jsonPart, GeminiRerankResponseDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Gemini 응답 파싱 실패: {}", response, e);
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }
    }

    /**
     * 응답에서 JSON 부분만 추출
     */
    private String extractJsonFromResponse(String response) {
        // 1. 먼저 코드 블록 제거
        response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");

        // 2. 앞뒤 공백 제거
        response = response.trim();

        // 3. JSON 시작과 끝 찾기
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            String jsonResult = response.substring(start, end + 1);
            log.info("추출된 JSON: {}", jsonResult); // 디버깅용
            return jsonResult;
        }

        log.error("JSON 추출 실패. 원본 응답: {}", response);
        throw new RuntimeException("유효한 JSON 형식을 찾을 수 없습니다: " + response);
    }
}
