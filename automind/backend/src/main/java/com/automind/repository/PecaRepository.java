package com.automind.repository;

import com.automind.domain.entity.Peca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PecaRepository extends JpaRepository<Peca, Long> {
    Optional<Peca> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);

    @Query("SELECT p FROM Peca p WHERE p.ativo = true AND p.quantidadeEstoque <= p.estoqueMinimo")
    List<Peca> findEstoqueBaixo();

    @Query("SELECT p FROM Peca p WHERE p.ativo = true AND " +
           "(LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           " LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Peca> buscar(String termo, Pageable pageable);

    Page<Peca> findByAtivo(boolean ativo, Pageable pageable);
}
