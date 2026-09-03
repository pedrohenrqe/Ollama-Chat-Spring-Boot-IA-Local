package com.portfolio.ollamachat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo esperado em POST /api/chat e /api/chat/stream
 *
 * @param message   texto enviado pelo usuário (obrigatório)
 * @param sessionId identificador da conversa; se omitido, uma nova sessão é criada
 */
public record ChatRequest(
        @NotBlank(message = "A mensagem não pode estar vazia")
        @Size(max = 4000, message = "A mensagem deve ter no máximo 4000 caracteres")
        String message,

        String sessionId
) {
}
