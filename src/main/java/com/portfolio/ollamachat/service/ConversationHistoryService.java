package com.portfolio.ollamachat.service;

import com.portfolio.ollamachat.config.OllamaProperties;
import com.portfolio.ollamachat.dto.MessageDto;
import com.portfolio.ollamachat.dto.ollama.OllamaMessage;
import com.portfolio.ollamachat.exception.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guarda o histórico de mensagens por sessão em memória (ConcurrentHashMap).
 *
 * Simples de propósito: para um projeto de portfólio isso já demonstra a
 * separação de responsabilidades. Em produção, essa camada seria substituída
 * por Redis, banco relacional, etc., sem precisar alterar o restante da aplicação
 * (o serviço de chat depende apenas desta interface implícita).
 */
@Service
@RequiredArgsConstructor
public class ConversationHistoryService {

    private final OllamaProperties properties;

    private record TimedMessage(OllamaMessage message, Instant timestamp) {
    }

    private final Map<String, List<TimedMessage>> sessions = new ConcurrentHashMap<>();

    public void addMessage(String sessionId, OllamaMessage message) {
        List<TimedMessage> history = sessions.computeIfAbsent(sessionId, id -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (history) {
            history.add(new TimedMessage(message, Instant.now()));
            int max = properties.chat().maxHistorySize();
            while (history.size() > max) {
                history.remove(0);
            }
        }
    }

    public List<OllamaMessage> getOllamaMessages(String sessionId) {
        List<TimedMessage> history = sessions.getOrDefault(sessionId, List.of());
        return history.stream().map(TimedMessage::message).toList();
    }

    public List<MessageDto> getHistory(String sessionId) {
        if (!sessions.containsKey(sessionId)) {
            throw new SessionNotFoundException(sessionId);
        }
        return sessions.get(sessionId).stream()
                .map(tm -> new MessageDto(tm.message().role(), tm.message().content(), tm.timestamp()))
                .toList();
    }

    public boolean exists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public void clear(String sessionId) {
        if (sessions.remove(sessionId) == null) {
            throw new SessionNotFoundException(sessionId);
        }
    }
}
