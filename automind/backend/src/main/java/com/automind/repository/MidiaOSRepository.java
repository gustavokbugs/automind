package com.automind.repository;

import com.automind.domain.entity.MidiaOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para acesso às mídias (fotos e vídeos) vinculadas a uma OS.
 * Spring Data JPA gera a implementação automaticamente em tempo de execução.
 */
@Repository
public interface MidiaOSRepository extends JpaRepository<MidiaOS, Long> {

    /**
     * Busca todas as mídias de uma OS ordenadas por data de envio.
     * Usado pelo Portal do Cliente para exibir as mídias na ordem correta.
     */
    List<MidiaOS> findByOrdemServicoIdOrderByEnviadaEmAsc(Long ordemServicoId);
}
