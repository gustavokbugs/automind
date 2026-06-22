package com.automind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class VeiculoResponse {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private Integer quilometragemAtual;
    private String chassis;
    private Long clienteId;
    private String clienteNome;
    private int totalOS;
    private LocalDateTime criadoEm;
}
