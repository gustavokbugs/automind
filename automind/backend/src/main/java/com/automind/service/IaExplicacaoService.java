package com.automind.service;

import com.automind.domain.entity.OrdemServico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * Gera uma explicação em linguagem simples dos serviços realizados na OS,
 * usando uma API de LLM (Large Language Model) gratuita.
 *
 * Por padrão usa a API do Groq (https://console.groq.com) com o modelo
 * Llama 3 — gratuita e sem necessidade de cartão de crédito.
 * Configure a chave em application.yml: ia.groq.api-key
 *
 * Se a chave não estiver configurada, retorna um texto padrão para
 * não quebrar o fluxo em ambiente de desenvolvimento.
 *
 * Arquitetura:
 *   OrdemServicoService.concluir() → IaExplicacaoService.gerar() → Groq API
 *   A resposta é salva em OrdemServico.explicacaoCliente no banco.
 *   Não é regerada a cada acesso — gerada UMA VEZ ao concluir a OS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IaExplicacaoService {

    // Chave da API lida do application.yml — vazia por padrão (modo desenvolvimento)
    @Value("${ia.groq.api-key:}")
    private String groqApiKey;

    // Modelo do Groq — llama-3.1-8b-instant é gratuito, atual e rápido
    @Value("${ia.groq.model:llama-3.1-8b-instant}")
    private String modelo;

    // ObjectMapper do Spring — usado para montar e ler o JSON da API com segurança
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    /**
     * Gera a explicação em linguagem simples para os serviços da OS.
     * Retorna texto de fallback se a API não estiver configurada ou falhar.
     */
    public String gerarExplicacao(OrdemServico os) {
        String listaServicos = montarListaServicos(os);

        // Modo desenvolvimento: retorna texto padrão sem chamar a API
        if (groqApiKey == null || groqApiKey.isBlank()) {
            log.warn("Chave Groq não configurada (ia.groq.api-key). Usando texto padrão.");
            return gerarTextoFallback(listaServicos);
        }

        try {
            return chamarGroqApi(listaServicos);
        } catch (Exception e) {
            log.error("Erro ao chamar API de IA: {}", e.getMessage());
            return gerarTextoFallback(listaServicos);
        }
    }

    /**
     * Monta a lista de serviços e peças da OS em formato de texto.
     * Exemplo de saída:
     *   - Troca de óleo e filtro
     *   - Pastilha de freio dianteira (2 unidades)
     */
    private String montarListaServicos(OrdemServico os) {
        return os.getItens().stream()
            .map(item -> {
                if (item.getServico() != null) return "- " + item.getServico().getNome();
                if (item.getPeca() != null) return "- " + item.getPeca().getNome() + " (" + item.getQuantidade() + " unidade(s))";
                return null;
            })
            .filter(s -> s != null)
            .collect(Collectors.joining("\n"));
    }

    /**
     * Chama a API do Groq com o prompt montado.
     * A API do Groq é compatível com o formato da OpenAI (messages array).
     *
     * Tanto o corpo da requisição quanto a leitura da resposta usam Jackson,
     * o que garante escaping correto de aspas, acentos e quebras de linha —
     * o conteúdo gerado pela IA frequentemente contém esses caracteres.
     */
    private String chamarGroqApi(String listaServicos) throws Exception {
        String prompt = """
            Você é um atendente de uma oficina mecânica falando diretamente com o
            dono do veículo, que é um leigo. Explique os serviços abaixo em linguagem
            simples e tranquilizadora, sem jargão técnico.

            Regras importantes:
            - Escreva um único parágrafo corrido, com no máximo 4 frases.
            - NÃO use markdown, asteriscos, títulos, listas ou emojis.
            - Não repita esta instrução; responda apenas com a explicação final.

            Serviços realizados:
            """ + listaServicos;

        // Monta o corpo da requisição como árvore JSON (escaping seguro garantido pelo Jackson)
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", modelo);
        body.put("max_tokens", 300);
        body.put("temperature", 0.7);
        ArrayNode messages = body.putArray("messages");
        ObjectNode mensagem = messages.addObject();
        mensagem.put("role", "user");
        mensagem.put("content", prompt);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
            .header("Authorization", "Bearer " + groqApiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .timeout(Duration.ofSeconds(30))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Groq API retornou status " + response.statusCode() + ": " + response.body());
        }

        return extrairConteudoResposta(response.body());
    }

    /**
     * Extrai o campo "content" da resposta JSON da API do Groq.
     * Formato: {"choices":[{"message":{"content":"..."}}]}
     */
    private String extrairConteudoResposta(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new RuntimeException("Formato de resposta inesperado da API: " + json);
        }
        return content.asText().trim();
    }

    /** Texto padrão usado quando a API não está configurada */
    private String gerarTextoFallback(String listaServicos) {
        return "Os seguintes serviços foram realizados no seu veículo:\n" + listaServicos +
               "\n\nTodos os serviços foram executados por técnicos qualificados seguindo " +
               "as especificações do fabricante. Seu veículo está pronto e em perfeitas condições.";
    }
}
