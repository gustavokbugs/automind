package com.automind.controller;

import com.automind.dto.response.ApiResponse;
import com.automind.dto.response.RecomendacaoResponse;
import com.automind.service.RecomendacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recomendacoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Recomendações", description = "Motor de recomendações de manutenção preventiva")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    @PostMapping("/veiculo/{veiculoId}/gerar")
    @Operation(summary = "Gerar recomendações para um veículo")
    public ResponseEntity<ApiResponse<List<RecomendacaoResponse>>> gerar(@PathVariable Long veiculoId) {
        List<RecomendacaoResponse> recomendacoes = recomendacaoService.gerarERetornarRecomendacoes(veiculoId)
            .stream().map(RecomendacaoResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(
            recomendacoes.isEmpty() ? "Veículo em dia! Nenhuma recomendação necessária." : "Recomendações geradas",
            recomendacoes));
    }

    @GetMapping("/veiculo/{veiculoId}")
    @Operation(summary = "Listar recomendações do veículo")
    public ResponseEntity<ApiResponse<List<RecomendacaoResponse>>> listar(@PathVariable Long veiculoId) {
        List<RecomendacaoResponse> recomendacoes = recomendacaoService.listarPorVeiculo(veiculoId)
            .stream().map(RecomendacaoResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(recomendacoes));
    }

    @PatchMapping("/{id}/visualizar")
    @Operation(summary = "Marcar recomendação como visualizada")
    public ResponseEntity<ApiResponse<Void>> marcarVisualizada(@PathVariable Long id) {
        recomendacaoService.marcarVisualizada(id);
        return ResponseEntity.ok(ApiResponse.ok("Marcada como visualizada", null));
    }
}
