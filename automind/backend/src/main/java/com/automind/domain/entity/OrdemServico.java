package com.automind.domain.entity;

import com.automind.domain.enums.StatusOS;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade principal do sistema — representa uma Ordem de Serviço (OS).
 *
 * Cada OS está ligada a um veículo e opcionalmente a um mecânico.
 * Contém os itens realizados (serviços + peças), o histórico de status
 * e os dados necessários para o Portal do Cliente (tokenPublico).
 *
 * Padrão: @Builder do Lombok permite criar OS com sintaxe fluente:
 *   OrdemServico.builder().numero("...").veiculo(v).build()
 */
@Entity
@Table(name = "ordens_servico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número legível da OS (ex: "OS-20240612-001") gerado pelo NumeroOSGenerator */
    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    /**
     * Token UUID único que identifica esta OS no Portal do Cliente.
     * É exposto na URL pública: /os/{tokenPublico}
     * Nunca expõe o ID interno da OS ao cliente.
     */
    @Column(name = "token_publico", nullable = false, unique = true, length = 36)
    private String tokenPublico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOS status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mecanico_id")
    private Mecanico mecanico;

    @Column(name = "quilometragem_entrada", nullable = false)
    private Integer quilometragemEntrada;

    @Column(name = "quilometragem_saida")
    private Integer quilometragemSaida;

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "diagnostico", length = 2000)
    private String diagnostico;

    /** Itens da OS (serviços executados + peças utilizadas) */
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemOrdemServico> itens = new ArrayList<>();

    /** Fotos e vídeos enviados pelo mecânico — visíveis no portal do cliente */
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MidiaOS> midias = new ArrayList<>();

    @Column(name = "valor_total", precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "desconto", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal desconto = BigDecimal.ZERO;

    /** true quando o cliente aprovou o orçamento pelo portal */
    @Column(name = "orcamento_aprovado")
    @Builder.Default
    private Boolean orcamentoAprovado = false;

    /** Momento exato em que o cliente clicou em "Aprovar orçamento" */
    @Column(name = "orcamento_aprovado_em")
    private LocalDateTime orcamentoAprovadoEm;

    /**
     * Explicação dos serviços gerada por IA ao concluir a OS.
     * Escrita em linguagem simples para o cliente leigo.
     * Gerada UMA VEZ e salva no banco — não é regerada a cada acesso.
     */
    @Column(name = "explicacao_cliente", length = 3000)
    private String explicacaoCliente;

    @Column(name = "aberta_em", nullable = false)
    private LocalDateTime abertaEm;

    @Column(name = "concluida_em")
    private LocalDateTime concluidaEm;

    @Column(name = "previsao_entrega")
    private LocalDateTime previsaoEntrega;

    /**
     * Executado automaticamente pelo JPA antes de INSERT.
     * Garante que campos obrigatórios tenham valores padrão.
     */
    @PrePersist
    void prePersist() {
        this.abertaEm = LocalDateTime.now();
        if (this.status == null) this.status = StatusOS.ABERTA;
        if (this.desconto == null) this.desconto = BigDecimal.ZERO;
        if (this.orcamentoAprovado == null) this.orcamentoAprovado = false;
        // Gera o token público caso não tenha sido definido manualmente
        if (this.tokenPublico == null) this.tokenPublico = UUID.randomUUID().toString();
    }

    /** Soma o valor de todos os itens e aplica o desconto global da OS */
    public void calcularTotal() {
        BigDecimal total = itens.stream()
            .map(ItemOrdemServico::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorTotal = total.subtract(this.desconto != null ? this.desconto : BigDecimal.ZERO);
    }

    /** Finaliza a OS, registra quilometragem de saída e calcula o total final */
    public void concluir(Integer quilometragemSaida) {
        this.status = StatusOS.CONCLUIDA;
        this.quilometragemSaida = quilometragemSaida;
        this.concluidaEm = LocalDateTime.now();
        calcularTotal();
    }

    /** Registra aprovação do orçamento pelo cliente no portal */
    public void aprovarOrcamento() {
        this.orcamentoAprovado = true;
        this.orcamentoAprovadoEm = LocalDateTime.now();
    }
}
