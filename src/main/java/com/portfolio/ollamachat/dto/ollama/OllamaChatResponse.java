package com.portfolio.ollamachat.dto.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corpo da resposta recebida de POST {ollama.base-url}/api/chat
 * (ignoramos campos extras como eval_count, total_duration etc. para manter o DTO enxuto)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaChatResponse(
        String model,
        OllamaMessage message,
        boolean done
) {
}
