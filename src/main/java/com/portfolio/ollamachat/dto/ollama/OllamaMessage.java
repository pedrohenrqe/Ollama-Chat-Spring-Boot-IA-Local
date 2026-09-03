package com.portfolio.ollamachat.dto.ollama;

/**
 * Representa uma mensagem no formato esperado pela API /api/chat do Ollama.
 * role: "system" | "user" | "assistant"
 */
public record OllamaMessage(String role, String content) {

    public static OllamaMessage system(String content) {
        return new OllamaMessage("system", content);
    }

    public static OllamaMessage user(String content) {
        return new OllamaMessage("user", content);
    }

    public static OllamaMessage assistant(String content) {
        return new OllamaMessage("assistant", content);
    }
}
