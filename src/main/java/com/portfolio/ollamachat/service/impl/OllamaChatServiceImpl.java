package com.portfolio.ollamachat.service.impl;

import com.portfolio.ollamachat.config.OllamaProperties;
import com.portfolio.ollamachat.dto.ChatRequest;
import com.portfolio.ollamachat.dto.ChatResponse;
import com.portfolio.ollamachat.dto.ollama.OllamaChatRequest;
import com.portfolio.ollamachat.dto.ollama.OllamaChatResponse;
import com.portfolio.ollamachat.dto.ollama.OllamaMessage;
import com.portfolio.ollamachat.exception.OllamaServiceException;
import com.portfolio.ollamachat.service.ChatService;
import com.portfolio.ollamachat.service.ConversationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaChatServiceImpl implements ChatService {

    private final WebClient ollamaWebClient;
    private final OllamaProperties properties;
    private final ConversationHistoryService historyService;

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        List<OllamaMessage> messages = buildMessages(sessionId, request.message());

        long start = System.currentTimeMillis();

        OllamaChatResponse response = ollamaWebClient.post()
                .uri("/api/chat")
                .bodyValue(new OllamaChatRequest(properties.model(), messages, false))
                .retrieve()
                .bodyToMono(OllamaChatResponse.class)
                .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300))
                        .filter(this::isTransient))
                .onErrorMap(this::translateError)
                .block();

        if (response == null || response.message() == null) {
            throw new OllamaServiceException("Resposta vazia recebida do Ollama");
        }

        String reply = response.message().content();
        historyService.addMessage(sessionId, OllamaMessage.user(request.message()));
        historyService.addMessage(sessionId, OllamaMessage.assistant(reply));

        long durationMs = System.currentTimeMillis() - start;
        return new ChatResponse(sessionId, reply, response.model(), durationMs);
    }

    @Override
    public Flux<String> streamMessage(ChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        List<OllamaMessage> messages = buildMessages(sessionId, request.message());
        StringBuilder fullReply = new StringBuilder();

        // Registra a mensagem do usuário imediatamente; a resposta do assistente
        // só é persistida quando o fluxo terminar (doOnComplete).
        historyService.addMessage(sessionId, OllamaMessage.user(request.message()));

        return ollamaWebClient.post()
                .uri("/api/chat")
                .bodyValue(new OllamaChatRequest(properties.model(), messages, true))
                .retrieve()
                .bodyToFlux(OllamaChatResponse.class)
                .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                .onErrorMap(this::translateError)
                .doOnNext(chunk -> {
                    if (chunk.message() != null && chunk.message().content() != null) {
                        fullReply.append(chunk.message().content());
                    }
                })
                .map(chunk -> chunk.message() != null && chunk.message().content() != null
                        ? chunk.message().content() : "")
                .filter(text -> !text.isEmpty())
                .doOnComplete(() -> historyService.addMessage(sessionId, OllamaMessage.assistant(fullReply.toString())));
    }

    private List<OllamaMessage> buildMessages(String sessionId, String userMessage) {
        List<OllamaMessage> messages = new ArrayList<>();
        messages.add(OllamaMessage.system(properties.systemPrompt()));
        messages.addAll(historyService.getOllamaMessages(sessionId));
        messages.add(OllamaMessage.user(userMessage));
        return messages;
    }

    private String resolveSessionId(String sessionId) {
        return (sessionId == null || sessionId.isBlank()) ? UUID.randomUUID().toString() : sessionId;
    }

    private boolean isTransient(Throwable throwable) {
        // Só reexecuta em erros de conexão/timeout, nunca em erros de negócio.
        return !(throwable instanceof WebClientResponseException);
    }

    private Throwable translateError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return new OllamaServiceException(
                    "Ollama retornou status " + ex.getStatusCode() + ": " + ex.getResponseBodyAsString(), ex);
        }
        if (throwable instanceof java.util.concurrent.TimeoutException) {
            return new OllamaServiceException(
                    "Tempo limite excedido ao aguardar resposta do Ollama. Verifique se o serviço está rodando em "
                            + properties.baseUrl(), throwable);
        }
        return new OllamaServiceException(
                "Não foi possível conectar ao Ollama em " + properties.baseUrl()
                        + ". Ele está em execução? (comando: `ollama serve`)", throwable);
    }
}
