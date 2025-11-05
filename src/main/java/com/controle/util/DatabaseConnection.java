package com.controle.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    private static final Properties props = new Properties();
    private static final String URL;

    static {
        System.out.println("Carregando configurações do banco de dados...");
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {

            if (input == null) {
                System.err.println("ERRO FATAL: Não foi possível encontrar o arquivo 'config.properties' no classpath.");
                throw new RuntimeException("Arquivo 'config.properties' não encontrado.");
            }

            props.load(input);

            String serverName = props.getProperty("db.server");
            String port = props.getProperty("db.port");
            String databaseName = props.getProperty("db.database");

            URL = "jdbc:sqlserver://" + serverName + ":" + port + ";" +
                    "databaseName=" + databaseName + ";" +
                    "encrypt=false;" +
                    "trustServerCertificate=true;";

            System.out.println("Configurações do banco de dados carregadas com sucesso.");

        } catch (IOException ex) {
            System.err.println("ERRO FATAL: Falha ao ler o arquivo 'config.properties'.");
            throw new RuntimeException("Falha ao ler 'config.properties'", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, props.getProperty("db.user"), props.getProperty("db.password"));
    }

    public static void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createCategoriasTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='categorias' and xtype='U') " +
                    "CREATE TABLE categorias (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "nome NVARCHAR(255) NOT NULL UNIQUE, " +
                    "tipo NVARCHAR(50) NOT NULL" +
                    ");";
            stmt.execute(createCategoriasTableSQL);
            System.out.println("Tabela 'categorias' verificada/criada com sucesso.");

            String createContasTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='contas' and xtype='U') " +
                    "CREATE TABLE contas (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "nome NVARCHAR(255) NOT NULL UNIQUE, " +
                    "saldo_inicial DECIMAL(18, 2) NOT NULL DEFAULT 0, " +
                    "tipo NVARCHAR(50) NOT NULL" +
                    ");";
            stmt.execute(createContasTableSQL);
            System.out.println("Tabela 'contas' verificada/criada com sucesso.");

            String createTransacoesTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='transacoes' and xtype='U') " +
                    "CREATE TABLE transacoes (" +
                    "id INT PRIMARY KEY IDENTITY(1,1), " +
                    "descricao NVARCHAR(MAX) NOT NULL, " +
                    "valor DECIMAL(18, 2) NOT NULL, " +
                    "data DATE NOT NULL, " +
                    "tipo NVARCHAR(50) NOT NULL, " +
                    "categoria_id INT, " +
                    "conta_id INT, " +
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (conta_id) REFERENCES contas(id) ON DELETE CASCADE" +
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
                    "conta_id INT NOT NULL, " +
                    "dia_do_mes INT NOT NULL, " +
                    "data_inicio DATE NOT NULL, " +
                    "data_fim DATE NULL, " +
                    "data_ultimo_processamento DATE NULL, " +
                    "FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (conta_id) REFERENCES contas(id) ON DELETE CASCADE" +
                    ");";
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