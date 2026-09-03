package com.portfolio.ollamachat.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String sessionId) {
        super("Sessão não encontrada: " + sessionId);
    }
}
