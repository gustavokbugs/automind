package com.automind.service;

import com.automind.domain.entity.Cliente;
import com.automind.dto.request.ClienteRequest;
import com.automind.dto.response.ClienteResponse;
import com.automind.exception.BusinessException;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService — Testes Unitários")
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private ClienteService clienteService;

    private ClienteRequest requestValido;
    private Cliente clienteSalvo;

    @BeforeEach
    void setUp() {
        requestValido = new ClienteRequest();
        requestValido.setNome("João Silva");
        requestValido.setCpf("123.456.789-09");
        requestValido.setEmail("joao@email.com");
        requestValido.setTelefone("(11) 99999-8888");
        requestValido.setEndereco("Rua A, 123");

        clienteSalvo = Cliente.builder()
            .id(1L)
            .nome("João Silva")
            .cpf("12345678909")
            .email("joao@email.com")
            .telefone("(11) 99999-8888")
            .endereco("Rua A, 123")
            .ativo(true)
            .criadoEm(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void deveCriarClienteComSucesso() {
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

        ClienteResponse response = clienteService.criar(requestValido);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo("João Silva");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF já cadastrado")
    void deveLancarExcecaoCpfDuplicado() {
        when(clienteRepository.existsByCpf(anyString())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(requestValido))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CPF já cadastrado");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando e-mail já cadastrado")
    void deveLancarExcecaoEmailDuplicado() {
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(requestValido))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("E-mail já cadastrado");
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void deveBuscarClientePorId() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));

        ClienteResponse response = clienteService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando cliente não existe")
    void deveLancarExcecaoClienteNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    @DisplayName("Deve inativar cliente")
    void deveInativarCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));
        when(clienteRepository.save(any())).thenReturn(clienteSalvo);

        clienteService.inativar(1L);

        assertThat(clienteSalvo.isAtivo()).isFalse();
        verify(clienteRepository).save(clienteSalvo);
    }
}
