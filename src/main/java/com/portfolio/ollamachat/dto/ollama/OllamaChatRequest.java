package com.portfolio.ollamachat.dto.ollama;

import java.util.List;

/**
 * Corpo da requisição enviada para POST {ollama.base-url}/api/chat
 */
public record OllamaChatRequest(
        String model,
        List<OllamaMessage> messages,
        boolean stream
) {
}
