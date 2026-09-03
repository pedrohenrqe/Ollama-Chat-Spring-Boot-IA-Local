package com.portfolio.ollamachat.controller;

import com.portfolio.ollamachat.dto.ChatRequest;
import com.portfolio.ollamachat.dto.ChatResponse;
import com.portfolio.ollamachat.dto.MessageDto;
import com.portfolio.ollamachat.service.ChatService;
import com.portfolio.ollamachat.service.ConversationHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ConversationHistoryService historyService;

    /**
     * Envia uma mensagem e aguarda a resposta completa do modelo.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(request));
    }

    /**
     * Envia uma mensagem e transmite a resposta em tempo real via Server-Sent Events,
     * assim como fazem interfaces como o ChatGPT.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@Valid @RequestBody ChatRequest request) {
        return chatService.streamMessage(request);
    }

    /**
     * Retorna o histórico completo de uma sessão de conversa.
     */
    @GetMapping("/{sessionId}/history")
    public ResponseEntity<List<MessageDto>> history(@PathVariable String sessionId) {
        return ResponseEntity.ok(historyService.getHistory(sessionId));
    }

    /**
     * Apaga o histórico de uma sessão, iniciando uma nova conversa.
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> clear(@PathVariable String sessionId) {
        historyService.clear(sessionId);
        return ResponseEntity.noContent().build();
    }
}
