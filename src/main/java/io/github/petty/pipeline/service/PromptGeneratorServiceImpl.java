package io.github.petty.pipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Log
public class PromptGeneratorServiceImpl implements PromptGeneratorService {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String generatePrompt(String extractedPetInfoJson, String location) throws JsonProcessingException {
        if (extractedPetInfoJson.isEmpty()) {
            throw new IllegalArgumentException("에러! 필수 정보가 없습니다.");
        } else if (location.isEmpty()) {
            throw new IllegalArgumentException("에러! 사용자 위치 정보가 없습니다.");
        }

        // 기본 문자열
        String petInfoString = """
                {
                %s
                }
                """.formatted(extractedPetInfoJson);
        // JSON 으로 변환
        Map<String, Object> petInfoMap = mapper.readValue(petInfoString, new TypeReference<>() {});
        // location 항목 추가
        petInfoMap.put("location", "%s".formatted(location));
        // JSON 문자열로 변환
        String finalString = mapper.writeValueAsString(petInfoMap);
        log.info(finalString);

        return finalString;

//        return String.format("""
//                {
//                %s,
//                "location": "%s"
//                }
//                """, extractedPetInfoJson, location);
    }
}
