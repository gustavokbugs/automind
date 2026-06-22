package com.automind.repository;

import com.automind.domain.entity.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    Optional<Veiculo> findByPlaca(String placa);
    boolean existsByPlaca(String placa);
    List<Veiculo> findByClienteId(Long clienteId);

    @Query("SELECT v FROM Veiculo v WHERE " +
           "LOWER(v.placa) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(v.modelo) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(v.marca) LIKE LOWER(CONCAT('%', :termo, '%'))")
    Page<Veiculo> buscar(@Param("termo") String termo, Pageable pageable);

    long count();
}
