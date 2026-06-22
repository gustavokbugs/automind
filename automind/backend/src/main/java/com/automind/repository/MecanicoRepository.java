package com.automind.repository;

import com.automind.domain.entity.Mecanico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MecanicoRepository extends JpaRepository<Mecanico, Long> {
    Optional<Mecanico> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    List<Mecanico> findByAtivo(boolean ativo);
}
