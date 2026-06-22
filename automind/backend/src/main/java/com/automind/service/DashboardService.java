package com.automind.service;

import com.automind.domain.enums.StatusOS;
import com.automind.dto.response.DashboardResponse;
import com.automind.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final PecaService pecaService;
    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fimMes = LocalDateTime.now();

        List<DashboardResponse.ServicoPopular> servicosPopulares = servicoRepository
            .findServicosMaisRealizados().stream()
            .limit(5)
            .map(row -> DashboardResponse.ServicoPopular.builder()
                .tipo(row[0].toString())
                .quantidade((Long) row[1])
                .build())
            .collect(Collectors.toList());

        return DashboardResponse.builder()
            .totalClientes(clienteRepository.countByAtivo(true))
            .totalVeiculos(veiculoRepository.count())
            .ordensAbertas(ordemServicoRepository.countByStatus(StatusOS.ABERTA))
            .ordensEmAndamento(ordemServicoRepository.countByStatus(StatusOS.EM_ANDAMENTO))
            .faturamentoMensal(ordemServicoRepository.faturamentoPeriodo(StatusOS.CONCLUIDA, inicioMes, fimMes))
            .pecasEstoqueBaixo(pecaService.getPecasEstoqueBaixo())
            .servicosMaisRealizados(servicosPopulares)
            .build();
    }
}
