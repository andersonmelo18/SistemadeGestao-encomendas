# Implementação de encomendas

## Identificação do projeto

Nome Maven e Spring: `sistemadegestao`. Pacote base: `br.edu.uniesp.sistemadegestao`. Classe principal: `SistemaDeGestaoApplication`.

No IntelliJ, recarregue o projeto Maven após a refatoração e execute essa classe. Se a IDE mantiver uma configuração antiga em memória, atualize sua Main class para `br.edu.uniesp.sistemadegestao.SistemaDeGestaoApplication` e selecione o módulo `sistemadegestao`.

A pasta física do checkout é `C:\Users\AndersoN\Documents\SistemadeGestao-encomendas`. O endereço do repositório Git e os nomes do banco/volume Docker existente foram preservados; a refatoração não migra dados ou recria a infraestrutura.

## Etapa 1 — clientes e acesso

Implementados cadastro de cliente com acesso vinculado, autenticação por celular/senha, hash BCrypt, autorização de rotas e criação de usuários internos pelo administrador. Os módulos seguem as camadas do projeto. CPF e e-mail são opcionais para novos clientes; dados antigos são preservados pela migração V13.

O módulo de pedidos e suas transições está implementado na etapa 2, descrita abaixo. Os painéis visuais, atualização em tempo real, entrega e dashboard ainda não foram implementados.

## Executar localmente

Requer JDK 21 e Maven (ou o wrapper `mvnw.cmd`). Configure `APP_ADMIN_CELULAR` e `APP_ADMIN_SENHA` no ambiente antes de iniciar com `mvnw.cmd spring-boot:run`. O celular deve conter DDD e número, somente 10 ou 11 dígitos, sem código do país. A senha deve ter pelo menos 8 caracteres e até 72 bytes UTF-8.

O administrador inicial é criado somente quando a tabela de usuários está vazia. Não existe senha padrão e reiniciar não redefine credenciais. Se houver usuários legados sem celular, será necessária uma migração de acesso antes de usar essas contas. Remova as variáveis de bootstrap após provisionar o administrador em um banco persistente.

O perfil local usa H2 em memória: os dados desaparecem ao encerrar a aplicação. Flyway cria o esquema e Hibernate valida sua compatibilidade; a console H2 fica desativada. Fora do perfil local, configure PostgreSQL com `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` e selecione outro perfil via `SPRING_PROFILES_ACTIVE`.

## Contrato inicial da API

Autenticação nesta etapa: HTTP Basic, usando celular e senha. Use HTTPS fora do desenvolvimento local. A futura interface ainda precisa implementar seu fluxo de login e encerramento de acesso.

| Rota | Acesso e finalidade |
| --- | --- |
| `GET /api/acesso/csrf` | Obtém token CSRF; conservar o cookie da sessão. |
| `GET /api/acesso/me` | Usuário autenticado: retorna identificação, perfil e ID do cliente vinculado. |
| `POST /api/usuarios` | Administrador: cria ADMIN, ATENDENTE, PRODUCAO ou ENTREGA. |
| `/clientes` e `/clientes/**` | Administrador e atendente: cadastro, consulta, atualização, ativação e inativação. |
| Demais rotas existentes | Restritas ao administrador até a implementação das permissões específicas dos novos módulos. |

Para POST/PUT/PATCH, enviar o token CSRF no cabeçalho cujo nome é retornado por `/api/acesso/csrf`, junto ao cookie da mesma sessão. A autenticação Basic continua necessária em cada requisição protegida.

Exemplo de corpo de `POST /clientes`:

```json
{
  "nome": "Maria Silva",
  "celular": "83999990001",
  "senha": "substitua-por-uma-senha"
}
```

O atendente não escolhe o perfil do cliente: o backend atribui CLIENTE e vincula o acesso ao cadastro na mesma transação. As respostas não contêm senha ou hash. Celulares duplicados são rejeitados. Clientes inativos não autenticam; a reativação restaura o acesso se o usuário também estiver ativo.

`POST /api/usuarios` aceita `nome`, `celular`, `senha` e `perfil`. O perfil CLIENTE só pode ser criado pelo fluxo de cadastro de clientes. TECNICO permanece no enum por compatibilidade com dados antigos, sem criação no novo fluxo.

Atualização de cliente mantém o contrato existente de nome e e-mail; agora e-mail é opcional. Alteração de celular e redefinição de senha serão tratadas em uma próxima etapa.

## Etapa 2 — pedidos e produção

Módulo `internal/pedido` organizado em controller, dto, entity, mapper, repository e service. A migração V14 cria pedidos, itens e histórico, com índices de consulta por cliente e status. As tabelas da assistência técnica permanecem preservadas.

Cada pedido pertence a um cliente e contém de 1 a 100 itens. Nesta etapa, o produto é informado pela descrição no item, com seu preço naquela venda; não existe ainda um catálogo independente de produtos. Quantidades são inteiras entre 1 e 9.999, e valores unitários positivos possuem até duas casas decimais. Subtotais e total são calculados pelo backend com BigDecimal; o cliente da API não determina o total.

O pedido inteiro segue `EM_ANALISE → APROVADO → FILA → EM_PRODUCAO → FINALIZADO`. Não é permitido pular, repetir ou retroceder etapas. O histórico registra o lançamento inicial e cada transição, com data e usuário responsável. Finalizado significa produção concluída; o registro de entrega será implementado separadamente.

Atendente e administrador podem editar itens e observações até FILA, inclusive. Em EM_PRODUCAO e FINALIZADO, a edição é bloqueada inclusive para administrador. O vínculo com o cliente não é alterado pela edição. A produção e o administrador avançam etapas, enquanto o cliente apenas consulta.

| Rota | Permissão |
| --- | --- |
| `POST /api/pedidos` | ADMIN, ATENDENTE: cria em EM_ANALISE. |
| `GET /api/pedidos` | ADMIN, ATENDENTE, PRODUCAO: lista paginada, com filtro opcional `status`. |
| `GET /api/pedidos/{id}` | ADMIN, ATENDENTE, PRODUCAO: detalhes, itens e histórico. |
| `PUT /api/pedidos/{id}` | ADMIN, ATENDENTE: substitui integralmente itens e observações, respeitando o bloqueio. |
| `PATCH /api/pedidos/{id}/status` | ADMIN, PRODUCAO: avança para a próxima etapa. |
| `GET /api/meus-pedidos` | CLIENTE: lista somente os pedidos vinculados ao usuário autenticado. |
| `GET /api/meus-pedidos/{id}` | CLIENTE: consulta pedido próprio; pedido alheio retorna 404. |

As listas aceitam `page`, `size` e `sort`; o padrão é 20 pedidos, por ID decrescente. As consultas do cliente também aceitam filtro `status`. O perfil ENTREGA ainda não recebe acesso a estas rotas.

Exemplo de criação:

```json
{
  "clienteId": 1,
  "itens": [
    { "produto": "Caneca personalizada", "quantidade": 3, "valorUnitario": 19.90 },
    { "produto": "Camisa", "quantidade": 2, "valorUnitario": 25.50 }
  ],
  "observacoes": "Arte combinada pelo WhatsApp"
}
```

O total desse exemplo é 110,70. A resposta inclui `id`, `versao`, `status`, `permiteEdicao`, itens e histórico. `permiteEdicao` representa a etapa do pedido; as permissões do usuário continuam sendo verificadas separadamente.

Para editar, envie `versao`, a lista completa de `itens` e `observacoes` no PUT. Para avançar status:

```json
{ "versao": 0, "status": "APROVADO" }
```

Sempre use a última versão retornada pela API. Edições e transições incrementam essa versão. Uma versão desatualizada, inclusive por operações simultâneas, retorna HTTP 409 e exige reler o pedido. O controle otimista garante que uma edição antiga não sobrescreva a entrada em produção. Transições inválidas e edição bloqueada retornam 422; dados inválidos retornam 400.

## Validação

Execute `mvnw.cmd test`. A suíte atual tem 24 testes passando: cadastro e acesso, isolamento de clientes, permissões, totais, validação de itens, edição até fila, bloqueio após produção, transições e histórico. Dois testes usam transações independentes para verificar as duas ordens possíveis de conflito entre edição e entrada em produção, incluindo a reversão dos itens e do histórico da operação perdedora. A inicialização aplica as 14 migrações e valida o esquema em H2. PostgreSQL ainda não foi validado nesta etapa.
