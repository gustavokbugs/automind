package com.automind.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VeiculoRequest {
    @NotBlank @Pattern(regexp = "[A-Z]{3}\\d[A-Z0-9]\\d{2}|[A-Z]{3}\\d{4}", message = "Placa inválida")
    private String placa;

    @NotBlank @Size(max = 50)
    private String marca;

    @NotBlank @Size(max = 80)
    private String modelo;

    @NotNull @Min(1900) @Max(2100)
    private Integer ano;

    @NotBlank @Size(max = 20)
    private String cor;

    @NotNull @Min(0)
    private Integer quilometragemAtual;

    @Size(max = 17)
    private String chassis;

    @NotNull
    private Long clienteId;
}
