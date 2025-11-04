package com.controle.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import com.controle.view.CategoryController;
import com.controle.view.TransactionController;
import com.controle.view.RecurringTransactionController;
import com.controle.view.BudgetController;
import com.controle.view.ReportsController;
import com.controle.view.AccountController;
import java.io.IOException;

public class MenuController extends BaseController {

    @FXML
    private void handleManageCategories(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/CategoryView.fxml"));
            Parent root = loader.load();

            CategoryController categoryController = loader.getController();
            categoryController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Categorias");
            primaryStage.show();

            applyFullScreen();
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de categorias: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de categorias.");
        }
    }

    @FXML
    private void handleManageAccounts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/AccountView.fxml"));
            Parent root = loader.load();

            AccountController accountController = loader.getController();
            accountController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Contas");
            primaryStage.show();

            applyFullScreen();
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de contas: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de contas.");
        }
    }

    @FXML
    private void handleManageTransactions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/TransactionView.fxml"));
            Parent root = loader.load();

            TransactionController transactionController = loader.getController();
            transactionController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Transações");
            primaryStage.show();

            applyFullScreen();
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de transações: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de transações.");
        }
    }

    @FXML
    private void handleManageRecurringTransactions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/RecurringTransactionView.fxml"));
            Parent root = loader.load();

            RecurringTransactionController recurringController = loader.getController();
            recurringController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Transações Recorrentes");
            primaryStage.show();

            applyFullScreen();
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de transações recorrentes: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de transações recorrentes.");
        }
    }

    @FXML
    private void handleManageBudgets(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/BudgetView.fxml"));
            Parent root = loader.load();

            BudgetController budgetController = loader.getController();
            budgetController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Orçamentos");
            primaryStage.show();

            applyFullScreen();
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de orçamentos: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de orçamentos.");
        }
    }

    @FXML
    private void handleViewReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/ReportsView.fxml"));
            Parent root = loader.load();

            ReportsController reportsController = loader.getController();
            reportsController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Relatórios");
            primaryStage.show();

            applyFullScreen();
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de relatórios: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela de relatórios.");
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @Override
    protected void clearAllErrors() {
    }
}