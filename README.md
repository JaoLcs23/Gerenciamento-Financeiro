# Controle de Gastos Pessoais

Um aplicativo de desktop completo para gerenciamento financeiro pessoal, construído com Java 17, JavaFX e SQL Server.

## Telas da Aplicação

| Menu Principal | Gerenciamento de Contas | Lançar Transação |
| Movimentação entre Contas | Extrato da Conta (Pop-up) | Gerenciamento de Categorias |
| Gerenciamento de Orçamentos | Transações Recorrentes | Relatórios Financeiros |

## Funcionalidades

Este projeto permite um controle financeiro completo, incluindo:

* **Gestão de Contas:** Cadastro de múltiplas contas (Conta Corrente, Poupança, Carteira, Cartão de Crédito).
* **Lógica de Cartão de Crédito:** O saldo do cartão de crédito é tratado corretamente como uma dívida (valor negativo).
* **Saldo em Tempo Real:** A tela de contas calcula e exibe o saldo atual de cada conta, considerando todas as transações.
* **Extrato Individual:** Ao clicar duas vezes em uma conta, uma janela pop-up exibe o extrato detalhado dela (saldo inicial, entradas, saídas e transações).
* **Lançamentos:** CRUD completo de transações de Receita e Despesa.
* **Movimentação Atômica:** Funcionalidade segura para movimentar valores entre contas (ex: saque), com verificação de saldo e transações atômicas (`commit`/`rollback`).
* **Orçamentos (Budgets):** Definição de limites de gastos mensais por categoria.
* **Transações Recorrentes:** Automação de lançamentos fixos (ex: aluguel, assinaturas).
* **Relatórios Avançados:**
    * Balanço do período (Receitas x Despesas).
    * Gráfico de Pizza com a distribuição de despesas por categoria.
    * Gráfico de Linha com a evolução do patrimônio total ao longo do tempo.
    * Exportação do relatório financeiro para **PDF**.

## Tecnologias Utilizadas

* **Java 17**
* **JavaFX 17** (para a interface gráfica)
* **Maven** (para gerenciamento de dependências e build)
* **SQL Server** (Banco de Dados)
* **JDBC (Microsoft Driver)** (para conexão com o banco)
* **Apache PDFBox** (para geração de relatórios em PDF)

## Arquitetura

O projeto utiliza uma arquitetura em 3 camadas (View, Service, DAO) para garantir a separação de responsabilidades e alta manutenibilidade.

1.  **View (Pacote `view`):** Controladores JavaFX e arquivos FXML. Responsáveis pela UI.
2.  **Service (Pacote `service`):** Classe `GastoPessoalService` que contém todas as regras de negócio e gerencia as transações (commit/rollback).
3.  **DAO (Pacote `dao`):** Camada de persistência que executa as queries SQL, recebendo a conexão da camada de serviço.

As credenciais do banco de dados são externalizadas em um arquivo `config.properties`, que é lido pelo `DatabaseConnection.java` e ignorado pelo Git.

## Como Executar

**Pré-requisitos:**
* JDK 17 (ou superior)
* Maven
* Uma instância do SQL Server (recomenda-se o Express)

**1. Clone o Repositório**
```bash
git clone [URL-DO-SEU-REPOSITORIO]
cd Controle-Financeiro
```

**2. Configure o Banco de Dados**
* Inicie seu serviço do SQL Server.
* No **SQL Server Configuration Manager**, certifique-se de que o protocolo **TCP/IP** está habilitado para a sua instância (ex: `SQLEXPRESS`) e ouvindo na porta correta (ex: `1433`).
* Crie um novo banco de dados chamado `db_controle_financeiro`.
* Crie um usuário e senha (ex: `joaolucas` / `12345678`) e dê a ele permissões de `db_owner` no banco `db_controle_financeiro`.

**3. Configure as Credenciais**
* Na pasta `src/main/resources/`, crie o arquivo `config.properties`.
* Adicione suas credenciais a ele. (O `.gitignore` já está configurado para ignorar este arquivo).

*Conteúdo do `config.properties`:*
```properties
# Configurações do Banco de Dados SQL Server
db.server=NOME-DO-SEU-SERVER\\SQLEXPRESS
db.port=1433
db.database=db_controle_financeiro
db.user=seu_usuario
db.password=sua_senha
```

**4. Build e Execução**
* A aplicação criará as tabelas automaticamente na primeira execução.

* **Via Terminal (Maven):**
    ```bash
    # Compila e baixa as dependências
    mvn clean install
    
    # Executa a aplicação
    mvn exec:java
    ```

* **Via IDE (IntelliJ/Eclipse):**
    1.  Abra o projeto como um projeto Maven.
    2.  Aguarde a IDE baixar as dependências.
    3.  Execute a classe `com.controle.app.App.java`.

## Autor

* **João Lucas** - *Desenvolvedor Principal*
* Auxiliado por: Gemini (Google AI)
