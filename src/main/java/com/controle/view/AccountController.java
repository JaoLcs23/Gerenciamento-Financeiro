package com.controle.view;

import com.controle.model.Conta;
import com.controle.model.TipoConta;
import com.controle.service.GastoPessoalService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class AccountController extends BaseController {

    private int selectedAccountId = 0;

    @FXML private TextField accountNameField;
    @FXML private ComboBox<TipoConta> accountTypeComboBox;
    @FXML private TextField accountSaldoInicialField;
    @FXML private TableView<Conta> accountTable;
    @FXML private TableColumn<Conta, Integer> colId;
    @FXML private TableColumn<Conta, String> colName;
    @FXML private TableColumn<Conta, TipoConta> colType;
    @FXML private TableColumn<Conta, Double> colSaldoInicial;

    @FXML private Button addAccountButton;
    @FXML private Button updateAccountButton;
    @FXML private Button deleteAccountButton;
    @FXML private Button newAccountButton;

    @FXML private Label accountNameErrorLabel;
    @FXML private Label accountTypeErrorLabel;
    @FXML private Label accountSaldoInicialErrorLabel;

    // O Label fullScreenHintLabel é herdado do BaseController
    // @FXML private Label fullScreenHintLabel;

    private GastoPessoalService service;
    private ObservableList<Conta> contasData = FXCollections.observableArrayList();

    public AccountController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        accountTypeComboBox.getItems().setAll(TipoConta.values());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colType.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colSaldoInicial.setCellValueFactory(new PropertyValueFactory<>("saldoInicial"));

        // Formata o Saldo Inicial como Moeda (R$)
        colSaldoInicial.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                    setText(currencyFormat.format(item));
                }
            }
        });

        accountTable.setItems(contasData);
        accountTable.setPlaceholder(new Label("Nenhuma conta cadastrada"));

        loadAccounts("");
        setFormMode(false);
        clearAllErrors();

        accountTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showAccountDetails(newValue));
    }

    private void showAccountDetails(Conta conta) {
        clearAllErrors();
        if (conta != null) {
            selectedAccountId = conta.getId();
            accountNameField.setText(conta.getNome());
            accountTypeComboBox.getSelectionModel().select(conta.getTipo());
            accountSaldoInicialField.setText(String.format(Locale.US, "%.2f", conta.getSaldoInicial()));
            setFormMode(true);
        } else {
            handleNewAccount(null);
        }
    }

    private void setFormMode(boolean isEditing) {
        addAccountButton.setDisable(isEditing);
        updateAccountButton.setDisable(!isEditing);
        deleteAccountButton.setDisable(!isEditing);
        newAccountButton.setDisable(false);
    }

    @FXML
    private void handleNewAccount(ActionEvent event) {
        accountNameField.clear();
        accountTypeComboBox.getSelectionModel().clearSelection();
        accountSaldoInicialField.setText("0.00");
        selectedAccountId = 0;
        accountTable.getSelectionModel().clearSelection();
        setFormMode(false);
        clearAllErrors();
        loadAccounts("");
    }

    @FXML
    private void handleAddOrUpdateAccount(ActionEvent event) {
        clearAllErrors();

        String name = accountNameField.getText();
        TipoConta type = accountTypeComboBox.getSelectionModel().getSelectedItem();
        double saldoInicial = 0.0;

        boolean isValid = true;
        if (name == null || name.trim().isEmpty()) {
            showFieldError(accountNameField, accountNameErrorLabel, "Nome da conta é obrigatório.");
            isValid = false;
        }
        if (type == null) {
            showFieldError(accountTypeComboBox, accountTypeErrorLabel, "Tipo da conta é obrigatório.");
            isValid = false;
        }

        try {
            String saldoStr = accountSaldoInicialField.getText().replace(",", ".");
            if (saldoStr != null && !saldoStr.trim().isEmpty()) {
                saldoInicial = Double.parseDouble(saldoStr);
            }
        } catch (NumberFormatException e) {
            showFieldError(accountSaldoInicialField, accountSaldoInicialErrorLabel, "Valor inválido. Use números.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        try {
            Conta conta = new Conta(name, saldoInicial, type);

            if (selectedAccountId == 0) {
                service.adicionarConta(conta);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Conta '" + name + "' adicionada!");
            } else {
                conta.setId(selectedAccountId);
                service.atualizarConta(conta);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Conta '" + name + "' atualizada!");
            }
            loadAccounts("");
            handleNewAccount(null);
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        if (selectedAccountId == 0) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Selecione uma conta para excluir.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir Conta?");
        confirmAlert.setContentText("Tem certeza? (Transações futuras associadas a esta conta serão afetadas).");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.excluirConta(selectedAccountId);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Conta excluída!");
                loadAccounts("");
                handleNewAccount(null);
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Não foi possível excluir. Verifique se a conta está em uso.");
            }
        }
    }

    private void loadAccounts(String searchTerm) {
        try {
            contasData.clear();
            // Precisamos adicionar o 'listarContasPorTermo' no Service
            contasData.addAll(service.listarContasPorTermo(searchTerm));
        } catch (Exception e) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erro ao carregar contas", e.getMessage()));
        }
    }

    @FXML
    private void handleGoBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
            Parent root = loader.load();
            MenuController menuController = loader.getController();
            menuController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
            primaryStage.show();

            applyFullScreen();
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar o menu principal.");
        }
    }

    @Override
    protected void clearAllErrors() {
        clearFieldError(accountNameField, accountNameErrorLabel);
        clearFieldError(accountTypeComboBox, accountTypeErrorLabel);
        clearFieldError(accountSaldoInicialField, accountSaldoInicialErrorLabel);
    }
}