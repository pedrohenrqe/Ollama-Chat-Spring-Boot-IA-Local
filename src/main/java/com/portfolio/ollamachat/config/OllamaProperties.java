package com.portfolio.ollamachat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mapeia as propriedades "ollama.*" definidas em application.yml.
 * Uso de records mantém a classe imutável e enxuta.
 */
@ConfigurationProperties(prefix = "ollama")
public record OllamaProperties(
        String baseUrl,
        String model,
        int timeoutSeconds,
        String systemPrompt,
        Chat chat
) {
    public record Chat(int maxHistorySize) {
    }
}
