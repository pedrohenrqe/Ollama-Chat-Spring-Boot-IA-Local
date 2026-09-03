package com.portfolio.ollamachat.dto;

import java.time.Instant;

public record MessageDto(
        String role,
        String content,
        Instant timestamp
) {
}
