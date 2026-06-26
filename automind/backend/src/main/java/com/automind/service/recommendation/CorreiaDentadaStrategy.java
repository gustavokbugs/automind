package com.automind.service.recommendation;

import com.automind.domain.entity.RecomendacaoManutencao;
import com.automind.domain.entity.Veiculo;
import com.automind.domain.enums.StatusOS;
import com.automind.domain.enums.TipoServico;
import com.automind.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CorreiaDentadaStrategy implements RecomendacaoStrategy {

    private static final int INTERVALO_KM = 60_000;

    private final OrdemServicoRepository ordemServicoRepository;

    @Override
    public Optional<RecomendacaoManutencao> avaliar(Veiculo veiculo, List<String> servicosRealizados) {
        boolean jaTrocou = ordemServicoRepository
            .findUltimaDataServicoPorTipo(veiculo.getId(), TipoServico.TROCA_CORREIA_DENTADA, StatusOS.CONCLUIDA)
            .isPresent();

        boolean kmAlto = veiculo.getQuilometragemAtual() >= INTERVALO_KM;

        if (!kmAlto) return Optional.empty();

        // Se já trocou, só recomenda quando está dentro dos 5.000 km finais do próximo intervalo
        if (jaTrocou && veiculo.getQuilometragemAtual() % INTERVALO_KM >= 5000) {
            return Optional.empty();
        }

        boolean urgente = veiculo.getQuilometragemAtual() >= INTERVALO_KM + 5000;

        RecomendacaoManutencao rec = RecomendacaoManutencao.builder()
            .veiculo(veiculo)
            .tipoServico(TipoServico.TROCA_CORREIA_DENTADA)
            .descricao(urgente
                ? "URGENTE: Troca de correia dentada atrasada — risco de dano ao motor!"
                : "Troca de correia dentada recomendada aos " + INTERVALO_KM + " km.")
            .kmRecomendado(INTERVALO_KM)
            .dataRecomendada(LocalDate.now())
            .urgente(urgente)
            .build();

        return Optional.of(rec);
    }
}
