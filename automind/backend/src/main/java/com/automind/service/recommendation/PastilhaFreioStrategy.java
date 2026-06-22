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
public class PastilhaFreioStrategy implements RecomendacaoStrategy {

    private static final int INTERVALO_KM = 30_000;

    private final OrdemServicoRepository ordemServicoRepository;

    @Override
    public Optional<RecomendacaoManutencao> avaliar(Veiculo veiculo, List<String> servicosRealizados) {
        Optional<LocalDateTime> ultimaOpt = ordemServicoRepository
            .findUltimaDataServicoPorTipo(veiculo.getId(), TipoServico.TROCA_PASTILHA_FREIO.name(), StatusOS.CONCLUIDA);

        boolean deveRecomendar = false;

        if (ultimaOpt.isEmpty()) {
            deveRecomendar = veiculo.getQuilometragemAtual() >= INTERVALO_KM;
        } else {
            int kmRestante = INTERVALO_KM - (veiculo.getQuilometragemAtual() % INTERVALO_KM);
            deveRecomendar = kmRestante <= 3000;
        }

        if (!deveRecomendar) return Optional.empty();

        boolean urgente = veiculo.getQuilometragemAtual() % INTERVALO_KM >= INTERVALO_KM - 1000;

        return Optional.of(RecomendacaoManutencao.builder()
            .veiculo(veiculo)
            .tipoServico(TipoServico.TROCA_PASTILHA_FREIO)
            .descricao(urgente
                ? "URGENTE: Pastilhas de freio no limite — troque imediatamente!"
                : "Troca de pastilhas de freio recomendada nos próximos 3.000 km.")
            .kmRecomendado(veiculo.getQuilometragemAtual() + (INTERVALO_KM - veiculo.getQuilometragemAtual() % INTERVALO_KM))
            .dataRecomendada(LocalDate.now().plusMonths(1))
            .urgente(urgente)
            .build());
    }
}
