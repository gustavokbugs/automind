package com.automind.service;

import com.automind.domain.entity.*;
import com.automind.domain.enums.StatusOS;
import com.automind.dto.request.OrdemServicoRequest;
import com.automind.dto.response.OrdemServicoResponse;
import com.automind.exception.BusinessException;
import com.automind.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoService — Testes Unitários")
class OrdemServicoServiceTest {

    @Mock private OrdemServicoRepository ordemServicoRepository;
    @Mock private VeiculoService veiculoService;
    @Mock private MecanicoRepository mecanicoRepository;
    @Mock private ServicoRepository servicoRepository;
    @Mock private PecaService pecaService;

    @InjectMocks private OrdemServicoService ordemServicoService;

    private Veiculo veiculo;
    private OrdemServico osAberta;

    @BeforeEach
    void setUp() {
        Cliente cliente = Cliente.builder().id(1L).nome("Maria").cpf("11122233344").email("m@m.com").build();
        veiculo = Veiculo.builder()
            .id(1L).placa("XYZ5678").marca("Honda").modelo("Civic")
            .ano(2021).quilometragemAtual(30000).cliente(cliente).ordensServico(new ArrayList<>()).build();

        osAberta = OrdemServico.builder()
            .id(1L).numero("OS-20240101-0001")
            .status(StatusOS.ABERTA)
            .veiculo(veiculo)
            .quilometragemEntrada(30000)
            .itens(new ArrayList<>())
            .desconto(BigDecimal.ZERO)
            .valorTotal(BigDecimal.ZERO)
            .build();
    }

    @Test
    @DisplayName("Deve criar OS com sucesso")
    void deveCriarOS() {
        OrdemServicoRequest request = new OrdemServicoRequest();
        request.setVeiculoId(1L);
        request.setQuilometragemEntrada(30000);
        request.setItens(List.of());

        when(veiculoService.buscarEntidade(1L)).thenReturn(veiculo);
        when(ordemServicoRepository.save(any())).thenReturn(osAberta);

        OrdemServicoResponse response = ordemServicoService.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(StatusOS.ABERTA);
    }

    @Test
    @DisplayName("Deve concluir OS com sucesso")
    void deveConcluirOS() {
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(osAberta));
        when(ordemServicoRepository.save(any())).thenReturn(osAberta);

        OrdemServicoResponse response = ordemServicoService.concluir(1L, 31000, "Serviço realizado");

        assertThat(osAberta.getStatus()).isEqualTo(StatusOS.CONCLUIDA);
        assertThat(osAberta.getQuilometragemSaida()).isEqualTo(31000);
    }

    @Test
    @DisplayName("Deve lançar exceção ao concluir OS já concluída")
    void deveLancarExcecaoOSJaConcluida() {
        osAberta.setStatus(StatusOS.CONCLUIDA);
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(osAberta));

        assertThatThrownBy(() -> ordemServicoService.concluir(1L, 31000, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("já concluída");
    }

    @Test
    @DisplayName("Deve lançar exceção quando km saída menor que entrada")
    void deveLancarExcecaoKmInvalido() {
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(osAberta));

        assertThatThrownBy(() -> ordemServicoService.concluir(1L, 29000, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Quilometragem de saída");
    }
}
