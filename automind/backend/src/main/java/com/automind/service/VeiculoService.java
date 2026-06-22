package com.automind.service;

import com.automind.domain.entity.Veiculo;
import com.automind.dto.request.VeiculoRequest;
import com.automind.dto.response.VeiculoResponse;
import com.automind.exception.BusinessException;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteService clienteService;

    @Transactional
    public VeiculoResponse criar(VeiculoRequest request) {
        String placa = request.getPlaca().toUpperCase().replaceAll("[^A-Z0-9]", "");

        if (veiculoRepository.existsByPlaca(placa)) {
            throw new BusinessException("Placa já cadastrada: " + placa);
        }

        var cliente = clienteService.buscarEntidade(request.getClienteId());

        Veiculo veiculo = Veiculo.builder()
            .placa(placa)
            .marca(request.getMarca())
            .modelo(request.getModelo())
            .ano(request.getAno())
            .cor(request.getCor())
            .quilometragemAtual(request.getQuilometragemAtual())
            .chassis(request.getChassis())
            .cliente(cliente)
            .build();

        return toResponse(veiculoRepository.save(veiculo));
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
        Veiculo veiculo = buscarEntidade(id);
        String placa = request.getPlaca().toUpperCase().replaceAll("[^A-Z0-9]", "");

        if (!veiculo.getPlaca().equals(placa) && veiculoRepository.existsByPlaca(placa)) {
            throw new BusinessException("Placa já cadastrada: " + placa);
        }

        veiculo.setPlaca(placa);
        veiculo.setMarca(request.getMarca());
        veiculo.setModelo(request.getModelo());
        veiculo.setAno(request.getAno());
        veiculo.setCor(request.getCor());
        veiculo.setQuilometragemAtual(request.getQuilometragemAtual());
        veiculo.setChassis(request.getChassis());

        return toResponse(veiculoRepository.save(veiculo));
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<VeiculoResponse> listar(String termo, Pageable pageable) {
        if (termo != null && !termo.isBlank()) {
            return veiculoRepository.buscar(termo, pageable).map(this::toResponse);
        }
        return veiculoRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarPorCliente(Long clienteId) {
        return veiculoRepository.findByClienteId(clienteId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public Veiculo buscarEntidade(Long id) {
        return veiculoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: " + id));
    }

    private VeiculoResponse toResponse(Veiculo v) {
        return VeiculoResponse.builder()
            .id(v.getId())
            .placa(v.getPlaca())
            .marca(v.getMarca())
            .modelo(v.getModelo())
            .ano(v.getAno())
            .cor(v.getCor())
            .quilometragemAtual(v.getQuilometragemAtual())
            .chassis(v.getChassis())
            .clienteId(v.getCliente().getId())
            .clienteNome(v.getCliente().getNome())
            .totalOS(v.getOrdensServico().size())
            .criadoEm(v.getCriadoEm())
            .build();
    }
}
