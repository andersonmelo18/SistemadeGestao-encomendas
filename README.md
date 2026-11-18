# Sistema de Gestão de Encomendas

Aplicação Java 21 com Spring Boot, Spring Security, JPA e Flyway. Inclui uma interface HTML/CSS/JavaScript servida pela própria aplicação.

## Integrantes

- Anderson Melo de Andrade
- Eduardo Cabral
- Gustavo Chagas do Nascimento
- Paulo Cezar de Medeiros
- Pedro Jorge Romero

## Funcionalidades

- Cadastro de clientes com celular e senha protegida por BCrypt.
- Acesso por perfil: administrador, atendente, produção, cliente e entrega.
- Pedidos com itens, quantidades, valores calculados e histórico.
- Fluxo único por pedido: em análise, aprovado, fila, em produção e finalizado.
- Edição permitida até fila, com bloqueio ao iniciar produção e proteção contra alterações concorrentes.
- Cliente consulta somente seus pedidos; a interface atualiza as listas a cada 10 segundos.
- Telas para clientes, equipe, atendimento e produção. Operações de entrega e dashboard administrativo completo ainda estão pendentes.

## Executar pelo IntelliJ

1. Abra o projeto e selecione JDK 21.
2. Recarregue o projeto Maven.
3. Na configuração de execução, selecione `br.edu.uniesp.sistemadegestao.SistemaDeGestaoApplication`.
4. Defina `APP_ADMIN_CELULAR` (DDD e número, somente dígitos) e `APP_ADMIN_SENHA` (mínimo de 8 caracteres, máximo de 72 bytes UTF-8).
5. Execute e acesse http://localhost:8080.

O administrador inicial é criado somente quando a tabela de usuários está vazia. Não há senha padrão. O perfil local usa H2 em memória: os dados desaparecem ao encerrar a aplicação.

Pelo terminal, com Java 21 e as variáveis configuradas:

```powershell
.\mvnw.cmd spring-boot:run
```

## Testes

```powershell
.\mvnw.cmd clean test
```

A última validação passou nos 24 testes, incluindo concorrência e isolamento entre clientes. O JavaScript foi validado sintaticamente; a validação visual automatizada ficou incompleta por indisponibilidade do acesso ao navegador.

## Organização

Os módulos em `br.edu.uniesp.sistemadegestao.internal` seguem as camadas `controller`, `dto`, `entity`, `mapper`, `repository` e `service`.

Consulte [IMPLEMENTACAO.md](IMPLEMENTACAO.md) para detalhes das APIs e [ESCOPO-ENCOMENDAS.md](ESCOPO-ENCOMENDAS.md) para o escopo funcional.

O projeto foi desenvolvido a partir da estrutura de [assistencia-uniesp](https://github.com/JonasBernardino/assistencia-uniesp), preservando o histórico e os módulos legados.
