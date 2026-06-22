package com.automind.repository;

import com.automind.domain.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCpf(String cpf);
    Optional<Cliente> findByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    @Query("SELECT c FROM Cliente c WHERE c.ativo = true AND " +
           "(LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           " LOWER(c.cpf) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           " LOWER(c.email) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Cliente> buscar(@Param("termo") String termo, Pageable pageable);

    Page<Cliente> findByAtivo(boolean ativo, Pageable pageable);

    long countByAtivo(boolean ativo);
}
