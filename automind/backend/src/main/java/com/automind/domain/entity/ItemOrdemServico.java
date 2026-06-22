package com.automind.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_ordem_servico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemOrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peca_id")
    private Peca peca;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(length = 500)
    private String observacao;

    public BigDecimal getSubtotal() {
        BigDecimal bruto = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        BigDecimal desc = desconto != null ? desconto : BigDecimal.ZERO;
        return bruto.subtract(desc);
    }
}
