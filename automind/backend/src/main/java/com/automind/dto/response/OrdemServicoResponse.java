package com.automind.dto.response;

import com.automind.domain.enums.StatusOS;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrdemServicoResponse {
    private Long id;
    private String numero;
    private StatusOS status;

    /** Token público para geração do link do Portal do Cliente */
    private String tokenPublico;
    private Long veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private Long clienteId;
    private String clienteNome;
    private Long mecanicoId;
    private String mecanicoNome;
    private Integer quilometragemEntrada;
    private Integer quilometragemSaida;
    private String observacoes;
    private String diagnostico;
    private List<ItemOSResponse> itens;
    private BigDecimal valorTotal;
    private BigDecimal desconto;
    private LocalDateTime abertaEm;
    private LocalDateTime concluidaEm;
    private LocalDateTime previsaoEntrega;

    @Data @Builder
    public static class ItemOSResponse {
        private Long id;
        private Long servicoId;
        private String servicoNome;
        private Long pecaId;
        private String pecaNome;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal desconto;
        private BigDecimal subtotal;
        private String observacao;
    }
}
