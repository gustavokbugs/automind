package com.automind.controller;

import com.automind.dto.response.ApiResponse;
import com.automind.dto.response.DashboardResponse;
import com.automind.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Dashboard", description = "Métricas e indicadores da oficina")
public class DashboardController {
    /** Serviço que calcula métricas e indicadores exibidos no painel. */
    private final DashboardService dashboardService;

    /**
     * Retorna os dados agregados do dashboard (totais, gráficos, KPIs).
     * Delegação direta ao `DashboardService` que prepara o DTO `DashboardResponse`.
     */
    @GetMapping
    @Operation(summary = "Obter dados do dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboard()));
    }
}
