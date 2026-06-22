package com.automind.service;

import com.automind.domain.entity.ItemOrdemServico;
import com.automind.domain.entity.OrdemServico;
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
public class IaExplicacaoService {

    // Chave da API lida do application.yml — vazia por padrão (modo desenvolvimento)
    @Value("${ia.groq.api-key:}")
    private String groqApiKey;

    // Modelo do Groq — llama3-8b-8192 é gratuito e rápido
    @Value("${ia.groq.model:llama3-8b-8192}")
    private String modelo;

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
     */
    private String chamarGroqApi(String listaServicos) throws Exception {
        String prompt = """
            Você é um assistente de uma oficina mecânica.
            Explique os seguintes serviços realizados em linguagem simples,
            sem jargão técnico, para o dono do veículo que é um leigo.
            Seja claro, direto e tranquilizador. Máximo de 4 linhas.

            Serviços realizados:
            """ + listaServicos;

        // Corpo da requisição no formato JSON da API do Groq/OpenAI
        String requestBody = """
            {
              "model": "%s",
              "messages": [{"role": "user", "content": %s}],
              "max_tokens": 300,
              "temperature": 0.7
            }
            """.formatted(modelo, escapeJson(prompt));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
            .header("Authorization", "Bearer " + groqApiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(30))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Groq API retornou status " + response.statusCode());
        }

        // Extrai o texto da resposta JSON sem depender de biblioteca externa
        return extrairConteudoResposta(response.body());
    }

    /**
     * Extrai o campo "content" da resposta JSON da API do Groq.
     * Usamos parsing manual para não adicionar dependência de Jackson aqui.
     * (Jackson já está disponível via Spring Boot mas é didático mostrar sem ele)
     */
    private String extrairConteudoResposta(String json) {
        // A resposta tem formato: {"choices":[{"message":{"content":"..."}}]}
        int start = json.indexOf("\"content\":\"") + 11;
        int end = json.indexOf("\"", start);
        if (start < 11 || end < 0) {
            throw new RuntimeException("Formato de resposta inesperado da API");
        }
        return json.substring(start, end)
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .trim();
    }

    /** Escapa aspas e quebras de linha para inserir no JSON */
    private String escapeJson(String text) {
        return "\"" + text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "") + "\"";
    }

    /** Texto padrão usado quando a API não está configurada */
    private String gerarTextoFallback(String listaServicos) {
        return "Os seguintes serviços foram realizados no seu veículo:\n" + listaServicos +
               "\n\nTodos os serviços foram executados por técnicos qualificados seguindo " +
               "as especificações do fabricante. Seu veículo está pronto e em perfeitas condições.";
    }
}
