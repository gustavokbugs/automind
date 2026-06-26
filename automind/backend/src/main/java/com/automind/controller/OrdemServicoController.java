package com.automind.controller;

import com.automind.domain.enums.StatusOS;
import com.automind.dto.request.OrdemServicoRequest;
import com.automind.dto.response.ApiResponse;
import com.automind.dto.response.OrdemServicoResponse;
import com.automind.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Ordens de Serviço", description = "Gestão completa de OS")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    @Operation(summary = "Abrir ordem de serviço")
    public ResponseEntity<ApiResponse<OrdemServicoResponse>> criar(@Valid @RequestBody OrdemServicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("OS aberta com sucesso", ordemServicoService.criar(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar OS por ID")
    public ResponseEntity<ApiResponse<OrdemServicoResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ordemServicoService.buscarPorId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar ordens de serviço")
    public ResponseEntity<ApiResponse<Page<OrdemServicoResponse>>> listar(
        @RequestParam(required = false) StatusOS status,
        @PageableDefault(size = 10, sort = "abertaEm") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ordemServicoService.listar(status, pageable)));
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Adicionar item (serviço ou peça) ao orçamento da OS")
    public ResponseEntity<ApiResponse<OrdemServicoResponse>> adicionarItem(
        @PathVariable Long id,
        @Valid @RequestBody OrdemServicoRequest.ItemOSRequest item) {
        return ResponseEntity.ok(ApiResponse.ok("Item adicionado", ordemServicoService.adicionarItem(id, item)));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @Operation(summary = "Remover item do orçamento da OS")
    public ResponseEntity<ApiResponse<OrdemServicoResponse>> removerItem(
        @PathVariable Long id,
        @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.ok("Item removido", ordemServicoService.removerItem(id, itemId)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status da OS")
    public ResponseEntity<ApiResponse<OrdemServicoResponse>> atualizarStatus(
        @PathVariable Long id,
        @RequestBody Map<String, String> body) {
        StatusOS novoStatus = StatusOS.valueOf(body.get("status"));
        String diagnostico = body.get("diagnostico");
        return ResponseEntity.ok(ApiResponse.ok(ordemServicoService.atualizarStatus(id, novoStatus, diagnostico)));
    }

    @PostMapping("/{id}/concluir")
    @Operation(summary = "Concluir ordem de serviço")
    public ResponseEntity<ApiResponse<OrdemServicoResponse>> concluir(
        @PathVariable Long id,
        @RequestBody Map<String, Object> body) {
        Integer kmSaida = (Integer) body.get("quilometragemSaida");
        String diagnostico = (String) body.get("diagnostico");
        return ResponseEntity.ok(ApiResponse.ok("OS concluída", ordemServicoService.concluir(id, kmSaida, diagnostico)));
    }

    @GetMapping("/veiculo/{veiculoId}/historico")
    @Operation(summary = "Histórico de OS por veículo")
    public ResponseEntity<ApiResponse<List<OrdemServicoResponse>>> historicoPorVeiculo(@PathVariable Long veiculoId) {
        return ResponseEntity.ok(ApiResponse.ok(ordemServicoService.historicoPorVeiculo(veiculoId)));
    }
}
