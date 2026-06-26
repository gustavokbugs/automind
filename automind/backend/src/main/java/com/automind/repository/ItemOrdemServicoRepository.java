package com.automind.repository;

import com.automind.domain.entity.ItemOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório dos itens de uma OS.
 *
 * Usado para persistir um item recém-criado e obter seu ID imediatamente
 * (necessário ao adicionar item a uma OS já existente/gerenciada — evita o
 * TransientObjectException que ocorre ao tentar fazer merge da OS gerenciada).
 */
@Repository
public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServico, Long> {
}
