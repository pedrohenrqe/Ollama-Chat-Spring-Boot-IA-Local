package com.portfolio.ollamachat.dto;

public record ChatResponse(
        String sessionId,
        String reply,
        String model,
        long durationMs
) {
}
