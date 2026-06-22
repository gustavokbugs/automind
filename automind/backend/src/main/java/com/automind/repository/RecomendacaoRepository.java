package com.automind.repository;

import com.automind.domain.entity.RecomendacaoManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RecomendacaoRepository extends JpaRepository<RecomendacaoManutencao, Long> {
    List<RecomendacaoManutencao> findByVeiculoIdOrderByUrgenteDescGeradaEmDesc(Long veiculoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RecomendacaoManutencao r WHERE r.veiculo.id = :veiculoId AND r.visualizada = false")
    void deleteNaoVisualizadasPorVeiculo(@Param("veiculoId") Long veiculoId);

    long countByVeiculoIdAndVisualizadaFalse(Long veiculoId);
}
