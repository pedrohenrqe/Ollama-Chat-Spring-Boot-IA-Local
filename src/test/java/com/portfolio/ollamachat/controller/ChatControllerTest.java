package com.portfolio.ollamachat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.ollamachat.dto.ChatRequest;
import com.portfolio.ollamachat.dto.ChatResponse;
import com.portfolio.ollamachat.service.ChatService;
import com.portfolio.ollamachat.service.ConversationHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ConversationHistoryService historyService;

    @Test
    void deveRetornar200ComRespostaValida() throws Exception {
        var request = new ChatRequest("Olá, tudo bem?", null);
        var response = new ChatResponse("session-123", "Tudo ótimo, e com você?", "llama3.2", 350);

        when(chatService.sendMessage(any())).thenReturn(response);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-123"))
                .andExpect(jsonPath("$.reply").value("Tudo ótimo, e com você?"));
    }

    @Test
    void deveRetornar400QuandoMensagemVazia() throws Exception {
        var request = new ChatRequest("", null);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
