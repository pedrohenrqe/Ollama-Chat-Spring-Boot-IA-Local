package com.portfolio.ollamachat.exception;

/**
 * Lançada quando a comunicação com o servidor Ollama falha
 * (serviço fora do ar, timeout, modelo inexistente, resposta inválida etc.)
 */
public class OllamaServiceException extends RuntimeException {

    public OllamaServiceException(String message) {
        super(message);
    }

    public OllamaServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
