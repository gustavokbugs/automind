package com.automind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class DashboardResponse {
    private long totalClientes;
    private long totalVeiculos;
    private long ordensAbertas;
    private long ordensEmAndamento;
    private BigDecimal faturamentoMensal;
    private List<PecaEstoqueBaixo> pecasEstoqueBaixo;
    private List<ServicoPopular> servicosMaisRealizados;

    @Data @Builder
    public static class PecaEstoqueBaixo {
        private Long id;
        private String codigo;
        private String nome;
        private int quantidadeEstoque;
        private int estoqueMinimo;
    }

    @Data @Builder
    public static class ServicoPopular {
        private String tipo;
        private long quantidade;
    }
}
