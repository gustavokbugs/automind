package com.automind.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mecanicos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mecanico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(length = 100)
    private String especialidade;

    @Column(name = "valor_hora", precision = 10, scale = 2)
    private BigDecimal valorHora;

    // @Builder.Default garante que o mecânico nasça ativo ao usar o builder
    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @OneToMany(mappedBy = "mecanico", fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrdemServico> ordensServico = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() { this.criadoEm = LocalDateTime.now(); }
}
