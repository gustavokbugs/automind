package com.automind.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PecaRequest {
    @NotBlank @Size(max = 50)
    private String codigo;

    @NotBlank @Size(max = 150)
    private String nome;

    @Size(max = 300)
    private String descricao;

    @NotNull @DecimalMin("0.01")
    private BigDecimal precoCompra;

    @NotNull @DecimalMin("0.01")
    private BigDecimal precoVenda;

    @NotNull @Min(0)
    private Integer quantidadeEstoque;

    @NotNull @Min(0)
    private Integer estoqueMinimo;

    @Size(max = 80)
    private String fabricante;
}
