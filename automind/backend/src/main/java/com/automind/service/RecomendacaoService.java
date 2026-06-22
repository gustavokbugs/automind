package com.automind.service;

import com.automind.domain.entity.RecomendacaoManutencao;
import com.automind.domain.entity.Veiculo;
import com.automind.repository.OrdemServicoRepository;
import com.automind.repository.RecomendacaoRepository;
import com.automind.service.recommendation.MotorRecomendacoes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecomendacaoService {

    private final RecomendacaoRepository recomendacaoRepository;
    private final VeiculoService veiculoService;
    private final OrdemServicoRepository ordemServicoRepository;
    private final MotorRecomendacoes motorRecomendacoes;

    @Transactional
    public List<RecomendacaoManutencao> gerarERetornarRecomendacoes(Long veiculoId) {
        Veiculo veiculo = veiculoService.buscarEntidade(veiculoId);

        // Remove recomendações antigas não visualizadas antes de gerar novas
        recomendacaoRepository.deleteNaoVisualizadasPorVeiculo(veiculoId);

        List<String> servicosRealizados = ordemServicoRepository.findByVeiculoId(veiculoId)
            .stream()
            .flatMap(os -> os.getItens().stream())
            .filter(item -> item.getServico() != null)
            .map(item -> item.getServico().getTipo().name())
            .toList();

        List<RecomendacaoManutencao> recomendacoes = motorRecomendacoes
            .gerarRecomendacoes(veiculo, servicosRealizados);

        recomendacoes.forEach(r -> r.setVeiculo(veiculo));

        return recomendacaoRepository.saveAll(recomendacoes);
    }

    @Transactional(readOnly = true)
    public List<RecomendacaoManutencao> listarPorVeiculo(Long veiculoId) {
        return recomendacaoRepository.findByVeiculoIdOrderByUrgenteDescGeradaEmDesc(veiculoId);
    }

    @Transactional
    public void marcarVisualizada(Long id) {
        recomendacaoRepository.findById(id).ifPresent(r -> {
            r.setVisualizada(true);
            recomendacaoRepository.save(r);
        });
    }
}
