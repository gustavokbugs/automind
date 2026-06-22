package com.automind.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.br.CPF;

@Data
public class ClienteRequest {
    @NotBlank @Size(min = 3, max = 100)
    private String nome;

    @NotBlank @CPF
    private String cpf;

    @NotBlank @Email
    private String email;

    // Aceita apenas dígitos, sem máscara — a formatação fica no frontend
    @NotBlank @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos (somente números)")
    private String telefone;

    @Size(max = 200)
    private String endereco;
}
