package com.automind.service;

import com.automind.domain.entity.Mecanico;
import com.automind.dto.request.MecanicoRequest;
import com.automind.exception.BusinessException;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.MecanicoRepository;
import com.automind.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MecanicoService {

    private final MecanicoRepository mecanicoRepository;

    @Transactional
    public Mecanico criar(MecanicoRequest request) {
        String cpf = CpfUtils.limpar(request.getCpf());
        if (mecanicoRepository.existsByCpf(cpf)) {
            throw new BusinessException("CPF já cadastrado");
        }
        return mecanicoRepository.save(Mecanico.builder()
            .nome(request.getNome())
            .cpf(cpf)
            .telefone(request.getTelefone())
            .especialidade(request.getEspecialidade())
            .valorHora(request.getValorHora())
            .build());
    }

    @Transactional
    public Mecanico atualizar(Long id, MecanicoRequest request) {
        Mecanico m = buscarEntidade(id);
        String cpf = CpfUtils.limpar(request.getCpf());
        if (!m.getCpf().equals(cpf) && mecanicoRepository.existsByCpf(cpf)) {
            throw new BusinessException("CPF já cadastrado");
        }
        m.setNome(request.getNome());
        m.setCpf(cpf);
        m.setTelefone(request.getTelefone());
        m.setEspecialidade(request.getEspecialidade());
        m.setValorHora(request.getValorHora());
        return mecanicoRepository.save(m);
    }

    @Transactional(readOnly = true)
    public List<Mecanico> listar() {
        return mecanicoRepository.findByAtivo(true);
    }

    @Transactional(readOnly = true)
    public Mecanico buscarEntidade(Long id) {
        return mecanicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Mecânico não encontrado: " + id));
    }

    @Transactional
    public void inativar(Long id) {
        Mecanico m = buscarEntidade(id);
        m.setAtivo(false);
        mecanicoRepository.save(m);
    }
}
