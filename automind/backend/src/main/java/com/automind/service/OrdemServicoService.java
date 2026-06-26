package com.automind.service;

import com.automind.domain.entity.*;
import com.automind.domain.enums.StatusOS;
import com.automind.dto.request.OrdemServicoRequest;
import com.automind.dto.response.OrdemServicoResponse;
import com.automind.exception.BusinessException;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço principal de Ordens de Serviço.
 *
 * Contém toda a lógica de negócio da OS: criação, atualização de status,
 * conclusão, baixa de estoque e integração com o Portal do Cliente.
 *
 * Padrão de camadas (usado neste projeto):
 *   Controller → Service → Repository → Banco de dados
 *
 * O Service é a única camada que conhece as regras de negócio.
 * O Controller só recebe e responde requisições HTTP.
 * O Repository só faz queries no banco.
 */
@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ItemOrdemServicoRepository itemOrdemServicoRepository;
    private final VeiculoService veiculoService;
    private final MecanicoRepository mecanicoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaService pecaService;
    private final IaExplicacaoService iaExplicacaoService;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request) {
        Veiculo veiculo = veiculoService.buscarEntidade(request.getVeiculoId());

        // UUID gerado no @PrePersist da entidade — mas pode ser definido aqui também
        OrdemServico os = OrdemServico.builder()
            .numero(gerarNumeroOS())
            .veiculo(veiculo)
            .quilometragemEntrada(request.getQuilometragemEntrada())
            .observacoes(request.getObservacoes())
            .previsaoEntrega(request.getPrevisaoEntrega())
            .desconto(request.getDesconto() != null ? request.getDesconto() : BigDecimal.ZERO)
            .status(StatusOS.ABERTA)
            .itens(new ArrayList<>())
            .build();

        if (request.getMecanicoId() != null) {
            Mecanico mecanico = mecanicoRepository.findById(request.getMecanicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mecânico não encontrado"));
            os.setMecanico(mecanico);
        }

        if (request.getItens() != null) {
            for (OrdemServicoRequest.ItemOSRequest itemReq : request.getItens()) {
                ItemOrdemServico item = criarItem(itemReq, os);
                os.getItens().add(item);
            }
        }

        os.calcularTotal();
        veiculo.setQuilometragemAtual(request.getQuilometragemEntrada());

        return toResponse(ordemServicoRepository.save(os));
    }

    /**
     * Gera o número da OS no formato OS-yyyyMMdd-NNNN com sequência diária.
     *
     * A sequência é derivada do BANCO (maior número já existente para a data),
     * e não de um contador em memória — isso evita colisão de chave única após
     * reinício do backend (que zerava o contador e regerava OS-...-0001).
     */
    private String gerarNumeroOS() {
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefixo = "OS-" + data + "-";
        int proximaSequencia = ordemServicoRepository
            .findTopByNumeroStartingWithOrderByNumeroDesc(prefixo)
            .map(ultima -> Integer.parseInt(ultima.getNumero().substring(prefixo.length())) + 1)
            .orElse(1);
        return String.format("%s%04d", prefixo, proximaSequencia);
    }

    @Transactional
    public OrdemServicoResponse atualizarStatus(Long id, StatusOS novoStatus, String diagnostico) {
        OrdemServico os = buscarEntidade(id);

        if (os.getStatus() == StatusOS.CONCLUIDA || os.getStatus() == StatusOS.CANCELADA) {
            throw new BusinessException("OS já finalizada não pode ser alterada");
        }

        os.setStatus(novoStatus);
        if (diagnostico != null) os.setDiagnostico(diagnostico);

        ordemServicoRepository.save(os);

        // Notifica o Portal do Cliente via SSE que o status mudou
        sseEmitterService.notificar(
            os.getTokenPublico(),
            "status-atualizado",
            "{\"status\":\"" + novoStatus.name() + "\"}"
        );

        return toResponse(os);
    }

    @Transactional
    public OrdemServicoResponse concluir(Long id, Integer quilometragemSaida, String diagnostico) {
        OrdemServico os = buscarEntidade(id);

        if (os.getStatus() == StatusOS.CONCLUIDA) {
            throw new BusinessException("OS já concluída");
        }
        if (quilometragemSaida == null) {
            throw new BusinessException("Quilometragem de saída é obrigatória");
        }
        if (quilometragemSaida < os.getQuilometragemEntrada()) {
            throw new BusinessException("Quilometragem de saída não pode ser menor que a de entrada");
        }

        // Baixa no estoque de peças utilizadas
        os.getItens().stream()
            .filter(item -> item.getPeca() != null)
            .forEach(item -> pecaService.ajustarEstoque(item.getPeca().getId(), -item.getQuantidade()));

        os.concluir(quilometragemSaida);
        if (diagnostico != null) os.setDiagnostico(diagnostico);

        // Atualiza quilometragem do veículo
        os.getVeiculo().setQuilometragemAtual(quilometragemSaida);

        // Gera explicação dos serviços em linguagem simples via IA
        // (gerada UMA VEZ ao concluir — salva no banco, não regerada)
        String explicacao = iaExplicacaoService.gerarExplicacao(os);
        os.setExplicacaoCliente(explicacao);

        ordemServicoRepository.save(os);

        // Notifica o Portal do Cliente que a OS foi concluída
        sseEmitterService.notificar(
            os.getTokenPublico(),
            "status-atualizado",
            "{\"status\":\"CONCLUIDA\"}"
        );

        return toResponse(os);
    }

    /**
     * Adiciona um item (serviço OU peça) ao orçamento de uma OS existente.
     * Recalcula o valor total e avisa o Portal do Cliente via SSE.
     * Não é permitido alterar OS já concluída ou cancelada.
     */
    @Transactional
    public OrdemServicoResponse adicionarItem(Long osId, OrdemServicoRequest.ItemOSRequest req) {
        OrdemServico os = buscarEntidade(osId);
        validarEditavel(os);

        boolean temServico = req.getServicoId() != null;
        boolean temPeca = req.getPecaId() != null;
        if (temServico == temPeca) {
            throw new BusinessException("Informe exatamente um serviço OU uma peça por item");
        }

        // A OS já está gerenciada (carregada na transação). Persistimos o item
        // diretamente para obter o ID na hora; o total é atualizado via dirty
        // checking da OS gerenciada (sem merge, que causaria TransientObjectException).
        ItemOrdemServico item = criarItem(req, os);
        itemOrdemServicoRepository.save(item);
        os.getItens().add(item);
        os.calcularTotal();

        notificarOrcamento(os);
        return toResponse(os);
    }

    /**
     * Remove um item do orçamento de uma OS existente e recalcula o total.
     */
    @Transactional
    public OrdemServicoResponse removerItem(Long osId, Long itemId) {
        OrdemServico os = buscarEntidade(osId);
        validarEditavel(os);

        boolean removido = os.getItens().removeIf(i -> itemId.equals(i.getId()));
        if (!removido) {
            throw new ResourceNotFoundException("Item não encontrado nesta OS: " + itemId);
        }

        // orphanRemoval=true na coleção de itens apaga a linha no flush da OS gerenciada
        os.calcularTotal();

        notificarOrcamento(os);
        return toResponse(os);
    }

    /** Bloqueia edição de itens quando a OS já está finalizada. */
    private void validarEditavel(OrdemServico os) {
        if (os.getStatus() == StatusOS.CONCLUIDA || os.getStatus() == StatusOS.CANCELADA) {
            throw new BusinessException("Não é possível alterar itens de uma OS "
                + os.getStatus().name().toLowerCase().replace('_', ' '));
        }
    }

    /** Notifica o Portal do Cliente que o orçamento mudou (para refazer o fetch). */
    private void notificarOrcamento(OrdemServico os) {
        BigDecimal total = os.getValorTotal() != null ? os.getValorTotal() : BigDecimal.ZERO;
        sseEmitterService.notificar(os.getTokenPublico(), "orcamento-atualizado",
            "{\"valorTotal\": " + total + "}");
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResponse> listar(StatusOS status, Pageable pageable) {
        if (status != null) {
            return ordemServicoRepository.findByStatus(status, pageable).map(this::toResponse);
        }
        return ordemServicoRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> historicoPorVeiculo(Long veiculoId) {
        return ordemServicoRepository.findByVeiculoId(veiculoId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public OrdemServico buscarEntidade(Long id) {
        return ordemServicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço não encontrada: " + id));
    }

    private ItemOrdemServico criarItem(OrdemServicoRequest.ItemOSRequest req, OrdemServico os) {
        ItemOrdemServico item = ItemOrdemServico.builder()
            .ordemServico(os)
            .quantidade(req.getQuantidade())
            .precoUnitario(req.getPrecoUnitario())
            .desconto(req.getDesconto() != null ? req.getDesconto() : BigDecimal.ZERO)
            .observacao(req.getObservacao())
            .build();

        if (req.getServicoId() != null) {
            item.setServico(servicoRepository.findById(req.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado")));
        }
        if (req.getPecaId() != null) {
            item.setPeca(pecaService.buscarEntidade(req.getPecaId()));
        }

        return item;
    }

    private OrdemServicoResponse toResponse(OrdemServico os) {
        List<OrdemServicoResponse.ItemOSResponse> itens = os.getItens().stream()
            .map(item -> OrdemServicoResponse.ItemOSResponse.builder()
                .id(item.getId())
                .servicoId(item.getServico() != null ? item.getServico().getId() : null)
                .servicoNome(item.getServico() != null ? item.getServico().getNome() : null)
                .pecaId(item.getPeca() != null ? item.getPeca().getId() : null)
                .pecaNome(item.getPeca() != null ? item.getPeca().getNome() : null)
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .desconto(item.getDesconto())
                .subtotal(item.getSubtotal())
                .observacao(item.getObservacao())
                .build())
            .collect(Collectors.toList());

        return OrdemServicoResponse.builder()
            .id(os.getId())
            .numero(os.getNumero())
            .tokenPublico(os.getTokenPublico())
            .status(os.getStatus())
            .veiculoId(os.getVeiculo().getId())
            .veiculoPlaca(os.getVeiculo().getPlaca())
            .veiculoModelo(os.getVeiculo().getMarca() + " " + os.getVeiculo().getModelo())
            .clienteId(os.getVeiculo().getCliente().getId())
            .clienteNome(os.getVeiculo().getCliente().getNome())
            .mecanicoId(os.getMecanico() != null ? os.getMecanico().getId() : null)
            .mecanicoNome(os.getMecanico() != null ? os.getMecanico().getNome() : null)
            .quilometragemEntrada(os.getQuilometragemEntrada())
            .quilometragemSaida(os.getQuilometragemSaida())
            .observacoes(os.getObservacoes())
            .diagnostico(os.getDiagnostico())
            .itens(itens)
            .valorTotal(os.getValorTotal())
            .desconto(os.getDesconto())
            .abertaEm(os.getAbertaEm())
            .concluidaEm(os.getConcluidaEm())
            .previsaoEntrega(os.getPrevisaoEntrega())
            .build();
    }
}
