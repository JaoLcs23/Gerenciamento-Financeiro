package com.controle.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // --- CONFIGURACOES PARA SQL SERVER ---
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

            // SQL para criar a tabela 'categorias' no SQL Server
            String createCategoriasTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='categorias' and xtype='U') " +
                    "CREATE TABLE categorias (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "nome NVARCHAR(255) NOT NULL UNIQUE, " +
                    "tipo NVARCHAR(50) NOT NULL" +
                    ");";
            stmt.execute(createCategoriasTableSQL);
            System.out.println("Tabela 'categorias' verificada/criada com sucesso.");

            // SQL para criar a tabela 'transacoes' no SQL Server
            String createTransacoesTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='transacoes' and xtype='U') " +
                    "CREATE TABLE transacoes (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "descricao NVARCHAR(MAX) NOT NULL, " +
                    "valor DECIMAL(18, 2) NOT NULL, " +
                    "data DATE NOT NULL, " +
                    "tipo NVARCHAR(50) NOT NULL, " +
                    "categoria_id INT, " +
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL" +
                    ");";
            stmt.execute(createTransacoesTableSQL);
            System.out.println("Tabela 'transacoes' verificada/criada com sucesso.");

            String createTransacoesRecorrentesSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='transacoes_recorrentes' and xtype='U') " +
                    "CREATE TABLE transacoes_recorrentes (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "descricao NVARCHAR(MAX) NOT NULL, " +
                    "valor DECIMAL(18, 2) NOT NULL, " +
                    "tipo NVARCHAR(50) NOT NULL, " +
                    "categoria_id INT NOT NULL, " +
                    "dia_do_mes INT NOT NULL, " +
                    "data_inicio DATE NOT NULL, " +
                    "data_fim DATE NULL, " +
                    "data_ultimo_processamento DATE NULL, " +
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE" +
                    ");";
            stmt.execute(createTransacoesRecorrentesSQL);
            System.out.println("Tabela 'transacoes_recorrentes' verificada/criada com sucesso.");

            stmt.execute(createTransacoesRecorrentesSQL);
            System.out.println("Tabela 'transacoes_recorrentes' verificada/criada com sucesso.");

            String createOrcamentosTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='orcamentos' and xtype='U') " +
                    "CREATE TABLE orcamentos (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "categoria_id INT NOT NULL, " +
                    "valor_limite DECIMAL(18, 2) NOT NULL, " +
                    "mes INT NOT NULL, " +
                    "ano INT NOT NULL, " +
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE, " +
                    // Adiciona uma restrição para não permitir dois orçamentos para a mesma categoria no mesmo mês/ano
                    "CONSTRAINT UQ_Categoria_Mes_Ano UNIQUE (categoria_id, mes, ano)" +
                    ");";
            stmt.execute(createOrcamentosTableSQL);
            System.out.println("Tabela 'orcamentos' verificada/criada com sucesso.");
        }
    }


    //Metodo para fechar uma conexao com o banco de dados
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