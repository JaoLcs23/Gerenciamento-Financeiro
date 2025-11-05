# Documentação do Projeto: Controle de Gastos Pessoais

## 1. Introdução

Este documento detalha a arquitetura, componentes e fluxo de dados do aplicativo "Controle de Gastos Pessoais". O objetivo do projeto é fornecer uma solução de desktop robusta, segura e completa para o gerenciamento de finanças pessoais, permitindo ao usuário controlar múltiplas contas, categorias, orçamentos e transações recorrentes.

## 2. Tecnologias Utilizadas

O projeto foi construído utilizando as seguintes tecnologias:
* **Java 17:** Versão da linguagem base.
* **JavaFX:** Framework para a construção da interface gráfica (UI).
* **Maven:** Gerenciador de dependências e build do projeto.
* **SQL Server:** Sistema de Gerenciamento de Banco de Dados (SGBD).
* **JDBC (Microsoft Driver):** Conector para a comunicação entre a aplicação Java e o banco SQL Server.
* **Apache PDFBox:** Biblioteca para a funcionalidade de "Exportar para PDF" na tela de relatórios.

## 3. Arquitetura do Projeto

A aplicação segue uma arquitetura em 3 camadas (3-Tier) para garantir a separação de responsabilidades (Separation of Concerns), o que facilita a manutenção e a escalabilidade.

### Camada 1: View (Apresentação)
* **Localização:** Pacote `com.controle.view`.
* **Componentes:** Arquivos `.fxml` (estruturas da UI), `style.css` (estilização) e as classes `Controller` (ex: `TransactionController`, `AccountController`).
* **Responsabilidade:** Exibir informações ao usuário, capturar entradas (cliques, formulários) e delegar as ações de negócio para a Camada de Serviço. **Não contém regras de negócio.**

### Camada 2: Service (Serviço/Negócio)
* **Localização:** Pacote `com.controle.service`.
* **Componentes:** `GastoPessoalService.java`.
* **Responsabilidade:** É o "cérebro" da aplicação. Contém todas as regras de negócio (ex: "não permitir saldo negativo", "calcular saldo do cartão de crédito de forma invertida", "ignorar movimentações nos relatórios").
* **Gerenciamento de Transações:** Esta camada é a **dona da conexão com o banco**. Ela é responsável por iniciar, comitar (`commit`) e reverter (`rollback`) transações atômicas (Padrão *Service-Layer Transactions*), garantindo a integridade dos dados.

### Camada 3: DAO (Acesso a Dados)
* **Localização:** Pacote `com.controle.dao`.
* **Componentes:** `AbstractDAO`, `GenericDAO` e as implementações (`ContaDAO`, `TransacaoDAO`, etc.).
* **Responsabilidade:** É a única camada que "fala" com o banco de dados. Contém as queries SQL (INSERT, SELECT, UPDATE, DELETE). Os DAOs **não** abrem ou gerenciam conexões; eles apenas recebem a conexão da Camada de Serviço e executam o SQL.

## 4. Estrutura de Pacotes

* `com.controle.app`: Classe principal de inicialização do JavaFX (`App.java`).
* `com.controle.dao`: Camada de Acesso a Dados.
* `com.controle.model`: Classes de entidade (POJOs) como `Conta`, `Transacao`, `Categoria`.
* `com.controle.service`: Camada de Regras de Negócio.
* `com.controle.util`: Classes utilitárias, como o `DatabaseConnection.java`.
* `com.controle.view`: Camada de Apresentação (Controllers do JavaFX).
* `resources/com/controle/view`: Arquivos FXML (`.fxml`) e o `style.css`.
* `resources/`: Arquivo de configuração (`config.properties`).

## 5. Esquema do Banco de Dados

O banco de dados (`db_controle_financeiro`) é composto por 5 tabelas principais:

1.  **`categorias`**
    * `id` (PK, Identity)
    * `nome` (NVARCHAR, Unique)
    * `tipo` (NVARCHAR - "RECEITA" ou "DESPESA")

2.  **`contas`**
    * `id` (PK, Identity)
    * `nome` (NVARCHAR, Unique)
    * `saldo_inicial` (DECIMAL)
    * `tipo` (NVARCHAR - ex: "CONTA_CORRENTE", "CARTAO_DE_CREDITO")

3.  **`transacoes`**
    * `id` (PK, Identity)
    * `descricao` (NVARCHAR)
    * `valor` (DECIMAL)
    * `data` (DATE)
    * `tipo` (NVARCHAR)
    * `categoria_id` (FK para `categorias.id`, ON DELETE SET NULL)
    * `conta_id` (FK para `contas.id`, ON DELETE CASCADE)

4.  **`transacoes_recorrentes`**
    * `id` (PK, Identity)
    * ... (descricao, valor, tipo, etc.)
    * `categoria_id` (FK para `categorias.id`, ON DELETE CASCADE)
    * `conta_id` (FK para `contas.id`, ON DELETE CASCADE)
    * `dia_do_mes` (INT)
    * `data_inicio` (DATE)
    * `data_fim` (DATE, Nullable)
    * `data_ultimo_processamento` (DATE, Nullable)

5.  **`orcamentos`**
    * `id` (PK, Identity)
    * `categoria_id` (FK para `categorias.id`, ON DELETE CASCADE)
    * `valor_limite` (DECIMAL)
    * `mes` (INT)
    * `ano` (INT)
    * `UQ_Categoria_Mes_Ano` (Constraint UNIQUE em `categoria_id`, `mes`, `ano`)

## 6. Fluxo de uma Operação (Ex: Movimentação entre Contas)

1.  **View (`MovimentacaoController`):** O usuário preenche o formulário (conta origem, destino, valor) e clica em "REGISTRAR MOVIMENTAÇÃO".
2.  **Controller:** O `MovimentacaoController.java` chama o método `service.transferirFundos()`, passando os objetos `Conta` e o `valor`.
3.  **Service (`GastoPessoalService`):**
    a.  O método `transferirFundos()` é iniciado.
    b.  Ele abre uma `Connection` com o banco usando `DatabaseConnection.getConnection()`.
    c.  Define `conn.setAutoCommit(false);` para iniciar a transação.
    d.  Chama `calcularSaldoAtual(contaOrigem, conn)` para verificar se o saldo é suficiente.
    e.  Se o saldo for insuficiente, lança uma `RuntimeException`, o `catch` executa `conn.rollback()` e a exceção é enviada para a View.
    f.  Se o saldo for suficiente, ele chama `transacaoDAO.save(despesa, conn)`.
    g.  Em seguida, chama `transacaoDAO.save(receita, conn)`.
    h.  Se ambas as chamadas DAO funcionarem, ele executa `conn.commit()`, salvando permanentemente as duas transações.
    i.  O `try-with-resources` fecha a `Connection`.
4.  **Controller:** O `MovimentacaoController` recebe a confirmação (ou a exceção), e exibe um `Alert` de "Sucesso" (ou "Erro") para o usuário.