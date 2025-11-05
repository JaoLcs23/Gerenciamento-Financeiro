package com.controle.view;

import com.controle.service.GastoPessoalService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController {

    protected Stage primaryStage;

    @FXML private BorderPane contentPane;
    @FXML private Label fullScreenHintLabel;

    private GastoPessoalService service;

    private static Timer hintTimer = null;
    private static AtomicInteger hintCounter = new AtomicInteger(0);

    public MainController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        handleShowDashboard(null);

        Platform.runLater(() -> {
            System.out.println("MainController: Verificando transações recorrentes pendentes...");
            service.processarTransacoesRecorrentes();
            handleShowDashboard(null);
        });
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        applyFullScreen();
        showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);
    }

    @FXML
    private void handleShowDashboard(ActionEvent event) {
        loadView("/com/controle/view/ReportsView.fxml", "Dashboard");
    }

    @FXML
    private void handleManageAccounts(ActionEvent event) {
        loadView("/com/controle/view/AccountView.fxml", "Gerenciamento de Contas");
    }

    @FXML
    private void handleManageCategories(ActionEvent event) {
        loadView("/com/controle/view/CategoryView.fxml", "Gerenciamento de Categorias");
    }

    @FXML
    private void handleManageTransactions(ActionEvent event) {
        loadView("/com/controle/view/TransactionView.fxml", "Lançar Transação");
    }

    @FXML
    private void handleManageMovimentacoes(ActionEvent event) {
        loadView("/com/controle/view/MovimentacaoView.fxml", "Movimentação entre Contas");
    }

    @FXML
    private void handleManageRecurring(ActionEvent event) {
        loadView("/com/controle/view/RecurringTransactionView.fxml", "Transações Recorrentes");
    }

    @FXML
    private void handleManageBudgets(ActionEvent event) {
        loadView("/com/controle/view/BudgetView.fxml", "Gerenciamento de Orçamentos");
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }

    private void loadView(String fxmlPath, String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent viewRoot = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setPrimaryStage(this.primaryStage);
            }

            contentPane.setCenter(viewRoot);

            System.out.println("Navegando para: " + viewName);

        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML: " + fxmlPath);
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela: " + fxmlPath);
        } catch (NullPointerException e) {
            System.err.println("Erro fatal: O arquivo FXML não foi encontrado em: " + fxmlPath);
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro Crítico", "Arquivo de interface não encontrado: " + fxmlPath);
        }
    }

    protected void applyFullScreen() {
        if (primaryStage != null) {
            Platform.runLater(() -> primaryStage.setFullScreen(true));
        }
    }

    protected void showFullScreenHintTemporarily(String message, int durationMillis) {
        hintCounter.incrementAndGet();
        showFullScreenHint(message);
        if (hintTimer != null) {
            hintTimer.cancel();
        }
        hintTimer = new Timer("FullScreenHintTimer", true);
        hintTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hintCounter.decrementAndGet() <= 0) {
                    hideFullScreenHint();
                }
            }
        }, durationMillis);
    }

    protected void showFullScreenHint(String message) {
        if (fullScreenHintLabel != null) {
            Platform.runLater(() -> {
                fullScreenHintLabel.setText(message);
                fullScreenHintLabel.setVisible(true);
                fullScreenHintLabel.setManaged(true);
            });
        }
    }

    protected void hideFullScreenHint() {
        if (fullScreenHintLabel != null) {
            Platform.runLater(() -> {
                fullScreenHintLabel.setVisible(false);
                fullScreenHintLabel.setManaged(false);
            });
        }
    }

    protected void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}