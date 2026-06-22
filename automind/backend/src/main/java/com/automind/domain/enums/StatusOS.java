package com.automind.domain.enums;

/**
 * Representa os possíveis estados de uma Ordem de Serviço.
 *
 * O fluxo normal é:
 *   ABERTA → EM_ANDAMENTO → EM_FINALIZACAO → CONCLUIDA
 *
 * O estado AGUARDANDO_PECA pode ocorrer em qualquer momento
 * durante EM_ANDAMENTO quando uma peça precisa ser pedida.
 *
 * No Portal do Cliente cada status mapeia para uma etapa visual
 * da linha do tempo exibida na página pública da OS.
 */
public enum StatusOS {

    /** Etapa 1 — OS recém aberta, veículo deu entrada */
    ABERTA,

    /** Etapa 2/3 — Diagnóstico em andamento ou serviço sendo executado */
    EM_ANDAMENTO,

    /** Etapa 4 — Pausado aguardando chegada de peça do fornecedor */
    AGUARDANDO_PECA,

    /** Etapa 5 — Serviço finalizado, passando por teste e higienização */
    EM_FINALIZACAO,

    /** Etapa 6 — Veículo pronto para retirada */
    CONCLUIDA,

    /** OS cancelada — não deve aparecer no portal do cliente */
    CANCELADA
}
