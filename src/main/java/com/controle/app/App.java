package com.controle.app;

import com.controle.view.MenuController;
import com.controle.service.GastoPessoalService;
import com.controle.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        // --- CORREÇÃO: CRIA AS TABELAS ANTES DE TUDO ---
        try {
            System.out.println("Verificando/Criando tabelas no banco de dados...");
            DatabaseConnection.createTables(); // Chama o método de criação
            System.out.println("Tabelas verificadas/criadas com sucesso.");
        } catch (Exception e) {
            System.err.println("ERRO FATAL: Não foi possível criar as tabelas do banco de dados: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro Crítico de Banco de Dados");
            alert.setHeaderText("Não foi possível conectar ou criar as tabelas no banco de dados.");
            alert.setContentText("Verifique sua conexão e as configurações em DatabaseConnection.java.\nDetalhes: " + e.getMessage());
            alert.showAndWait();
            Platform.exit(); // Fecha a aplicação
            return; // Interrompe a execução
        }
        // --- FIM DA CORREÇÃO ---

        // Roda o processamento em background (agora com a tabela já criada)
        new Thread(() -> {
            System.out.println("Iniciando processamento de transações recorrentes em background...");
            try {
                GastoPessoalService service = new GastoPessoalService();
                service.processarTransacoesRecorrentes();
                System.out.println("Processamento de transações recorrentes finalizado.");
            } catch (Exception e) {
                System.err.println("Erro no processamento de recorrentes: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();


        Parent root = null;
        Scene scene = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
            root = loader.load();

            MenuController menuController = loader.getController();
            if (menuController != null) {
                menuController.setPrimaryStage(primaryStage);
            } else {
                System.err.println("ERRO: MenuController não foi obtido do FXMLLoader!");
            }

            scene = new Scene(root, 700, 750);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
            primaryStage.setScene(scene);

            primaryStage.show();
            Platform.runLater(() -> primaryStage.setFullScreen(true));

        } catch (IOException e) {
            // Este é o erro que você está vendo (FXML corrompido)
            System.err.println("ERRO FATAL: Erro de IO ao carregar FXML ou CSS: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Inicialização");
            alert.setHeaderText("Não foi possível iniciar a aplicação (Erro de FXML).");
            alert.setContentText("Verifique o arquivo .fxml para erros de sintaxe.\nDetalhes: " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("ERRO FATAL: Erro inesperado durante a inicialização: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro Inesperado");
            alert.setHeaderText("Um erro inesperado ocorreu ao iniciar.");
            alert.setContentText("Detalhes: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}