package com.automind.controller;

import com.automind.dto.request.VeiculoRequest;
import com.automind.dto.response.ApiResponse;
import com.automind.dto.response.VeiculoResponse;
import com.automind.service.VeiculoService;
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

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Veículos", description = "Gestão de veículos")
public class VeiculoController {

    /** Serviço que gerencia veículos (vínculo com cliente, histórico, validações). */
    private final VeiculoService veiculoService;

    /** Cadastra um veículo para um cliente; valida campos obrigatórios. */
    @PostMapping
    @Operation(summary = "Cadastrar veículo")
    public ResponseEntity<ApiResponse<VeiculoResponse>> criar(@Valid @RequestBody VeiculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Veículo cadastrado", veiculoService.criar(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo")
    public ResponseEntity<ApiResponse<VeiculoResponse>> atualizar(
        @PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(veiculoService.atualizar(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID")
    public ResponseEntity<ApiResponse<VeiculoResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(veiculoService.buscarPorId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar veículos")
    public ResponseEntity<ApiResponse<Page<VeiculoResponse>>> listar(
        @RequestParam(required = false) String termo,
        @PageableDefault(size = 10, sort = "modelo") Pageable pageable) {
        // suporta filtro por termo (placa, modelo) e paginação
        return ResponseEntity.ok(ApiResponse.ok(veiculoService.listar(termo, pageable)));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar veículos por cliente")
    public ResponseEntity<ApiResponse<List<VeiculoResponse>>> listarPorCliente(@PathVariable Long clienteId) {
        // lista veículos pertencentes ao cliente informado
        return ResponseEntity.ok(ApiResponse.ok(veiculoService.listarPorCliente(clienteId)));
    }
}
