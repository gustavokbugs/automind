package com.automind.controller;

import com.automind.domain.entity.Servico;
import com.automind.dto.response.ApiResponse;
import com.automind.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Serviços", description = "Catálogo de serviços")
public class ServicoController {

    private final ServicoService servicoService;

    @GetMapping
    @Operation(summary = "Listar serviços ativos")
    public ResponseEntity<ApiResponse<List<Servico>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(servicoService.listar()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID")
    public ResponseEntity<ApiResponse<Servico>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(servicoService.buscarEntidade(id)));
    }
}
