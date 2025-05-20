package io.github.petty.pipeline.support;

import io.github.petty.pipeline.service.PromptGeneratorService;
import io.github.petty.pipeline.service.TogetherService;
import io.grpc.lb.v1.LoadBalancerGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Component
@Log
@RequiredArgsConstructor
public class TogetherPromptBuilder {

    private final TogetherService togetherService;
    private final PromptGeneratorService promptGeneratorService;

    public String buildPrompt(String visionReport, String location) throws Exception {
        try {
            if (visionReport.isEmpty()) {
                log.info("Vision API 에서 필수 정보를 받아오지 못했습니다.");
                throw new IllegalStateException("Vision API 에서 필수 정보를 받아오지 못했습니다.");
            }
            String extractedPetInfoJson = togetherService.answer(
                    visionReport + " -> 이 문장에서 반려동물의 이름(name), 종(species), 무게(weight), 맹수 여부(is_danger(only true or false))를 JSON 형식으로 작성 + " +
                            "만약 반려동물의 종과 무게를 보았을 때, 입마개가 필요할 것 같다면 맹수 여부를 'true'로 작성 + " + "고양이는 맹수 여부를 false로 작성 " +
                            "무게는 kg 단위를 반드시 포함 " + "no markdown " + "-> 양식에 맞춰서 작성 " + """
                            "name": "???",
                            "species": "???",
                            "weight": "???",
                            "is_danger": "???"
                            """ + " -> 부가 설명이나 그 어떠한 텍스트 없이 양식의 빈 항목을 채워서 답변할 것."
            );
            return promptGeneratorService.generatePrompt(extractedPetInfoJson, location);

        } catch (RuntimeException e) {
            log.info("TogetherPromptBuilder 내부 오류.");
            throw new RuntimeException("프롬프트 빌드 중 예기치 못한 오류가 발생했습니다.");
        }
    }
}
