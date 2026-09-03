package com.portfolio.ollamachat.service;

import com.portfolio.ollamachat.config.OllamaProperties;
import com.portfolio.ollamachat.dto.MessageDto;
import com.portfolio.ollamachat.dto.ollama.OllamaMessage;
import com.portfolio.ollamachat.exception.SessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationHistoryServiceTest {

    private ConversationHistoryService service;

    @BeforeEach
    void setUp() {
        OllamaProperties properties = new OllamaProperties(
                "http://localhost:11434", "llama3.2", 30, "system prompt",
                new OllamaProperties.Chat(4) // limite pequeno para facilitar o teste de corte
        );
        service = new ConversationHistoryService(properties);
    }

    @Test
    void deveAdicionarEBuscarMensagens() {
        service.addMessage("s1", OllamaMessage.user("oi"));
        service.addMessage("s1", OllamaMessage.assistant("olá!"));

        var history = service.getHistory("s1");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).role()).isEqualTo("user");
        assertThat(history.get(1).content()).isEqualTo("olá!");
    }

    @Test
    void deveLimitarTamanhoDoHistorico() {
        for (int i = 0; i < 10; i++) {
            service.addMessage("s1", OllamaMessage.user("mensagem " + i));
        }

        var history = service.getHistory("s1");

        assertThat(history).hasSize(4);
        assertThat(history.get(3).content()).isEqualTo("mensagem 9");
    }

    @Test
    void deveLancarExcecaoParaSessaoInexistente() {
        assertThatThrownBy(() -> service.getHistory("inexistente"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void deveLimparHistorico() {
        service.addMessage("s1", OllamaMessage.user("oi"));

        service.clear("s1");

        assertThat(service.exists("s1")).isFalse();
    }
}
