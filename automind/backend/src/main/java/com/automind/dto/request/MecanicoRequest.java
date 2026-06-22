package com.automind.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;

@Data
public class MecanicoRequest {
    @NotBlank @Size(min = 3, max = 100)
    private String nome;

    @NotBlank @CPF
    private String cpf;

    @NotBlank
    private String telefone;

    @Size(max = 100)
    private String especialidade;

    @DecimalMin("0.0")
    private BigDecimal valorHora;
}
