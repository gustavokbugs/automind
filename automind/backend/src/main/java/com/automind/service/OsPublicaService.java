package com.automind.service;

import com.automind.domain.entity.MidiaOS;
import com.automind.domain.entity.OrdemServico;
import com.automind.domain.enums.StatusOS;
import com.automind.dto.response.OsPublicaDTO;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelas operações do Portal do Cliente.
 *
 * Todas as operações aqui são PÚBLICAS — não exigem autenticação JWT.
 * Por isso, o serviço retorna apenas dados seguros via OsPublicaDTO.
 *
 * Responsabilidades:
 * - Buscar dados públicos de uma OS por token
 * - Registrar aprovação do orçamento pelo cliente
 * - Converter entidade para DTO público (sem dados sensíveis)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OsPublicaService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final SseEmitterService sseEmitterService;

    /**
     * Busca os dados públicos de uma OS pelo token UUID.
     * Lança ResourceNotFoundException se o token não existir.
     */
    @Transactional(readOnly = true)
    public OsPublicaDTO buscarPorToken(String tokenPublico) {
        OrdemServico os = ordemServicoRepository.findByTokenPublico(tokenPublico)
            .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada com o link informado"));

        return toPublicDTO(os);
    }

    /**
     * Registra a aprovação do orçamento pelo cliente.
     * Salva timestamp da aprovação e notifica via SSE o painel interno.
     *
     * Regra de negócio: só pode aprovar se estiver em diagnóstico (EM_ANDAMENTO sem aprovação)
     */
    @Transactional
    public OsPublicaDTO aprovarOrcamento(String tokenPublico) {
        OrdemServico os = ordemServicoRepository.findByTokenPublico(tokenPublico)
            .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada"));

        if (Boolean.TRUE.equals(os.getOrcamentoAprovado())) {
            throw new IllegalStateException("Orçamento já foi aprovado anteriormente");
        }
        if (os.getStatus() != StatusOS.EM_ANDAMENTO) {
            throw new IllegalStateException("Orçamento só pode ser aprovado quando a OS está em andamento");
        }

        os.aprovarOrcamento();
        ordemServicoRepository.save(os);

        // Notifica o painel interno via SSE que o orçamento foi aprovado
        sseEmitterService.notificar(tokenPublico, "orcamento-aprovado",
            "{\"aprovado\": true, \"token\": \"" + tokenPublico + "\"}");

        log.info("Orçamento aprovado pelo cliente — OS token: {}", tokenPublico);
        return toPublicDTO(os);
    }

    /**
     * Converte a entidade OrdemServico para o DTO público.
     * Aplica a lógica de mascaramento: nunca expõe dados sensíveis.
     */
    public OsPublicaDTO toPublicDTO(OrdemServico os) {
        // Converte os itens (serviços + peças) para o formato público
        List<OsPublicaDTO.ItemPublicoDTO> itensPublicos = os.getItens().stream()
            .map(item -> OsPublicaDTO.ItemPublicoDTO.builder()
                .descricao(item.getServico() != null
                    ? item.getServico().getNome()
                    : item.getPeca() != null ? item.getPeca().getNome() : "Item")
                .tipo(item.getServico() != null ? "SERVICO" : "PECA")
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .subtotal(item.getSubtotal())
                .build())
            .collect(Collectors.toList());

        // Converte as mídias (fotos e vídeos) para o formato público
        List<OsPublicaDTO.MidiaPublicaDTO> midiasPublicas = os.getMidias().stream()
            .map(m -> OsPublicaDTO.MidiaPublicaDTO.builder()
                .id(m.getId())
                .url(m.getUrl())
                .tipo(m.getTipo())
                .legenda(m.getLegenda())
                .enviadaEm(m.getEnviadaEm())
                .build())
            .collect(Collectors.toList());

        return OsPublicaDTO.builder()
            .numero(os.getNumero())
            .status(os.getStatus())
            .veiculoMarca(os.getVeiculo().getMarca())
            .veiculoModelo(os.getVeiculo().getModelo())
            .veiculoAno(String.valueOf(os.getVeiculo().getAno()))
            .veiculoPlaca(os.getVeiculo().getPlaca())
            .veiculoCor(os.getVeiculo().getCor())
            .mecanicoNome(os.getMecanico() != null ? os.getMecanico().getNome() : null)
            .abertaEm(os.getAbertaEm())
            .previsaoEntrega(os.getPrevisaoEntrega())
            .itens(itensPublicos)
            .valorTotal(os.getValorTotal())
            .orcamentoAprovado(os.getOrcamentoAprovado())
            .orcamentoAprovadoEm(os.getOrcamentoAprovadoEm())
            .explicacaoCliente(os.getExplicacaoCliente())
            .midias(midiasPublicas)
            .build();
    }
}
