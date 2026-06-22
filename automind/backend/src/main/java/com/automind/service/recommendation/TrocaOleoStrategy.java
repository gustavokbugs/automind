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
public class TrocaOleoStrategy implements RecomendacaoStrategy {

    private static final int INTERVALO_KM = 10_000;
    private static final int INTERVALO_MESES = 6;

    private final OrdemServicoRepository ordemServicoRepository;

    @Override
    public Optional<RecomendacaoManutencao> avaliar(Veiculo veiculo, List<String> servicosRealizados) {
        Optional<LocalDateTime> ultimaTrocaOpt = ordemServicoRepository
            .findUltimaDataServicoPorTipo(veiculo.getId(), TipoServico.TROCA_OLEO.name(), StatusOS.CONCLUIDA);

        boolean porKm = false;
        boolean porData = false;

        if (ultimaTrocaOpt.isPresent()) {
            // Sem o km registrado no momento da última troca, só é possível verificar por data
            porData = ultimaTrocaOpt.get().isBefore(LocalDateTime.now().minusMonths(INTERVALO_MESES));
        } else {
            // Nunca fez troca de óleo no sistema — recomendar
            porKm = true;
        }

        if (!porKm && !porData) return Optional.empty();

        boolean urgente = porKm && porData;
        String descricao = urgente
            ? "URGENTE: Troca de óleo atrasada por km e por tempo!"
            : porKm ? "Troca de óleo recomendada: intervalo de km atingido."
                    : "Troca de óleo recomendada: intervalo de 6 meses atingido.";

        RecomendacaoManutencao rec = RecomendacaoManutencao.builder()
            .veiculo(veiculo)
            .tipoServico(TipoServico.TROCA_OLEO)
            .descricao(descricao)
            .kmRecomendado(veiculo.getQuilometragemAtual())
            .dataRecomendada(LocalDate.now())
            .urgente(urgente)
            .build();

        return Optional.of(rec);
    }

}
