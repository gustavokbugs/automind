package com.automind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class ClienteResponse {
    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String endereco;
    private boolean ativo;
    private int totalVeiculos;
    private LocalDateTime criadoEm;
}
