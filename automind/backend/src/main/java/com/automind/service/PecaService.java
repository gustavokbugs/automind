package com.automind.service;

import com.automind.domain.entity.Peca;
import com.automind.dto.request.PecaRequest;
import com.automind.dto.response.DashboardResponse;
import com.automind.exception.BusinessException;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.PecaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PecaService {

    private final PecaRepository pecaRepository;

    @Transactional
    public Peca criar(PecaRequest request) {
        if (pecaRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Código de peça já cadastrado: " + request.getCodigo());
        }
        return pecaRepository.save(Peca.builder()
            .codigo(request.getCodigo().toUpperCase())
            .nome(request.getNome())
            .descricao(request.getDescricao())
            .precoCompra(request.getPrecoCompra())
            .precoVenda(request.getPrecoVenda())
            .quantidadeEstoque(request.getQuantidadeEstoque())
            .estoqueMinimo(request.getEstoqueMinimo())
            .fabricante(request.getFabricante())
            .build());
    }

    @Transactional
    public Peca atualizar(Long id, PecaRequest request) {
        Peca peca = buscarEntidade(id);
        if (!peca.getCodigo().equals(request.getCodigo()) && pecaRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Código já cadastrado");
        }
        peca.setCodigo(request.getCodigo().toUpperCase());
        peca.setNome(request.getNome());
        peca.setDescricao(request.getDescricao());
        peca.setPrecoCompra(request.getPrecoCompra());
        peca.setPrecoVenda(request.getPrecoVenda());
        peca.setQuantidadeEstoque(request.getQuantidadeEstoque());
        peca.setEstoqueMinimo(request.getEstoqueMinimo());
        peca.setFabricante(request.getFabricante());
        return pecaRepository.save(peca);
    }

    @Transactional(readOnly = true)
    public Peca buscarEntidade(Long id) {
        return pecaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Peca> listar(String termo, Pageable pageable) {
        if (termo != null && !termo.isBlank()) {
            return pecaRepository.buscar(termo, pageable);
        }
        return pecaRepository.findByAtivo(true, pageable);
    }

    @Transactional(readOnly = true)
    public List<DashboardResponse.PecaEstoqueBaixo> getPecasEstoqueBaixo() {
        return pecaRepository.findEstoqueBaixo().stream()
            .map(p -> DashboardResponse.PecaEstoqueBaixo.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nome(p.getNome())
                .quantidadeEstoque(p.getQuantidadeEstoque())
                .estoqueMinimo(p.getEstoqueMinimo())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void ajustarEstoque(Long id, int quantidade) {
        Peca peca = buscarEntidade(id);
        int novaQtd = peca.getQuantidadeEstoque() + quantidade;
        if (novaQtd < 0) {
            throw new BusinessException("Estoque insuficiente. Disponível: " + peca.getQuantidadeEstoque());
        }
        peca.setQuantidadeEstoque(novaQtd);
        pecaRepository.save(peca);
    }
}
