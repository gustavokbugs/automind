# AutoMind — Documentação para Apresentação

**Sistema de Gestão de Oficina Automotiva Inteligente**
Projeto Final de Fábrica de Software · Spring Boot + React

---

## 1. O que é o AutoMind

O AutoMind é uma plataforma web completa para gestão de oficinas automotivas de
pequeno e médio porte. Ele digitaliza toda a operação da oficina — do cadastro de
clientes até o fechamento das ordens de serviço — e adiciona inteligência por meio
de um **motor de recomendações automático** e de um **Portal do Cliente** com
acompanhamento em tempo real.

### Problema que resolve
- Ordens de serviço em papel, fáceis de perder
- Histórico do veículo disperso ou inexistente
- Manutenções preventivas esquecidas (perda de receita)
- Estoque de peças sem controle
- Falta de métricas para decisão gerencial

### Solução
Uma plataforma integrada que centraliza clientes, veículos, peças, mecânicos e
ordens de serviço, gera recomendações preventivas automaticamente, controla
estoque com alertas e exibe um dashboard com métricas em tempo real.

---

## 2. Funcionalidades do Sistema

| # | Módulo | Descrição |
|---|--------|-----------|
| 1 | **Autenticação JWT** | Login stateless com 3 perfis de acesso (ADMIN, ATENDENTE, MECANICO) |
| 2 | **Gestão de Clientes** | CRUD completo, validação de CPF, busca, paginação, inativação lógica |
| 3 | **Gestão de Veículos** | Vínculo com cliente, placa única, quilometragem, histórico de atendimentos |
| 4 | **Gestão de Peças/Estoque** | Controle de estoque, alerta de nível mínimo, ajuste de quantidade |
| 5 | **Gestão de Mecânicos** | Cadastro de técnicos, especialidade, valor/hora |
| 6 | **Catálogo de Serviços** | Serviços padrão da oficina com preço-base e tempo estimado |
| 7 | **Ordens de Serviço (OS)** | Abertura, itens (serviços + peças), mudança de status, conclusão, baixa de estoque automática |
| 8 | **Motor de Recomendações** | Sugestões de manutenção preventiva geradas automaticamente (Strategy Pattern) |
| 9 | **Dashboard** | Métricas em tempo real: OS abertas, faturamento mensal, estoque baixo, serviços mais realizados |
| 10 | **Portal do Cliente** | Página pública (sem login) para acompanhar a OS em tempo real |

### Detalhe — Portal do Cliente (diferencial do projeto)
Quando uma OS é aberta, o sistema gera um **link único e seguro** (token UUID). O
cliente acessa esse link e vê:
- Uma **linha do tempo** com 6 etapas que avançam conforme o serviço
- O **orçamento**, que ele pode **aprovar com um clique**
- **Fotos e vídeos** enviados pelo mecânico em tempo real (via SSE)
- Ao concluir, uma **explicação dos serviços em linguagem simples gerada por IA**

---

## 3. Arquitetura do Backend

### 3.1 Stack tecnológica
- **Java 17** + **Spring Boot 3.2.3**
- **Spring Data JPA** (acesso ao banco) + **PostgreSQL 16**
- **Spring Security** + **JWT** (autenticação stateless)
- **Lombok** (redução de código repetitivo)
- **Swagger/OpenAPI** (documentação automática da API)
- **JUnit 5 + Mockito** (testes)
- **Docker / Docker Compose** (orquestração)

### 3.2 Arquitetura em camadas

O backend segue o padrão de **arquitetura em camadas** (layered architecture). Cada
requisição percorre as camadas de cima para baixo, e cada camada tem uma única
responsabilidade:

```
   Cliente HTTP (navegador / app)
            │
            ▼
   ┌──────────────────┐
   │   CONTROLLER      │  Recebe a requisição HTTP, valida e responde (JSON)
   └──────────────────┘
            │
            ▼
   ┌──────────────────┐
   │     SERVICE       │  Contém TODA a regra de negócio (única camada que decide)
   └──────────────────┘
            │
            ▼
   ┌──────────────────┐
   │   REPOSITORY      │  Faz as queries no banco (gerado pelo Spring Data JPA)
   └──────────────────┘
            │
            ▼
   ┌──────────────────┐
   │  BANCO (Postgres) │
   └──────────────────┘
```

Camadas de apoio que atravessam todo o fluxo:
- **DTO** (Data Transfer Object): objetos de entrada (`request`) e saída (`response`)
  que separam o contrato da API das entidades do banco.
- **Domain/Entity**: as classes mapeadas para tabelas (JPA).
- **Security**: o filtro JWT que intercepta requisições antes de chegar ao controller.
- **Exception**: tratamento centralizado de erros.
- **Config**: configurações de inicialização (segurança, swagger, dados iniciais).

### 3.3 Princípios e padrões aplicados
- **SOLID** — especialmente Single Responsibility (cada classe uma função) e
  Open/Closed (motor de recomendações extensível sem alterar código).
- **Strategy Pattern** — cada tipo de recomendação é uma estratégia independente.
- **Dependency Injection** — o Spring injeta as dependências automaticamente.
- **DTO Pattern** — nunca expõe a entidade do banco diretamente na API.

---

## 4. Estrutura de Pastas do Backend

```
com.automind
├── AutoMindApplication.java        ← ponto de entrada da aplicação
├── config/                         ← configurações (segurança, swagger, etc.)
├── controller/                     ← endpoints REST (camada de entrada)
├── domain/
│   ├── entity/                     ← entidades JPA (tabelas do banco)
│   └── enums/                      ← enumerações (status, perfis, tipos)
├── dto/
│   ├── request/                    ← objetos de entrada da API
│   └── response/                   ← objetos de saída da API
├── exception/                      ← exceções e tratamento global de erros
├── repository/                     ← interfaces de acesso ao banco (JPA)
├── security/                       ← autenticação JWT
├── service/                        ← regras de negócio
│   └── recommendation/             ← motor de recomendações (Strategy Pattern)
└── util/                           ← utilitários (CPF, número de OS)
```

---

## 5. Função de Cada Arquivo Java

### 5.1 Raiz

| Arquivo | Função |
|---------|--------|
| `AutoMindApplication.java` | Classe principal anotada com `@SpringBootApplication`. Inicia toda a aplicação Spring Boot (`main`). |

### 5.2 config/ — Configurações

| Arquivo | Função |
|---------|--------|
| `SecurityConfig.java` | Configura o Spring Security: define rotas públicas (`/auth`, `/public`, `/uploads`, swagger) e protegidas, ativa CORS, registra o filtro JWT, define sessão como STATELESS e o codificador de senha BCrypt. |
| `UserDetailsConfig.java` | Define o `UserDetailsService` — como o Spring carrega um usuário pelo e-mail durante a autenticação (busca no `UsuarioRepository`). |
| `JwtAuthFilter` (em security/) | *(ver seção segurança)* |
| `SwaggerConfig.java` | Configura a documentação OpenAPI/Swagger e o esquema de autenticação Bearer JWT na interface `/swagger-ui.html`. |
| `DataInitializer.java` | Executa no start da aplicação (`CommandLineRunner`) e cria o usuário admin padrão (`admin@automind.com` / `admin123`) se ainda não existir. |
| `WebConfig.java` | Registra o diretório de uploads como recurso estático, permitindo acesso às fotos/vídeos via URL `/uploads/{arquivo}`. |

### 5.3 controller/ — Endpoints REST

| Arquivo | Endpoint base | Função |
|---------|---------------|--------|
| `AuthController.java` | `/auth` | `POST /auth/login` — autentica o usuário e devolve o token JWT. |
| `ClienteController.java` | `/clientes` | CRUD de clientes. Usa `@PreAuthorize` para restringir por perfil (criar/editar = ADMIN/ATENDENTE; inativar = ADMIN). |
| `VeiculoController.java` | `/veiculos` | CRUD de veículos e listagem por cliente. |
| `PecaController.java` | `/pecas` | CRUD de peças e ajuste de estoque. |
| `MecanicoController.java` | `/mecanicos` | CRUD de mecânicos. |
| `ServicoController.java` | `/servicos` | Listagem do catálogo de serviços. |
| `OrdemServicoController.java` | `/ordens-servico` | Abertura, busca, listagem (com filtro de status), atualização de status, conclusão e histórico por veículo. |
| `DashboardController.java` | `/dashboard` | Retorna as métricas consolidadas para a tela inicial. |
| `RecomendacaoController.java` | `/recomendacoes` | Gera e lista recomendações de manutenção por veículo; marca como visualizada. |
| `OsPublicaController.java` | `/public/os` | **Portal do Cliente (sem autenticação)**: busca dados da OS por token, stream SSE de eventos em tempo real e aprovação de orçamento. |
| `UploadController.java` | `/ordens-servico/{id}/midias` | Upload de fotos e vídeos pelo mecânico, vinculados à OS. |

### 5.4 domain/entity/ — Entidades (tabelas do banco)

| Arquivo | Função |
|---------|--------|
| `Usuario.java` | Usuário do sistema (login). Implementa `UserDetails` do Spring Security. Tem perfil (ADMIN/ATENDENTE/MECANICO). |
| `Cliente.java` | Cliente da oficina (nome, CPF, e-mail, telefone). Tem lista de veículos. Inativação lógica via campo `ativo`. |
| `Veiculo.java` | Veículo vinculado a um cliente (placa, marca, modelo, ano, quilometragem). Tem histórico de OS. |
| `Mecanico.java` | Técnico da oficina (especialidade, valor/hora). |
| `Servico.java` | Serviço do catálogo (nome, tipo, preço-base, tempo estimado). |
| `Peca.java` | Peça do estoque (código, preços, quantidade, estoque mínimo). Método `isEstoqueBaixo()`. |
| `OrdemServico.java` | **Entidade central.** Liga veículo + mecânico + itens. Controla status, valores, token público, aprovação de orçamento e explicação por IA. Métodos de negócio: `calcularTotal()`, `concluir()`, `aprovarOrcamento()`. |
| `ItemOrdemServico.java` | Item de uma OS (um serviço OU uma peça, com quantidade e preço). Calcula `getSubtotal()`. |
| `RecomendacaoManutencao.java` | Recomendação preventiva gerada para um veículo (tipo, descrição, urgência). |
| `MidiaOS.java` | Foto ou vídeo enviado pelo mecânico, vinculado a uma OS (usado no Portal do Cliente). |

### 5.5 domain/enums/ — Enumerações

| Arquivo | Função |
|---------|--------|
| `PerfilUsuario.java` | Perfis de acesso: `ADMIN`, `MECANICO`, `ATENDENTE`. |
| `StatusOS.java` | Estados da OS: `ABERTA`, `EM_ANDAMENTO`, `AGUARDANDO_PECA`, `EM_FINALIZACAO`, `CONCLUIDA`, `CANCELADA`. Cada um mapeia uma etapa no Portal do Cliente. |
| `TipoServico.java` | Tipos de serviço (troca de óleo, correia, freio, pneu, revisão, etc.). |

### 5.6 dto/request/ — Objetos de entrada

| Arquivo | Função |
|---------|--------|
| `LoginRequest.java` | E-mail e senha do login. |
| `ClienteRequest.java` | Dados para criar/editar cliente, com validações (CPF, e-mail, telefone só dígitos). |
| `VeiculoRequest.java` | Dados para criar/editar veículo. |
| `PecaRequest.java` | Dados para criar/editar peça. |
| `MecanicoRequest.java` | Dados para criar/editar mecânico. |
| `OrdemServicoRequest.java` | Dados para abrir uma OS, incluindo a lista de itens (classe aninhada `ItemOSRequest`). |

### 5.7 dto/response/ — Objetos de saída

| Arquivo | Função |
|---------|--------|
| `ApiResponse.java` | **Envelope padrão de TODAS as respostas** da API: `sucesso`, `mensagem`, `dados`, `timestamp`. Métodos estáticos `ok()` e `erro()`. |
| `AuthResponse.java` | Resposta do login: token, tipo, dados do usuário. |
| `ClienteResponse.java` | Dados de cliente devolvidos pela API (com CPF formatado e total de veículos). |
| `VeiculoResponse.java` | Dados de veículo devolvidos pela API. |
| `OrdemServicoResponse.java` | Dados completos da OS (itens, valores, token público). |
| `DashboardResponse.java` | Métricas do dashboard (classes aninhadas `PecaEstoqueBaixo` e `ServicoPopular`). |
| `OsPublicaDTO.java` | **Dados PÚBLICOS da OS** para o Portal do Cliente — sem expor dados sensíveis (custo de peças, dados de outros clientes). |

### 5.8 exception/ — Tratamento de erros

| Arquivo | Função |
|---------|--------|
| `ResourceNotFoundException.java` | Exceção para "recurso não encontrado" → retorna HTTP 404. |
| `BusinessException.java` | Exceção para violação de regra de negócio (ex: CPF duplicado) → retorna HTTP 422. |
| `GlobalExceptionHandler.java` | **Captura centralizada de exceções** (`@RestControllerAdvice`). Converte cada tipo de erro no HTTP status correto e no envelope `ApiResponse`. Trata validação, credenciais inválidas, acesso negado e erros genéricos. |

### 5.9 repository/ — Acesso ao banco

Todos estendem `JpaRepository` — o Spring Data JPA **gera a implementação
automaticamente** a partir do nome dos métodos.

| Arquivo | Função |
|---------|--------|
| `UsuarioRepository.java` | Busca usuário por e-mail; verifica existência. |
| `ClienteRepository.java` | Busca/filtra clientes, conta ativos, valida CPF/e-mail únicos. |
| `VeiculoRepository.java` | Busca veículos, valida placa única, lista por cliente. |
| `PecaRepository.java` | Busca peças, valida código, lista as de estoque baixo. |
| `MecanicoRepository.java` | Busca mecânicos ativos, valida CPF. |
| `ServicoRepository.java` | Lista serviços ativos e os mais realizados (dashboard). |
| `OrdemServicoRepository.java` | Busca por número/token, filtra por status, calcula faturamento, busca última data de serviço (recomendações). |
| `RecomendacaoRepository.java` | Lista/ordena recomendações por veículo; remove as não visualizadas. |
| `MidiaOSRepository.java` | Lista as mídias de uma OS ordenadas por data de envio. |

### 5.10 security/ — Autenticação JWT

| Arquivo | Função |
|---------|--------|
| `JwtService.java` | **Gera e valida tokens JWT.** Cria o token no login (com username + expiração), extrai o usuário do token e verifica validade/expiração usando a chave secreta. |
| `JwtAuthFilter.java` | **Filtro executado em toda requisição** (`OncePerRequestFilter`). Lê o header `Authorization: Bearer ...`, valida o token e, se válido, coloca o usuário autenticado no contexto do Spring Security. |

### 5.11 service/ — Regras de negócio

| Arquivo | Função |
|---------|--------|
| `AuthService.java` | Realiza o login (autentica e gera token) e cria o usuário admin inicial. |
| `ClienteService.java` | Regras de cliente: valida CPF/e-mail únicos, cria, atualiza, lista, inativa. Converte entidade ↔ DTO. |
| `VeiculoService.java` | Regras de veículo: normaliza placa, valida unicidade, vincula ao cliente. |
| `PecaService.java` | Regras de peça: valida código, lista estoque baixo, **ajusta estoque** (validando que não fica negativo). |
| `MecanicoService.java` | Regras de mecânico: valida CPF, cria, atualiza, inativa. |
| `ServicoService.java` | Lista serviços ativos e busca por ID. |
| `OrdemServicoService.java` | **Serviço mais complexo.** Abre OS (gera número e token), monta itens, calcula total, muda status, conclui OS (baixa estoque + atualiza km + dispara IA), notifica o Portal via SSE. |
| `DashboardService.java` | Consolida as métricas: conta clientes/veículos/OS, soma faturamento do mês, lista estoque baixo e serviços mais realizados. |
| `RecomendacaoService.java` | Coordena o motor de recomendações: reúne o histórico do veículo, chama o motor e salva as recomendações geradas. |
| `OsPublicaService.java` | Lógica do **Portal do Cliente**: busca OS por token, registra aprovação de orçamento e converte para o DTO público (mascarando dados sensíveis). |
| `SseEmitterService.java` | Gerencia conexões **SSE** (Server-Sent Events). Mantém os clientes conectados e envia eventos em tempo real quando o status/conteúdo da OS muda. |
| `IaExplicacaoService.java` | Chama a **API de IA** (Groq/Llama 3) ao concluir a OS para gerar a explicação dos serviços em linguagem simples. Tem texto de fallback se não houver chave configurada. |

### 5.12 service/recommendation/ — Motor de Recomendações (Strategy Pattern)

| Arquivo | Função |
|---------|--------|
| `RecomendacaoStrategy.java` | **Interface** que define o contrato `avaliar(veiculo, servicos)`. Toda estratégia a implementa. |
| `MotorRecomendacoes.java` | **Orquestrador.** O Spring injeta automaticamente todas as estratégias numa lista; o motor percorre cada uma e coleta as recomendações aplicáveis. |
| `TrocaOleoStrategy.java` | Recomenda troca de óleo por km (10.000) ou tempo (6 meses). |
| `CorreiaDentadaStrategy.java` | Recomenda troca de correia dentada por quilometragem. |
| `PastilhaFreioStrategy.java` | Recomenda troca de pastilhas de freio. |
| `TrocaPneuStrategy.java` | Recomenda troca/rodízio de pneus. |
| `RevisaoGeralStrategy.java` | Recomenda revisão geral periódica. |

> **Por que Strategy Pattern?** Para adicionar um novo tipo de recomendação, basta
> criar uma nova classe que implementa `RecomendacaoStrategy`. O motor a inclui
> automaticamente, **sem alterar nenhum código existente** (princípio Open/Closed).

### 5.13 util/ — Utilitários

| Arquivo | Função |
|---------|--------|
| `CpfUtils.java` | Limpa (remove máscara) e formata CPFs. Classe utilitária estática. |
| `NumeroOSGenerator.java` | Gera o número legível da OS no formato `OS-AAAAMMDD-NNNN`. |

---

## 6. Fluxo de Exemplo — Abrir e Concluir uma OS

Esse fluxo é ótimo para demonstrar a arquitetura na apresentação:

1. **Atendente** faz login → `AuthController` → `AuthService` → recebe token JWT.
2. Abre uma OS → `OrdemServicoController.criar()` → `OrdemServicoService.criar()`:
   - Gera número (`NumeroOSGenerator`) e token público (UUID)
   - Monta os itens (serviços + peças)
   - Calcula o total
   - Salva via `OrdemServicoRepository`
3. O link do **Portal do Cliente** (`/os/{token}`) é gerado e enviado ao cliente.
4. **Mecânico** atualiza o status e envia fotos → `SseEmitterService` notifica o
   cliente em tempo real.
5. **Cliente** aprova o orçamento pelo portal → `OsPublicaService.aprovarOrcamento()`.
6. Mecânico **conclui** a OS → `OrdemServicoService.concluir()`:
   - Dá baixa no estoque de peças (`PecaService.ajustarEstoque`)
   - Atualiza a quilometragem do veículo
   - Chama a **IA** (`IaExplicacaoService`) para gerar a explicação
   - Notifica o cliente via SSE
7. O **motor de recomendações** pode então sugerir a próxima manutenção preventiva.

---

## 7. Resumo para os Slides

- **3 camadas:** Controller (entra) → Service (decide) → Repository (persiste).
- **DTOs** isolam a API do banco; **`ApiResponse`** padroniza toda resposta.
- **Segurança:** JWT stateless + 3 perfis + filtro em toda requisição.
- **Padrão de destaque:** Strategy Pattern no motor de recomendações.
- **Diferenciais:** Portal do Cliente público com SSE (tempo real) + IA + upload de mídias.
- **Qualidade:** tratamento global de erros, validações, testes, Docker.
