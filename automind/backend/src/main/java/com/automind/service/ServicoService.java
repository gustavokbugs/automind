package com.automind.service;

import com.automind.domain.entity.Servico;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public List<Servico> listar() {
        return servicoRepository.findByAtivo(true);
    }

    @Transactional(readOnly = true)
    public Servico buscarEntidade(Long id) {
        return servicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado: " + id));
    }
}
