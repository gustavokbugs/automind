# AutoMind — Sistema de Gestão de Oficina Automotiva Inteligente

> Projeto Final de Fábrica de Software — Desenvolvimento Full Stack Profissional

[![CI](https://github.com/seu-usuario/automind/actions/workflows/ci.yml/badge.svg)](https://github.com/seu-usuario/automind/actions)

---

## Visão Geral

O **AutoMind** é uma plataforma web completa para gestão de oficinas automotivas, com foco em produtividade, rastreabilidade e inteligência preventiva. O sistema combina tecnologias modernas com padrões de projeto avançados para oferecer uma solução profissional e escalável.

### Diferenciais

- **Motor de Recomendações** com **Strategy Pattern**: análise automática do histórico e quilometragem para sugerir manutenções preventivas
- **API REST** padronizada com paginação, filtros e tratamento global de exceções
- **Arquitetura em camadas** seguindo princípios SOLID e Clean Code
- **Testes automatizados** com JUnit 5 e Mockito
- **Docker** para ambiente reproduzível com um único comando

---

## Tecnologias

### Backend
| Tecnologia | Versão | Função |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.2 | Framework web |
| Spring Security + JWT | — | Autenticação |
| Spring Data JPA | — | Persistência |
| PostgreSQL | 16 | Banco de dados |
| Swagger/OpenAPI | 2.3 | Documentação da API |
| JUnit 5 + Mockito | — | Testes automatizados |
| Maven | 3.9 | Build |

### Frontend
| Tecnologia | Versão | Função |
|---|---|---|
| React | 18 | UI |
| Vite | 5 | Build tool |
| Tailwind CSS | 3 | Estilização |
| Axios | 1.6 | HTTP Client |
| React Router | 6 | Navegação |
| React Query | 5 | Cache de dados |

---

## Arquitetura

```
automind/
├── backend/
│   └── src/main/java/com/automind/
│       ├── controller/      → Endpoints REST
│       ├── service/         → Regras de negócio
│       │   └── recommendation/ → Strategy Pattern
│       ├── repository/      → Acesso a dados (JPA)
│       ├── domain/
│       │   ├── entity/      → Entidades JPA
│       │   └── enums/       → Enumerações
│       ├── dto/             → Data Transfer Objects
│       ├── security/        → JWT Filter e Service
│       ├── config/          → Spring Config
│       ├── exception/       → Exceções e Handler global
│       └── util/            → Utilitários
│
└── frontend/
    └── src/
        ├── pages/           → Telas do sistema
        ├── components/      → Componentes reutilizáveis
        ├── services/        → Camada de API (Axios)
        └── context/         → Estado global (Auth)
```

---

## Modelagem — Diagrama ER

```
CLIENTES ──< VEICULOS >── ORDENS_SERVICO ──< ITENS_ORDEM_SERVICO
                │                │
                │                ├── SERVICOS
                │                └── PECAS
                └── RECOMENDACOES_MANUTENCAO

MECANICOS ──< ORDENS_SERVICO
USUARIOS (autenticação independente)
```

---

## Strategy Pattern — Motor de Recomendações

```
         ┌─────────────────────────┐
         │   RecomendacaoStrategy  │  ← Interface
         └─────────┬───────────────┘
                   │
     ┌─────────────┼──────────────────────────┐
     ▼             ▼              ▼            ▼
TrocaOleo  CorreiaDentada  PastilhaFreio  TrocaPneu
Strategy      Strategy        Strategy    Strategy
 (10k km)     (60k km)        (30k km)    (50k km)

         ┌───────────────────────────┐
         │      MotorRecomendacoes   │
         │  List<Strategy>.stream()  │
         │    .map(s -> s.avaliar()) │
         └───────────────────────────┘
```

---

## Funcionalidades

| Módulo | Funcionalidades |
|---|---|
| **Autenticação** | Login JWT, perfis ADMIN/MECANICO/ATENDENTE |
| **Clientes** | CRUD, validação CPF/email, paginação e busca |
| **Veículos** | Vinculados a clientes, placa única, km atual |
| **Peças** | Estoque, alerta de mínimo, ajuste manual |
| **Ordens de Serviço** | Abertura, andamento, conclusão, histórico |
| **Recomendações** | Motor automático por km e tempo |
| **Dashboard** | Métricas em tempo real |

---

## Como executar

### Com Docker (recomendado)

```bash
# Clone o projeto
git clone https://github.com/seu-usuario/automind.git
cd automind

# Suba todos os serviços
docker-compose up --build

# Acesse:
# Frontend → http://localhost:3000
# Backend API → http://localhost:8080/api
# Swagger → http://localhost:8080/api/swagger-ui.html
```

**Login padrão:** `admin@automind.com` / `admin123`

### Desenvolvimento local

**Backend:**
```bash
cd backend

# Configure PostgreSQL local ou use o Docker apenas para o banco:
docker run -d --name automind-db -e POSTGRES_DB=automind -e POSTGRES_USER=automind -e POSTGRES_PASSWORD=automind123 -p 5432:5432 postgres:16-alpine

# Execute o schema
psql -U automind -d automind -f src/main/resources/db/schema.sql
psql -U automind -d automind -f src/main/resources/db/data.sql

# Rode o backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
# Acesse http://localhost:3000
```

### Testes

```bash
cd backend
mvn test               # Todos os testes
mvn test -pl backend   # Apenas backend
```

---

## Endpoints principais

```
POST /api/auth/login              → Login, retorna JWT

GET  /api/clientes                → Listar (paginado + filtro)
POST /api/clientes                → Criar
PUT  /api/clientes/{id}           → Atualizar
DEL  /api/clientes/{id}           → Inativar

GET  /api/veiculos                → Listar
POST /api/veiculos                → Criar
GET  /api/veiculos/cliente/{id}   → Por cliente

GET  /api/ordens-servico          → Listar (filtro por status)
POST /api/ordens-servico          → Abrir OS
PATCH /api/ordens-servico/{id}/status → Alterar status
POST /api/ordens-servico/{id}/concluir → Concluir

POST /api/recomendacoes/veiculo/{id}/gerar → Gerar recomendações
GET  /api/recomendacoes/veiculo/{id}       → Listar

GET  /api/dashboard               → Métricas gerais
```

---

## Testes automatizados

Os testes cobrem:

- `ClienteServiceTest` — CRUD, validações, duplicatas
- `OrdemServicoServiceTest` — Abertura, conclusão, regras de negócio
- `MotorRecomendacoesTest` — Todas as strategies do padrão Strategy

```bash
mvn test
# Relatórios em: backend/target/surefire-reports/
```

---

## Pitch — Resumo para apresentação

### Problema
Oficinas automotivas perdem clientes e receita por falta de controle e proatividade. Sem sistema: ordens em papel, histórico perdido, manutenções preventivas esquecidas.

### Solução — AutoMind
Plataforma integrada que digitaliza a operação da oficina e usa inteligência baseada em histórico para recomendar manutenções proativamente.

### Diferenciais técnicos
1. **Strategy Pattern** — motor de recomendações extensível sem modificar código existente (OCP)
2. **JWT Stateless** — segurança escalável com perfis de acesso
3. **API REST** padronizada com Swagger — pronta para integrações
4. **Docker** — implantação reproduzível em qualquer ambiente
5. **CI/CD** — GitHub Actions valida todo push automaticamente

### Fluxo principal
`Login → Dashboard → Cadastrar Cliente → Cadastrar Veículo → Abrir OS → Fechar OS → Ver Recomendações`

---

## Diagrama de Classes (principais)

```
Cliente ──has── List<Veiculo>
Veiculo ──has── List<OrdemServico>
Veiculo ──has── List<RecomendacaoManutencao>
OrdemServico ──has── List<ItemOrdemServico>
ItemOrdemServico ──ref── Servico
ItemOrdemServico ──ref── Peca
OrdemServico ──ref── Mecanico

<<interface>> RecomendacaoStrategy
    + avaliar(Veiculo, List<String>): Optional<Recomendacao>

TrocaOleoStrategy implements RecomendacaoStrategy
CorreiaDentadaStrategy implements RecomendacaoStrategy
PastilhaFreioStrategy implements RecomendacaoStrategy
TrocaPneuStrategy implements RecomendacaoStrategy
RevisaoGeralStrategy implements RecomendacaoStrategy

MotorRecomendacoes ──uses── List<RecomendacaoStrategy>
```

---

*Desenvolvido como Projeto Final de Fábrica de Software — Demonstração de Full Stack com Java, Spring Boot, React e PostgreSQL.*
