package com.automind.dto.response;

import com.automind.domain.enums.StatusOS;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de resposta do Portal do Cliente — dados PÚBLICOS da OS.
 *
 * Retorna APENAS o que o cliente precisa ver.
 * Nunca expõe: ID interno da OS, dados de custo das peças,
 * dados de outros clientes, dados internos da oficina.
 *
 * Padrão DTO: separamos a entidade do banco (OrdemServico) da
 * representação pública. Isso protege dados sensíveis e permite
 * que o contrato da API evolua independentemente do banco.
 */
@Data
@Builder
public class OsPublicaDTO {

    /** Número legível da OS (ex: "OS-20240612-001") */
    private String numero;

    /** Status atual — mapeia para a etapa na linha do tempo */
    private StatusOS status;

    // Dados do veículo (sem chassi ou informações internas)
    private String veiculoMarca;
    private String veiculoModelo;
    private String veiculoAno;
    private String veiculoPlaca;
    private String veiculoCor;

    /** Nome do mecânico designado (apenas o nome, sem CPF ou dados pessoais) */
    private String mecanicoNome;

    /** Horário em que o veículo deu entrada */
    private LocalDateTime abertaEm;

    /** Previsão de entrega informada no momento da abertura */
    private LocalDateTime previsaoEntrega;

    /** Itens do orçamento visíveis ao cliente */
    private List<ItemPublicoDTO> itens;

    /** Valor total da OS */
    private BigDecimal valorTotal;

    /** Se o cliente já aprovou o orçamento */
    private Boolean orcamentoAprovado;

    /** Momento da aprovação do orçamento */
    private LocalDateTime orcamentoAprovadoEm;

    /** Explicação dos serviços em linguagem simples (gerada por IA ao concluir) */
    private String explicacaoCliente;

    /** Mídias (fotos e vídeos) enviadas pelo mecânico */
    private List<MidiaPublicaDTO> midias;

    // =========================================================================
    // DTOs internos (classes aninhadas para manter tudo no mesmo arquivo)
    // =========================================================================

    /**
     * Item do orçamento visível ao cliente.
     * Mostra nome e valor — omite preço de custo das peças.
     */
    @Data
    @Builder
    public static class ItemPublicoDTO {
        private String descricao;   // Nome do serviço ou peça
        private String tipo;        // "SERVICO" ou "PECA" — usado nos gráficos do portal
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;
    }

    /**
     * Mídia (foto ou vídeo) enviada pelo mecânico.
     * Retorna a URL para o frontend carregar diretamente.
     */
    @Data
    @Builder
    public static class MidiaPublicaDTO {
        private Long id;
        private String url;
        private String tipo;       // "FOTO" ou "VIDEO"
        private String legenda;
        private LocalDateTime enviadaEm;
    }
}
