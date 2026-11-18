# Sistema de gestão de encomendas

## Base e organização

Usar este repositório como base, preservando a organização por domínio demonstrada em `internal/cliente`. Novos módulos seguem as camadas `controller`, `dto`, `entity`, `mapper`, `repository` e `service`, conforme sua necessidade.

- Controllers recebem e validam requisições HTTP e devolvem DTOs.
- DTOs definem entradas e saídas; entidades não são respostas da API.
- Services coordenam casos de uso, transações e regras de negócio.
- Entities representam o domínio e encapsulam suas mudanças.
- Mappers convertem entidades e DTOs.
- Repositories concentram o acesso à persistência.
- Manter o padrão existente de injeção por construtor e exceções compartilhadas.

## Objetivo

Gerenciar encomendas recebidas pelo atendente via Instagram ou WhatsApp, desde o lançamento até a entrega, com acompanhamento de status pelo cliente e áreas separadas por perfil. O recebimento pelas redes sociais é externo ao sistema; integração automática não foi solicitada.

## Painéis

| Perfil | Responsabilidades |
| --- | --- |
| Atendente | Cadastrar clientes com nome, celular e senha de acesso; lançar pedidos vinculados ao cliente, produtos, quantidades e valores; consultar e alterar pedidos enquanto permitido; encaminhar à análise de produção. |
| Produção | Visualizar cards dos produtos/pedidos e conduzir as etapas em análise, aprovado, fila, em produção e finalizado. |
| Cliente | Acessar com celular e senha; acompanhar somente os próprios pedidos e suas etapas em tempo real; solicitar mudanças ao atendente por contato externo. |
| Administrador | Acessar gestão de clientes, vendas e dashboard gerencial. |
| Entrega | Acessar painel simples dos pedidos finalizados para realizar o controle de entrega. |

A separação por área deve ser aplicada tanto na interface quanto nas permissões do backend. O cliente não edita pedidos diretamente.

## Fluxo e regras confirmadas

1. O cliente faz o pedido pelo Instagram ou WhatsApp.
2. O atendente cadastra ou identifica o cliente e lança o pedido com produtos, quantidades e valores.
3. O pedido entra em análise de produção.
4. O fluxo de produção é: EM_ANALISE → APROVADO → FILA → EM_PRODUCAO → FINALIZADO. O status pertence ao pedido inteiro: todos os produtos avançam juntos, sem etapas independentes por item.
5. Até FILA, inclusive, o cliente pode solicitar alterações ao atendente.
6. Ao entrar em EM_PRODUCAO, o pedido inteiro e todos os seus itens ficam bloqueados simultaneamente para alterações de conteúdo, quantidades e valores. As transições de andamento e o registro de entrega continuam possíveis.
7. O bloqueio deve ser validado no backend e protegido contra uma edição concorrente à entrada em produção.
8. O acompanhamento do cliente deve refletir mudanças sem recarregamento manual e respeitar o isolamento dos dados de cada cliente.
9. Pedido finalizado na produção fica disponível para o processo de entrega.

Senhas devem ser armazenadas somente como hash e nunca retornadas nos DTOs. Mudanças de status devem manter histórico com data, etapa e responsável.

## Situação da base inspecionada

Projeto Java 21, Spring Boot, JPA, validação, Flyway e dependências de PostgreSQL/H2. Existem módulos de cliente, equipamento, ordem de serviço e entidades relacionadas à assistência técnica.

O cadastro atual de cliente exige CPF e e-mail e ainda não possui celular. O usuário atual usa e-mail e contém os perfis ADMIN, ATENDENTE, TECNICO e CLIENTE. Será necessário adaptar o cadastro e a autenticação ao escopo de encomendas e introduzir permissões de produção e entrega. Os módulos existentes não devem ser removidos automaticamente durante essa adaptação.

## Decisões a detalhar durante a implementação

- Dados adicionais de produto e pedido, como personalização, anexos e prazo.
- Regras de reprovação, cancelamento e eventual retorno à análise.
- Se entrega significa retirada, envio ou ambos, e quais dados serão registrados na conclusão.
- Indicadores do dashboard e distinção entre valor vendido e valor efetivamente pago.

## Sequência de implementação

1. Adequar clientes, autenticação e autorização por perfil.
2. Implementar produtos, pedidos e itens seguindo as camadas existentes.
3. Implementar transições, histórico e bloqueio transacional de alterações.
4. Implementar os painéis de atendimento, produção e acompanhamento do cliente.
5. Implementar atualização em tempo real com isolamento por cliente.
6. Implementar entrega e dashboard administrativo.
7. Verificar permissões, transições, valores, concorrência e isolamento entre clientes com testes adequados.

Este documento registra o escopo; não representa funcionalidades já implementadas.

