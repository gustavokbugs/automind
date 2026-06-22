package com.automind.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa uma foto ou vídeo enviado pelo mecânico durante a execução da OS.
 *
 * Os arquivos ficam salvos no servidor local (diretório configurável) e
 * a URL de acesso é armazenada aqui para exibição na linha do tempo do Portal do Cliente.
 *
 * Exemplo de exibição: na Etapa 3 (Execução do Serviço) o cliente vê
 * literalmente o que está sendo feito no carro dele.
 */
@Entity
@Table(name = "midias_os")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MidiaOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** OS à qual esta mídia pertence */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    /** Nome do arquivo salvo no servidor (ex: "abc123.jpg") */
    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    /** URL relativa para acessar o arquivo (ex: "/uploads/abc123.jpg") */
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    /** "FOTO" ou "VIDEO" — define como o frontend exibe o arquivo */
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    /** Legenda opcional adicionada pelo mecânico ao enviar */
    @Column(name = "legenda", length = 300)
    private String legenda;

    /** Momento do upload — define a ordem de exibição na timeline */
    @Column(name = "enviada_em", nullable = false)
    private LocalDateTime enviadaEm;

    @PrePersist
    void prePersist() {
        this.enviadaEm = LocalDateTime.now();
    }
}
