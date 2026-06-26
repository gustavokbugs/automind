package com.automind.dto.response;

import com.automind.domain.entity.RecomendacaoManutencao;
import com.automind.domain.enums.TipoServico;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de resposta das recomendações de manutenção.
 *
 * Existe para NÃO serializar a entidade {@link RecomendacaoManutencao} diretamente:
 * ela aponta para Veiculo → Cliente → Veiculos (relação bidirecional), o que gera
 * recursão infinita no JSON (payload de centenas de KB e front sem conseguir ler).
 *
 * Aqui expomos apenas o necessário para a tela, achatando o veículo em campos simples.
 */
@Data
@Builder
public class RecomendacaoResponse {

    private Long id;
    private TipoServico tipoServico;
    private String descricao;
    private Integer kmRecomendado;
    private LocalDate dataRecomendada;
    private boolean urgente;
    private boolean visualizada;
    private LocalDateTime geradaEm;

    // Dados achatados do veículo (sem o grafo Cliente ↔ Veiculos)
    private Long veiculoId;
    private String veiculoPlaca;

    public static RecomendacaoResponse from(RecomendacaoManutencao r) {
        return RecomendacaoResponse.builder()
            .id(r.getId())
            .tipoServico(r.getTipoServico())
            .descricao(r.getDescricao())
            .kmRecomendado(r.getKmRecomendado())
            .dataRecomendada(r.getDataRecomendada())
            .urgente(r.isUrgente())
            .visualizada(r.isVisualizada())
            .geradaEm(r.getGeradaEm())
            .veiculoId(r.getVeiculo() != null ? r.getVeiculo().getId() : null)
            .veiculoPlaca(r.getVeiculo() != null ? r.getVeiculo().getPlaca() : null)
            .build();
    }
}
