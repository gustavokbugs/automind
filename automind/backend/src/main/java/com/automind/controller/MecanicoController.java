package com.automind.controller;

import com.automind.domain.entity.Mecanico;
import com.automind.dto.request.MecanicoRequest;
import com.automind.dto.response.ApiResponse;
import com.automind.service.MecanicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mecanicos")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Mecânicos", description = "Gestão de mecânicos")
public class MecanicoController {

    /** Serviço com regras de negócio para mecânicos (cadastro, inativação, listagem). */
    private final MecanicoService mecanicoService;

    /** Cadastra um novo mecânico — apenas `ADMIN` pode executar. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar mecânico")
    public ResponseEntity<ApiResponse<Mecanico>> criar(@Valid @RequestBody MecanicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Mecânico cadastrado", mecanicoService.criar(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar mecânico")
    public ResponseEntity<ApiResponse<Mecanico>> atualizar(@PathVariable Long id, @Valid @RequestBody MecanicoRequest request) {
        // atualiza os dados do mecânico indicado por `id`
        return ResponseEntity.ok(ApiResponse.ok(mecanicoService.atualizar(id, request)));
    }

    @GetMapping
    @Operation(summary = "Listar mecânicos ativos")
    public ResponseEntity<ApiResponse<List<Mecanico>>> listar() {
        // retorna apenas mecânicos ativos para exibição no dropdown/painel
        return ResponseEntity.ok(ApiResponse.ok(mecanicoService.listar()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar mecânico por ID")
    public ResponseEntity<ApiResponse<Mecanico>> buscarPorId(@PathVariable Long id) {
        // busca entidade Mecanico; use buscarEntidade quando precisar da entidade completa
        return ResponseEntity.ok(ApiResponse.ok(mecanicoService.buscarEntidade(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inativar mecânico")
    public ResponseEntity<ApiResponse<Void>> inativar(@PathVariable Long id) {
        // inativa o mecânico em vez de deletar para preservar histórico de OS
        mecanicoService.inativar(id);
        return ResponseEntity.ok(ApiResponse.ok("Mecânico inativado", null));
    }
}
