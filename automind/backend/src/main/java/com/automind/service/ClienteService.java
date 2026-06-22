package com.automind.service;

import com.automind.domain.entity.Cliente;
import com.automind.dto.request.ClienteRequest;
import com.automind.dto.response.ClienteResponse;
import com.automind.exception.BusinessException;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.ClienteRepository;
import com.automind.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        String cpfLimpo = CpfUtils.limpar(request.getCpf());

        if (clienteRepository.existsByCpf(cpfLimpo)) {
            throw new BusinessException("CPF já cadastrado");
        }
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado");
        }

        Cliente cliente = Cliente.builder()
            .nome(request.getNome())
            .cpf(cpfLimpo)
            .email(request.getEmail().toLowerCase())
            .telefone(request.getTelefone())
            .endereco(request.getEndereco())
            .build();

        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);
        String cpfLimpo = CpfUtils.limpar(request.getCpf());

        if (!cliente.getCpf().equals(cpfLimpo) && clienteRepository.existsByCpf(cpfLimpo)) {
            throw new BusinessException("CPF já cadastrado");
        }
        if (!cliente.getEmail().equals(request.getEmail()) && clienteRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado");
        }

        cliente.setNome(request.getNome());
        cliente.setCpf(cpfLimpo);
        cliente.setEmail(request.getEmail().toLowerCase());
        cliente.setTelefone(request.getTelefone());
        cliente.setEndereco(request.getEndereco());

        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(String termo, Pageable pageable) {
        if (termo != null && !termo.isBlank()) {
            return clienteRepository.buscar(termo, pageable).map(this::toResponse);
        }
        return clienteRepository.findByAtivo(true, pageable).map(this::toResponse);
    }

    @Transactional
    public void inativar(Long id) {
        Cliente cliente = buscarEntidade(id);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    public Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
    }

    private ClienteResponse toResponse(Cliente c) {
        return ClienteResponse.builder()
            .id(c.getId())
            .nome(c.getNome())
            .cpf(CpfUtils.formatar(c.getCpf()))
            .email(c.getEmail())
            .telefone(c.getTelefone())
            .endereco(c.getEndereco())
            .ativo(c.isAtivo())
            .totalVeiculos(c.getVeiculos().size())
            .criadoEm(c.getCriadoEm())
            .build();
    }
}
