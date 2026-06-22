-- ============================================
-- AutoMind — Schema PostgreSQL
-- ============================================

CREATE TABLE IF NOT EXISTS usuarios (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    senha       VARCHAR(255) NOT NULL,
    perfil      VARCHAR(20) NOT NULL CHECK (perfil IN ('ADMIN','MECANICO','ATENDENTE')),
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS clientes (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    cpf           VARCHAR(14) NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    telefone      VARCHAR(20) NOT NULL,
    endereco      VARCHAR(200),
    ativo         BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cliente_cpf   ON clientes(cpf);
CREATE INDEX IF NOT EXISTS idx_cliente_email ON clientes(email);

CREATE TABLE IF NOT EXISTS veiculos (
    id                   BIGSERIAL PRIMARY KEY,
    placa                VARCHAR(10) NOT NULL UNIQUE,
    marca                VARCHAR(50) NOT NULL,
    modelo               VARCHAR(80) NOT NULL,
    ano                  INTEGER NOT NULL,
    cor                  VARCHAR(20) NOT NULL,
    quilometragem_atual  INTEGER NOT NULL DEFAULT 0,
    chassis              VARCHAR(17),
    cliente_id           BIGINT NOT NULL REFERENCES clientes(id),
    criado_em            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_veiculo_placa ON veiculos(placa);

CREATE TABLE IF NOT EXISTS mecanicos (
    id           BIGSERIAL PRIMARY KEY,
    nome         VARCHAR(100) NOT NULL,
    cpf          VARCHAR(14) NOT NULL UNIQUE,
    telefone     VARCHAR(20) NOT NULL,
    especialidade VARCHAR(100),
    valor_hora   NUMERIC(10,2),
    ativo        BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS servicos (
    id                    BIGSERIAL PRIMARY KEY,
    nome                  VARCHAR(150) NOT NULL,
    descricao             VARCHAR(500),
    tipo                  VARCHAR(30) NOT NULL,
    preco_base            NUMERIC(10,2) NOT NULL,
    tempo_estimado_horas  NUMERIC(4,1),
    ativo                 BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em             TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pecas (
    id                  BIGSERIAL PRIMARY KEY,
    codigo              VARCHAR(50) NOT NULL UNIQUE,
    nome                VARCHAR(150) NOT NULL,
    descricao           VARCHAR(300),
    preco_compra        NUMERIC(10,2) NOT NULL,
    preco_venda         NUMERIC(10,2) NOT NULL,
    quantidade_estoque  INTEGER NOT NULL DEFAULT 0,
    estoque_minimo      INTEGER NOT NULL DEFAULT 0,
    fabricante          VARCHAR(80),
    ativo               BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em           TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em       TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_peca_codigo ON pecas(codigo);

CREATE TABLE IF NOT EXISTS ordens_servico (
    id                    BIGSERIAL PRIMARY KEY,
    numero                VARCHAR(20) NOT NULL UNIQUE,

    -- Token UUID público — exposto na URL do Portal do Cliente
    -- Nunca expõe o ID interno ao cliente
    token_publico         VARCHAR(36) NOT NULL UNIQUE DEFAULT gen_random_uuid()::text,

    status                VARCHAR(20) NOT NULL DEFAULT 'ABERTA'
        CHECK (status IN ('ABERTA','EM_ANDAMENTO','AGUARDANDO_PECA','EM_FINALIZACAO','CONCLUIDA','CANCELADA')),

    veiculo_id            BIGINT NOT NULL REFERENCES veiculos(id),
    mecanico_id           BIGINT REFERENCES mecanicos(id),
    quilometragem_entrada INTEGER NOT NULL,
    quilometragem_saida   INTEGER,
    observacoes           VARCHAR(1000),
    diagnostico           VARCHAR(2000),
    valor_total           NUMERIC(12,2) DEFAULT 0,
    desconto              NUMERIC(10,2) DEFAULT 0,

    -- Aprovação do orçamento pelo cliente no Portal
    orcamento_aprovado    BOOLEAN NOT NULL DEFAULT FALSE,
    orcamento_aprovado_em TIMESTAMP,

    -- Explicação dos serviços gerada por IA ao concluir a OS
    -- Salva no banco — não é regerada a cada acesso
    explicacao_cliente    VARCHAR(3000),

    aberta_em             TIMESTAMP NOT NULL DEFAULT NOW(),
    concluida_em          TIMESTAMP,
    previsao_entrega      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_os_status     ON ordens_servico(status);
CREATE INDEX IF NOT EXISTS idx_os_veiculo    ON ordens_servico(veiculo_id);
CREATE INDEX IF NOT EXISTS idx_os_aberta_em  ON ordens_servico(aberta_em);

CREATE TABLE IF NOT EXISTS itens_ordem_servico (
    id               BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    servico_id       BIGINT REFERENCES servicos(id),
    peca_id          BIGINT REFERENCES pecas(id),
    quantidade       INTEGER NOT NULL DEFAULT 1,
    preco_unitario   NUMERIC(10,2) NOT NULL,
    desconto         NUMERIC(5,2) DEFAULT 0,
    observacao       VARCHAR(500),
    CONSTRAINT chk_item_servico_ou_peca CHECK (servico_id IS NOT NULL OR peca_id IS NOT NULL)
);

-- Tabela de mídias (fotos e vídeos) enviadas pelo mecânico durante a OS
-- Exibidas na linha do tempo do Portal do Cliente (Etapa 3)
CREATE TABLE IF NOT EXISTS midias_os (
    id               BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    nome_arquivo     VARCHAR(255) NOT NULL,
    url              VARCHAR(500) NOT NULL,
    tipo             VARCHAR(10) NOT NULL CHECK (tipo IN ('FOTO','VIDEO')),
    legenda          VARCHAR(300),
    enviada_em       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_midia_os_id ON midias_os(ordem_servico_id);

CREATE TABLE IF NOT EXISTS recomendacoes_manutencao (
    id               BIGSERIAL PRIMARY KEY,
    veiculo_id       BIGINT NOT NULL REFERENCES veiculos(id),
    tipo_servico     VARCHAR(30) NOT NULL,
    descricao        VARCHAR(200) NOT NULL,
    km_recomendado   INTEGER,
    data_recomendada DATE,
    urgente          BOOLEAN NOT NULL DEFAULT FALSE,
    visualizada      BOOLEAN NOT NULL DEFAULT FALSE,
    gerada_em        TIMESTAMP NOT NULL DEFAULT NOW()
);
