package com.controle.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // Configurações para SQL Server
    private static final String SERVER_NAME = "DESKTOP-V3M2DDJ\\SQLEXPRESS";
    private static final int PORT = 1433;
    private static final String DATABASE_NAME = "db_controle_financeiro";
    private static final String USER = "joaolucas";
    private static final String PASSWORD = "12345678";

    // URL de conexao para SQL Server
    private static final String URL = "jdbc:sqlserver://" + SERVER_NAME + ":" + PORT + ";" +
            "databaseName=" + DATABASE_NAME + ";" +
            "user=" + USER + ";" +
            "password=" + PASSWORD + ";" +
            "encrypt=false;" +
            "trustServerCertificate=true;";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Tabela 'categorias'
            String createCategoriasTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='categorias' and xtype='U') " +
                    "CREATE TABLE categorias (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "nome NVARCHAR(255) NOT NULL UNIQUE, " +
                    "tipo NVARCHAR(50) NOT NULL" +
                    ");";
            stmt.execute(createCategoriasTableSQL);
            System.out.println("Tabela 'categorias' verificada/criada com sucesso.");

            // Tabela 'contas'
            String createContasTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='contas' and xtype='U') " +
                    "CREATE TABLE contas (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "nome NVARCHAR(255) NOT NULL UNIQUE, " +
                    "saldo_inicial DECIMAL(18, 2) NOT NULL DEFAULT 0, " +
                    "tipo NVARCHAR(50) NOT NULL" +
                    ");";
            stmt.execute(createContasTableSQL);
            System.out.println("Tabela 'contas' verificada/criada com sucesso.");

            // Tabela 'transacoes'
            String createTransacoesTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='transacoes' and xtype='U') " +
                    "CREATE TABLE transacoes (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "descricao NVARCHAR(MAX) NOT NULL, " +
                    "valor DECIMAL(18, 2) NOT NULL, " +
                    "data DATE NOT NULL, " +
                    "tipo NVARCHAR(50) NOT NULL, " +
                    "categoria_id INT, " +
                    "conta_id INT, " + // <-- CAMPO NOVO
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (conta_id) REFERENCES contas(id) ON DELETE CASCADE" + // <-- RELACIONAMENTO NOVO
                    ");";
            stmt.execute(createTransacoesTableSQL);
            System.out.println("Tabela 'transacoes' verificada/criada com sucesso.");

            // Tabela 'transacoes_recorrentes'
            String createTransacoesRecorrentesSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='transacoes_recorrentes' and xtype='U') " +
                    "CREATE TABLE transacoes_recorrentes (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "descricao NVARCHAR(MAX) NOT NULL, " +
                    "valor DECIMAL(18, 2) NOT NULL, " +
                    "tipo NVARCHAR(50) NOT NULL, " +
                    "categoria_id INT NOT NULL, " +
                    "conta_id INT NOT NULL, " + // <-- CAMPO NOVO
                    "dia_do_mes INT NOT NULL, " +
                    "data_inicio DATE NOT NULL, " +
                    "data_fim DATE NULL, " +
                    "data_ultimo_processamento DATE NULL, " +
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (conta_id) REFERENCES contas(id) ON DELETE CASCADE" + // <-- RELACIONAMENTO NOVO
                    ");";
            stmt.execute(createTransacoesRecorrentesSQL);
            System.out.println("Tabela 'transacoes_recorrentes' verificada/criada com sucesso.");

            // Tabela 'orcamentos'
            String createOrcamentosTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='orcamentos' and xtype='U') " +
                    "CREATE TABLE orcamentos (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "categoria_id INT NOT NULL, " +
                    "valor_limite DECIMAL(18, 2) NOT NULL, " +
                    "mes INT NOT NULL, " +
                    "ano INT NOT NULL, " +
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE, " +
                    "CONSTRAINT UQ_Categoria_Mes_Ano UNIQUE (categoria_id, mes, ano)" +
                    ");";
            stmt.execute(createOrcamentosTableSQL);
            System.out.println("Tabela 'orcamentos' verificada/criada com sucesso.");

        } catch (SQLException e) {
            if (!e.getMessage().contains("Could not create constraint or index") && !e.getMessage().contains("already exists")) {
                throw e;
            } else {
                System.out.println("Aviso: Tabelas ou constraints já existem, pulando criação.");
            }
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão com o banco de dados: " + e.getMessage());
            }
        }
    }
}