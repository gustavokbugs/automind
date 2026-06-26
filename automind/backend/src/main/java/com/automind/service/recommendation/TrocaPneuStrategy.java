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
public class TrocaPneuStrategy implements RecomendacaoStrategy {

    private static final int INTERVALO_KM = 50_000;

    private final OrdemServicoRepository ordemServicoRepository;

    @Override
    public Optional<RecomendacaoManutencao> avaliar(Veiculo veiculo, List<String> servicosRealizados) {
        boolean precisaTrocar = veiculo.getQuilometragemAtual() >= INTERVALO_KM
            && veiculo.getQuilometragemAtual() % INTERVALO_KM <= 5000;

        boolean semRegistro = ordemServicoRepository
            .findUltimaDataServicoPorTipo(veiculo.getId(), TipoServico.TROCA_PNEU, StatusOS.CONCLUIDA)
            .isEmpty() && veiculo.getQuilometragemAtual() >= INTERVALO_KM;

        if (!precisaTrocar && !semRegistro) return Optional.empty();

        boolean urgente = veiculo.getQuilometragemAtual() % INTERVALO_KM >= INTERVALO_KM - 2000;

        return Optional.of(RecomendacaoManutencao.builder()
            .veiculo(veiculo)
            .tipoServico(TipoServico.TROCA_PNEU)
            .descricao("Verificação e possível troca de pneus recomendada. Intervalo: " + INTERVALO_KM + " km.")
            .kmRecomendado(INTERVALO_KM)
            .dataRecomendada(LocalDate.now())
            .urgente(urgente)
            .build());
    }
}
