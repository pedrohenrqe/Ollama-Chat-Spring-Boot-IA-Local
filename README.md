# 🦙 Ollama Chat — Spring Boot + IA Local

API REST para chat com um modelo de linguagem (LLM) rodando **100% local** via [Ollama](https://ollama.com), construída com Spring Boot 3 e Java 17. Projeto pensado para portfólio: código enxuto, arquitetura em camadas clara e boas práticas do ecossistema Spring.

## ✨ Por que esse projeto é interessante

- **IA sem depender de API paga**: usa Ollama rodando localmente (ou em container), sem chave de API, sem custo por token.
- **Streaming em tempo real (SSE)**: a resposta do modelo é transmitida token a token para o cliente, igual a interfaces tipo ChatGPT.
- **Arquitetura em camadas**: `controller → service → integração externa`, com DTOs próprios (nunca expondo o contrato do Ollama para fora).
- **Tratamento de erros centralizado** (`@RestControllerAdvice`), incluindo timeouts e falhas de conexão com o Ollama.
- **Histórico de conversa por sessão**, com limite configurável de mensagens.
- **Pronto para container**: `Dockerfile` multi-stage + `docker-compose` subindo app e Ollama juntos.
- **Testado**: testes unitários de serviço e testes de controller com `MockMvc`.

## 🧱 Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.3 |
| Cliente HTTP | Spring WebClient (reativo, síncrono e streaming) |
| Validação | Bean Validation (Jakarta) |
| Observabilidade | Spring Actuator (`/actuator/health`) |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc |
| IA | [Ollama](https://ollama.com) (modelo local, ex: `llama3.2`) |
| Build | Maven |
| Container | Docker / Docker Compose |

## 🏗️ Arquitetura

```
Cliente (HTML/JS ou curl)
        │
        ▼
ChatController  ──▶  ChatService (interface)
        │                    │
        │                    ▼
        │           OllamaChatServiceImpl
        │                    │  WebClient
        │                    ▼
        │          Ollama API local (:11434)
        │
        ▼
ConversationHistoryService (memória, por sessão)
```

DTOs internos (`dto.ollama.*`) isolam o formato específico da API do Ollama dos DTOs públicos (`dto.*`) expostos pela nossa API — assim, trocar de provedor de IA no futuro (ex: outro servidor compatível) não quebra o contrato externo.

## 🔌 Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/chat` | Envia mensagem, aguarda resposta completa |
| `POST` | `/api/chat/stream` | Envia mensagem, recebe resposta via SSE (streaming) |
| `GET` | `/api/chat/{sessionId}/history` | Histórico da conversa |
| `DELETE` | `/api/chat/{sessionId}` | Apaga o histórico da sessão |
| `GET` | `/actuator/health` | Status da aplicação |

### Exemplo — chat simples

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Explique o que é injeção de dependência em uma frase."}'
```

```json
{
  "sessionId": "9f2a1c3e-...",
  "reply": "Injeção de dependência é um padrão onde um objeto recebe suas dependências de fora, em vez de criá-las ele mesmo.",
  "model": "llama3.2",
  "durationMs": 812
}
```

### Exemplo — streaming (SSE)

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Conte até 5", "sessionId": "9f2a1c3e-..."}'
```

## ▶️ Como rodar localmente

### Pré-requisitos
- Java 17+
- Maven 3.9+
- [Ollama instalado](https://ollama.com/download) e um modelo baixado:
  ```bash
  ollama pull llama3.2
  ollama serve
  ```

### Rodando a aplicação
```bash
mvn spring-boot:run
```

Acesse:
- Interface de teste: http://localhost:8080
- Health check: http://localhost:8080/actuator/health

### Rodando os testes
```bash
mvn test
```

## 🐳 Rodando com Docker Compose

Sobe a aplicação **e** o Ollama juntos:

```bash
docker compose up --build
```

Depois, baixe o modelo dentro do container do Ollama (uma vez):
```bash
docker exec -it ollama ollama pull llama3.2
```

## ⚙️ Configuração

Todas as variáveis ficam em `application.yml` (ou podem ser sobrescritas por variáveis de ambiente):

```yaml
ollama:
  base-url: http://localhost:11434
  model: llama3.2
  timeout-seconds: 60
  system-prompt: "Você é um assistente virtual útil, direto e educado."
  chat:
    max-history-size: 20
```

## 🚀 Possíveis evoluções

- Persistir o histórico em banco de dados (Postgres/Redis) em vez de memória.
- Autenticação por usuário (Spring Security + JWT), vinculando sessões a contas.
- RAG (Retrieval-Augmented Generation) indexando documentos próprios.
- Métricas de uso por modelo/sessão via Micrometer + Prometheus.
