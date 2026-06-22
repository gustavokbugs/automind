package com.automind.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia conexões SSE (Server-Sent Events) abertas pelos clientes no Portal.
 *
 * SSE é uma tecnologia web que mantém a conexão HTTP aberta, permitindo
 * que o servidor envie dados ao navegador sem o cliente precisar fazer
 * requisições repetidas (ao contrário do polling).
 *
 * Quando o mecânico atualiza o status da OS no painel interno,
 * este serviço notifica em tempo real todos os clientes que estão
 * com o portal daquela OS aberto no navegador.
 *
 * ConcurrentHashMap é usado porque múltiplos clientes podem abrir
 * o portal simultaneamente (ex: cliente + familiar) — é thread-safe.
 */
@Slf4j
@Service
public class SseEmitterService {

    // Mapeia token da OS → emitter do cliente conectado
    // Cada token pode ter apenas um emitter ativo (última conexão ganha)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Cria e registra um novo emitter SSE para o token informado.
     * Timeout de 30 minutos — suficiente para acompanhar uma OS padrão.
     */
    public SseEmitter criar(String tokenPublico) {
        // SseEmitter com timeout de 30 minutos (em milissegundos)
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // Registra callbacks para quando a conexão for encerrada
        emitter.onCompletion(() -> emitters.remove(tokenPublico));
        emitter.onTimeout(() -> emitters.remove(tokenPublico));
        emitter.onError(e -> emitters.remove(tokenPublico));

        emitters.put(tokenPublico, emitter);
        log.info("Cliente conectado ao portal SSE da OS: {}", tokenPublico);
        return emitter;
    }

    /**
     * Envia um evento a todos os clientes que estão com o portal da OS aberto.
     * Chamado sempre que o status ou o conteúdo de uma OS é alterado.
     *
     * @param tokenPublico token da OS que foi atualizada
     * @param evento nome do evento (ex: "status-atualizado", "midia-adicionada")
     * @param dados dados a enviar (string JSON ou texto simples)
     */
    public void notificar(String tokenPublico, String evento, String dados) {
        SseEmitter emitter = emitters.get(tokenPublico);
        if (emitter == null) return; // Nenhum cliente conectado — ignora

        try {
            emitter.send(SseEmitter.event()
                .name(evento)
                .data(dados));
            log.info("Evento '{}' enviado para OS: {}", evento, tokenPublico);
        } catch (IOException e) {
            // Conexão caiu — remove o emitter
            log.warn("Erro ao enviar SSE para OS {}: {}", tokenPublico, e.getMessage());
            emitters.remove(tokenPublico);
        }
    }
}
