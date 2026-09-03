package com.portfolio.ollamachat.service;

import com.portfolio.ollamachat.dto.ChatRequest;
import com.portfolio.ollamachat.dto.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * Envia a mensagem ao modelo e aguarda a resposta completa (chamada bloqueante).
     */
    ChatResponse sendMessage(ChatRequest request);

    /**
     * Envia a mensagem ao modelo e retorna um fluxo (Flux) com os pedaços (tokens/chunks)
     * da resposta conforme são gerados — usado pelo endpoint SSE de streaming.
     */
    Flux<String> streamMessage(ChatRequest request);
}
