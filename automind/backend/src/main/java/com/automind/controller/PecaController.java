package com.automind.controller;

import com.automind.domain.entity.Peca;
import com.automind.dto.request.PecaRequest;
import com.automind.dto.response.ApiResponse;
import com.automind.service.PecaService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pecas")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Peças", description = "Controle de estoque de peças")
public class PecaController {

    private final PecaService pecaService;

    @PostMapping
    @Operation(summary = "Cadastrar peça")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public ResponseEntity<ApiResponse<Peca>> criar(@Valid @RequestBody PecaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Peça cadastrada", pecaService.criar(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar peça")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public ResponseEntity<ApiResponse<Peca>> atualizar(@PathVariable Long id, @Valid @RequestBody PecaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(pecaService.atualizar(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar peça por ID")
    public ResponseEntity<ApiResponse<Peca>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(pecaService.buscarEntidade(id)));
    }

    @GetMapping
    @Operation(summary = "Listar peças")
    public ResponseEntity<ApiResponse<Page<Peca>>> listar(
        @RequestParam(required = false) String termo,
        @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(pecaService.listar(termo, pageable)));
    }

    @PatchMapping("/{id}/estoque")
    @Operation(summary = "Ajustar estoque")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public ResponseEntity<ApiResponse<Void>> ajustarEstoque(
        @PathVariable Long id, @RequestBody Map<String, Integer> body) {
        pecaService.ajustarEstoque(id, body.get("quantidade"));
        return ResponseEntity.ok(ApiResponse.ok("Estoque atualizado", null));
    }
}
