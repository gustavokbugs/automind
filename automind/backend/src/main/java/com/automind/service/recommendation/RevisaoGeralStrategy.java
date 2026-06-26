package com.automind.service.recommendation;

import com.automind.domain.entity.RecomendacaoManutencao;
import com.automind.domain.entity.Veiculo;
import com.automind.domain.enums.StatusOS;
import com.automind.domain.enums.TipoServico;
import com.automind.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RevisaoGeralStrategy implements RecomendacaoStrategy {

    private static final int INTERVALO_MESES = 12;

    private final OrdemServicoRepository ordemServicoRepository;

    @Override
    public Optional<RecomendacaoManutencao> avaliar(Veiculo veiculo, List<String> servicosRealizados) {
        Optional<LocalDateTime> ultimaRevisaoOpt = ordemServicoRepository
            .findUltimaDataServicoPorTipo(veiculo.getId(), TipoServico.REVISAO_GERAL, StatusOS.CONCLUIDA);

        boolean deveRevisar;

        if (ultimaRevisaoOpt.isEmpty()) {
            deveRevisar = true;
        } else {
            deveRevisar = ultimaRevisaoOpt.get()
                .isBefore(LocalDateTime.now().minusMonths(INTERVALO_MESES));
        }

        if (!deveRevisar) return Optional.empty();

        return Optional.of(RecomendacaoManutencao.builder()
            .veiculo(veiculo)
            .tipoServico(TipoServico.REVISAO_GERAL)
            .descricao("Revisão geral anual recomendada para manter o veículo em perfeito estado.")
            .dataRecomendada(LocalDate.now())
            .urgente(false)
            .build());
    }
}
