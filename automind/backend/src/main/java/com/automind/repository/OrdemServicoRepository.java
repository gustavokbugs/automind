package com.automind.repository;

import com.automind.domain.entity.OrdemServico;
import com.automind.domain.enums.StatusOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA converte automaticamente os nomes dos métodos em queries SQL.
 * Exemplo: findByTokenPublico → SELECT * FROM ordens_servico WHERE token_publico = ?
 */
@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    Optional<OrdemServico> findByNumero(String numero);
    List<OrdemServico> findByVeiculoId(Long veiculoId);
    Page<OrdemServico> findByStatus(StatusOS status, Pageable pageable);
    long countByStatus(StatusOS status);

    /**
     * Busca OS pelo token público UUID.
     * Endpoint sem autenticação — usado pelo Portal do Cliente.
     */
    Optional<OrdemServico> findByTokenPublico(String tokenPublico);

    @Query("SELECT COALESCE(SUM(os.valorTotal), 0) FROM OrdemServico os " +
           "WHERE os.status = :status AND os.concluidaEm BETWEEN :inicio AND :fim")
    BigDecimal faturamentoPeriodo(
        @Param("status") StatusOS status,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT MAX(os.concluidaEm) FROM OrdemServico os " +
           "JOIN os.itens i JOIN i.servico s " +
           "WHERE os.veiculo.id = :veiculoId AND s.tipo = :tipoServico AND os.status = :status")
    Optional<LocalDateTime> findUltimaDataServicoPorTipo(
        @Param("veiculoId") Long veiculoId,
        @Param("tipoServico") String tipoServico,
        @Param("status") StatusOS status
    );

    @Query("SELECT os FROM OrdemServico os WHERE os.veiculo.cliente.id = :clienteId")
    List<OrdemServico> findByClienteId(@Param("clienteId") Long clienteId);
}
