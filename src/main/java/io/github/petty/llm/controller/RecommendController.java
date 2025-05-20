package io.github.petty.llm.controller;

import io.github.petty.llm.common.AreaCode;
import io.github.petty.llm.service.ChatService;
import io.github.petty.llm.service.RecommendService;
import io.github.petty.llm.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {
    // RecommendService로 분리
    private final RecommendService recommendService;

    @PostMapping
    public ResponseEntity<String> recommend(@RequestBody Map<String, String> promptMap) {
        String result = recommendService.recommend(promptMap);
        return ResponseEntity.ok(result);
    }
    
}
