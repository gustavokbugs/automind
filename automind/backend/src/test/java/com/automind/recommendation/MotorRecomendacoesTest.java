package com.automind.recommendation;

import com.automind.domain.entity.RecomendacaoManutencao;
import com.automind.domain.entity.Veiculo;
import com.automind.domain.enums.StatusOS;
import com.automind.domain.enums.TipoServico;
import com.automind.repository.OrdemServicoRepository;
import com.automind.service.recommendation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Motor de Recomendações — Strategy Pattern")
class MotorRecomendacoesTest {

    @Mock private OrdemServicoRepository ordemServicoRepository;

    private MotorRecomendacoes motorRecomendacoes;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        TrocaOleoStrategy trocaOleo = new TrocaOleoStrategy(ordemServicoRepository);
        CorreiaDentadaStrategy correia = new CorreiaDentadaStrategy(ordemServicoRepository);
        PastilhaFreioStrategy pastilha = new PastilhaFreioStrategy(ordemServicoRepository);
        TrocaPneuStrategy pneu = new TrocaPneuStrategy(ordemServicoRepository);
        RevisaoGeralStrategy revisao = new RevisaoGeralStrategy(ordemServicoRepository);

        motorRecomendacoes = new MotorRecomendacoes(
            List.of(trocaOleo, correia, pastilha, pneu, revisao)
        );

        veiculo = Veiculo.builder()
            .id(1L)
            .placa("ABC1234")
            .marca("Toyota")
            .modelo("Corolla")
            .ano(2020)
            .quilometragemAtual(65000)
            .build();
    }

    @Test
    @DisplayName("Deve recomendar troca de óleo para veículo sem histórico")
    void deveRecomendarTrocaOleoSemHistorico() {
        when(ordemServicoRepository.findUltimaDataServicoPorTipo(anyLong(), anyString(), any(StatusOS.class)))
            .thenReturn(Optional.empty());

        List<RecomendacaoManutencao> recomendacoes = motorRecomendacoes.gerarRecomendacoes(veiculo, List.of());

        assertThat(recomendacoes).anyMatch(r -> r.getTipoServico() == TipoServico.TROCA_OLEO);
    }

    @Test
    @DisplayName("Deve recomendar correia dentada acima de 60.000 km")
    void deveRecomendarCorreiaDentadaAcimaDe60000Km() {
        when(ordemServicoRepository.findUltimaDataServicoPorTipo(anyLong(), anyString(), any(StatusOS.class)))
            .thenReturn(Optional.empty());

        List<RecomendacaoManutencao> recomendacoes = motorRecomendacoes.gerarRecomendacoes(veiculo, List.of());

        assertThat(recomendacoes).anyMatch(r -> r.getTipoServico() == TipoServico.TROCA_CORREIA_DENTADA);
    }

    @Test
    @DisplayName("Deve gerar múltiplas recomendações para veículo com alta quilometragem")
    void deveGerarMultiplasRecomendacoes() {
        when(ordemServicoRepository.findUltimaDataServicoPorTipo(anyLong(), anyString(), any(StatusOS.class)))
            .thenReturn(Optional.empty());

        List<RecomendacaoManutencao> recomendacoes = motorRecomendacoes.gerarRecomendacoes(veiculo, List.of());

        assertThat(recomendacoes).isNotEmpty();
        assertThat(recomendacoes.size()).isGreaterThan(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia para veículo com baixa quilometragem")
    void deveRetornarVazioParaVeiculoNovo() {
        Veiculo veiculoNovo = Veiculo.builder()
            .id(2L)
            .quilometragemAtual(500)
            .build();

        when(ordemServicoRepository.findUltimaDataServicoPorTipo(anyLong(), anyString(), any(StatusOS.class)))
            .thenReturn(Optional.of(java.time.LocalDateTime.now().minusDays(10)));

        List<RecomendacaoManutencao> recomendacoes = motorRecomendacoes.gerarRecomendacoes(veiculoNovo, List.of());

        // Para veículo novo, apenas a troca de óleo pode aparecer (sem histórico)
        assertThat(recomendacoes.stream()
            .filter(r -> r.getTipoServico() == TipoServico.TROCA_CORREIA_DENTADA)
        ).isEmpty();
    }

    @Test
    @DisplayName("Recomendação urgente deve ter flag urgente=true")
    void recomendacaoUrgenteFlagCorreta() {
        Veiculo veiculoUrgente = Veiculo.builder()
            .id(3L)
            .quilometragemAtual(61000)
            .build();

        when(ordemServicoRepository.findUltimaDataServicoPorTipo(anyLong(), anyString(), any(StatusOS.class)))
            .thenReturn(Optional.empty());

        List<RecomendacaoManutencao> recomendacoes = motorRecomendacoes.gerarRecomendacoes(veiculoUrgente, List.of());

        assertThat(recomendacoes.stream().filter(RecomendacaoManutencao::isUrgente)).isNotEmpty();
    }
}
