package com.automind.domain.entity;

import com.automind.domain.enums.TipoServico;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recomendacoes_manutencao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecomendacaoManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoServico tipoServico;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(name = "km_recomendado")
    private Integer kmRecomendado;

    @Column(name = "data_recomendada")
    private LocalDate dataRecomendada;

    @Column(nullable = false)
    private boolean urgente = false;

    @Column(name = "visualizada")
    private boolean visualizada = false;

    @Column(name = "gerada_em", nullable = false, updatable = false)
    private LocalDateTime geradaEm;

    @PrePersist
    void prePersist() { this.geradaEm = LocalDateTime.now(); }
}
