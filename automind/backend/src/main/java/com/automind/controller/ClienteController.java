package com.automind.controller;

import com.automind.dto.request.ClienteRequest;
import com.automind.dto.response.ApiResponse;
import com.automind.dto.response.ClienteResponse;
import com.automind.service.ClienteService;
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

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Clientes", description = "Gestão de clientes da oficina")
public class ClienteController {

    /** Serviço que contém a lógica de negócio para clientes (CRUD, validações). */
    private final ClienteService clienteService;

    /**
     * Cria um novo cliente.
     * - Requer papel `ADMIN` ou `ATENDENTE`.
     * - Valida o payload com `@Valid` antes de delegar ao service.
     */
    @PostMapping
    @Operation(summary = "Cadastrar cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<ApiResponse<ClienteResponse>> criar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Cliente cadastrado com sucesso", clienteService.criar(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<ApiResponse<ClienteResponse>> atualizar(
        @PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        // atualiza dados do cliente identificado por `id` e retorna o recurso atualizado
        return ResponseEntity.ok(ApiResponse.ok("Cliente atualizado", clienteService.atualizar(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ApiResponse<ClienteResponse>> buscarPorId(@PathVariable Long id) {
        // busca cliente por ID; lança ResourceNotFoundException se não encontrado
        return ResponseEntity.ok(ApiResponse.ok(clienteService.buscarPorId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar clientes com paginação e filtro")
    public ResponseEntity<ApiResponse<Page<ClienteResponse>>> listar(
        @RequestParam(required = false) String termo,
        @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        // lista clientes com paginação e busca por termo (nome, email etc.)
        return ResponseEntity.ok(ApiResponse.ok(clienteService.listar(termo, pageable)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar cliente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> inativar(@PathVariable Long id) {
        // marca cliente como inativo (não remove do banco) para preservar histórico
        clienteService.inativar(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente inativado", null));
    }
}
