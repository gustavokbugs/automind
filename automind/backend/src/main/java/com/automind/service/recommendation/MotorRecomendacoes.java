package com.automind.service.recommendation;

import com.automind.domain.entity.RecomendacaoManutencao;
import com.automind.domain.entity.Veiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Motor de recomendações — implementa o padrão de projeto Strategy.
 *
 * PADRÃO STRATEGY: Define uma família de algoritmos (cada tipo de manutenção),
 * encapsula cada um em uma classe separada e os torna intercambiáveis.
 * Isso permite adicionar novos tipos de recomendação sem alterar este código.
 *
 * Como funciona:
 *   1. Spring injeta automaticamente TODAS as implementações de RecomendacaoStrategy
 *      (TrocaOleoStrategy, CorreiaDentadaStrategy, etc.) nesta lista
 *   2. Ao gerar recomendações, percorremos cada strategy e pedimos sua avaliação
 *   3. Cada strategy retorna Optional.empty() se não houver recomendação,
 *      ou Optional.of(recomendacao) se o veículo precisar daquele serviço
 *
 * Para adicionar um novo tipo de manutenção, basta criar uma nova classe que
 * implementa RecomendacaoStrategy — este motor vai incluí-la automaticamente.
 * Isso é o princípio Open/Closed do SOLID: aberto para extensão, fechado para modificação.
 */
@Service
@RequiredArgsConstructor
public class MotorRecomendacoes {

    // Spring injeta automaticamente todas as implementações de RecomendacaoStrategy
    // encontradas no classpath (graças ao @Service em cada strategy)
    private final List<RecomendacaoStrategy> strategies;

    /**
     * Avalia todas as strategies e retorna as recomendações aplicáveis ao veículo.
     *
     * @param veiculo veículo a ser avaliado (tem quilometragem, histórico de OS)
     * @param servicosRealizados tipos de serviço já feitos nesta OS (para cruzamento)
     * @return lista de recomendações geradas (pode ser vazia se tudo estiver em dia)
     */
    public List<RecomendacaoManutencao> gerarRecomendacoes(Veiculo veiculo, List<String> servicosRealizados) {
        return strategies.stream()
            .map(strategy -> strategy.avaliar(veiculo, servicosRealizados))
            .filter(Optional::isPresent)          // Remove strategies que não geraram recomendação
            .map(Optional::get)                   // Extrai o valor do Optional
            .collect(Collectors.toList());
    }
}
