package com.automind.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdemServicoRequest {
    @NotNull
    private Long veiculoId;

    private Long mecanicoId;

    @NotNull @Min(0)
    private Integer quilometragemEntrada;

    @Size(max = 1000)
    private String observacoes;

    private LocalDateTime previsaoEntrega;

    @Valid
    private List<ItemOSRequest> itens;

    @DecimalMin("0.0")
    private BigDecimal desconto;

    @Data
    public static class ItemOSRequest {
        private Long servicoId;
        private Long pecaId;

        @NotNull @Min(1)
        private Integer quantidade;

        @NotNull @DecimalMin("0.01")
        private BigDecimal precoUnitario;

        @DecimalMin("0.0")
        private BigDecimal desconto;

        @Size(max = 500)
        private String observacao;
    }
}
