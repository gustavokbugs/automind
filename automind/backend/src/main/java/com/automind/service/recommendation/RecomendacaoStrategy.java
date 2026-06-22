package com.automind.service.recommendation;

import com.automind.domain.entity.RecomendacaoManutencao;
import com.automind.domain.entity.Veiculo;

import java.util.List;
import java.util.Optional;

public interface RecomendacaoStrategy {
    Optional<RecomendacaoManutencao> avaliar(Veiculo veiculo, List<String> servicosRealizados);
}
