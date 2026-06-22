package com.automind.controller;

import com.automind.dto.response.ApiResponse;
import com.automind.dto.response.OsPublicaDTO;
import com.automind.service.OsPublicaService;
import com.automind.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller PÚBLICO do Portal do Cliente.
 *
 * Todos os endpoints aqui NÃO exigem autenticação JWT.
 * O acesso é controlado apenas pelo token UUID único da OS.
 *
 * Endpoints:
 *   GET  /api/public/os/{token}          → dados da OS para o portal
 *   GET  /api/public/os/{token}/eventos  → SSE: atualizações em tempo real
 *   POST /api/public/os/{token}/aprovar-orcamento → cliente aprova orçamento
 *
 * Esses endpoints estão liberados no SecurityConfig via:
 *   .requestMatchers("/public/**").permitAll()
 */
@RestController
@RequestMapping("/public/os")
@RequiredArgsConstructor
@Tag(name = "Portal do Cliente", description = "Endpoints públicos para acompanhamento da OS")
public class OsPublicaController {

    private final OsPublicaService osPublicaService;
    private final SseEmitterService sseEmitterService;

    /**
     * Retorna os dados públicos da OS para exibição no portal do cliente.
     * Não exige autenticação — qualquer pessoa com o link pode acessar.
     */
    @GetMapping("/{token}")
    @Operation(summary = "Buscar dados públicos da OS pelo token")
    public ResponseEntity<ApiResponse<OsPublicaDTO>> buscar(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok(osPublicaService.buscarPorToken(token)));
    }

    /**
     * Endpoint SSE — mantém conexão aberta e envia eventos ao cliente em tempo real.
     *
     * O frontend usa EventSource para ouvir este endpoint:
     *   const source = new EventSource(`/api/public/os/${token}/eventos`)
     *
     * Quando o mecânico atualiza o status da OS, o SseEmitterService.notificar()
     * é chamado e o evento chega instantaneamente no navegador do cliente.
     *
     * MediaType.TEXT_EVENT_STREAM_VALUE é o content-type padrão do protocolo SSE.
     */
    @GetMapping(value = "/{token}/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream de eventos em tempo real (SSE)")
    public SseEmitter streamEventos(@PathVariable String token) {
        return sseEmitterService.criar(token);
    }

    /**
     * Registra a aprovação do orçamento pelo cliente.
     * Chamado quando o cliente clica em "Aprovar orçamento" no portal.
     *
     * Salva: timestamp da aprovação + flag orcamentoAprovado = true
     */
    @PostMapping("/{token}/aprovar-orcamento")
    @Operation(summary = "Cliente aprova o orçamento")
    public ResponseEntity<ApiResponse<OsPublicaDTO>> aprovarOrcamento(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok("Orçamento aprovado com sucesso!", osPublicaService.aprovarOrcamento(token)));
    }
}
