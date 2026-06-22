package com.automind.repository;

import com.automind.domain.entity.Servico;
import com.automind.domain.enums.TipoServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByAtivo(boolean ativo);
    List<Servico> findByTipo(TipoServico tipo);

    @Query("SELECT s.tipo, COUNT(i) FROM ItemOrdemServico i JOIN i.servico s " +
           "GROUP BY s.tipo ORDER BY COUNT(i) DESC")
    List<Object[]> findServicosMaisRealizados();
}
