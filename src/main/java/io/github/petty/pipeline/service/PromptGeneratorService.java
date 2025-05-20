package io.github.petty.pipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface PromptGeneratorService {
    String generatePrompt(String extractedPetInfoJson, String location) throws JsonProcessingException;
}
