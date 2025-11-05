package com.controle.view;

import com.controle.model.Conta;
import com.controle.model.TipoConta;
import com.controle.service.GastoPessoalService;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountController extends BaseController {

    private int selectedAccountId = 0;

    @FXML private TextField accountNameField;
    @FXML private ComboBox<TipoConta> accountTypeComboBox;
    @FXML private TextField accountSaldoInicialField;
    @FXML private TableView<ContaWrapper> accountTable;
    @FXML private TableColumn<ContaWrapper, Integer> colId;
    @FXML private TableColumn<ContaWrapper, String> colName;
    @FXML private TableColumn<ContaWrapper, TipoConta> colType;
    @FXML private TableColumn<ContaWrapper, Double> colSaldoInicial;
    @FXML private TableColumn<ContaWrapper, Double> colSaldoAtual;
    @FXML private Button addAccountButton;
    @FXML private Button updateAccountButton;
    @FXML private Button deleteAccountButton;
    @FXML private Button newAccountButton;
    @FXML private Label accountNameErrorLabel;
    @FXML private Label accountTypeErrorLabel;
    @FXML private Label accountSaldoInicialErrorLabel;

    private GastoPessoalService service;

    private ObservableList<ContaWrapper> contasData = FXCollections.observableArrayList();

    public static class ContaWrapper {
        private final Conta conta;
        private final SimpleDoubleProperty saldoAtual;

        public ContaWrapper(Conta conta) {
            this.conta = conta;
            this.saldoAtual = new SimpleDoubleProperty(0.0);
        }

        public int getId() { return conta.getId(); }
        public String getNome() { return conta.getNome(); }
        public TipoConta getTipo() { return conta.getTipo(); }
        public double getSaldoInicial() { return conta.getSaldoInicial(); }
        public SimpleDoubleProperty saldoAtualProperty() { return saldoAtual; }
        public double getSaldoAtual() { return saldoAtual.get(); }
        public void setSaldoAtual(double saldo) { this.saldoAtual.set(saldo); }
        public Conta getConta() { return conta; }
    }

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

        colSaldoAtual.setCellValueFactory(cellData -> cellData.getValue().saldoAtualProperty().asObject());

        formatCurrencyColumn(colSaldoInicial);
        formatCurrencyColumn(colSaldoAtual);

        accountTable.setItems(contasData);
        accountTable.setPlaceholder(new Label("Carregando contas..."));

        loadAccounts("");
        setFormMode(false);
        clearAllErrors();

        accountTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showAccountDetails(newValue));

        accountTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ContaWrapper selectedConta = accountTable.getSelectionModel().getSelectedItem();
                if (selectedConta != null) {
                    abrirJanelaExtrato(selectedConta);
                }
            }
        });
    }

    private void formatCurrencyColumn(TableColumn<ContaWrapper, Double> column) {
        column.setCellFactory(cell -> new TableCell<ContaWrapper, Double>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().remove("balance-label-negative");
                } else {
                    setText(currencyFormat.format(item));
                    if (item < 0) {
                        if (!getStyleClass().contains("balance-label-negative")) {
                            getStyleClass().add("balance-label-negative");
                        }
                    } else {
                        getStyleClass().remove("balance-label-negative");
                    }
                }
            }
        });
    }

    private void showAccountDetails(ContaWrapper wrapper) {
        clearAllErrors();
        if (wrapper != null) {
            Conta conta = wrapper.getConta();

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
        confirmAlert.setContentText("Atenção: Todas as transações (normais e recorrentes) associadas a esta conta serão EXCLUÍDAS permanentemente. Continuar?");

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
        accountTable.setPlaceholder(new Label("Buscando contas..."));

        Task<List<ContaWrapper>> loadTask = new Task<>() {
            @Override
            protected List<ContaWrapper> call() throws Exception {
                List<Conta> contas = service.listarContasPorTermo(searchTerm);
                return contas.stream().map(ContaWrapper::new).collect(Collectors.toList());
            }
        };

        loadTask.setOnSucceeded(event -> {
            contasData.setAll(loadTask.getValue());
            if (contasData.isEmpty()) {
                accountTable.setPlaceholder(new Label("Nenhuma conta cadastrada"));
            }
            calculateSaldosAsync();
        });

        loadTask.setOnFailed(event -> {
            accountTable.setPlaceholder(new Label("Erro ao carregar contas."));
            showAlert(Alert.AlertType.ERROR, "Erro ao Carregar", "Não foi possível carregar a lista de contas.");
        });

        new Thread(loadTask).start();
    }

    private void calculateSaldosAsync() {
        for (ContaWrapper wrapper : contasData) {
            Task<Double> saldoTask = new Task<>() {
                @Override
                protected Double call() throws Exception {
                    return service.getSaldoAtual(wrapper.getId());
                }
            };

            saldoTask.setOnSucceeded(event -> {
                wrapper.setSaldoAtual(saldoTask.getValue());
            });

            saldoTask.setOnFailed(event -> {
                System.err.println("Falha ao calcular saldo para conta " + wrapper.getId());
            });

            new Thread(saldoTask).start();
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

    private void abrirJanelaExtrato(ContaWrapper contaWrapper) {
        if (contaWrapper == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/ExtratoView.fxml"));
            Parent root = loader.load();

            ExtratoController extratoController = loader.getController();

            extratoController.initData(contaWrapper.getConta(), contaWrapper.getSaldoAtual());

            Stage stage = new Stage();
            stage.setTitle("Extrato da Conta: " + contaWrapper.getNome());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());
            stage.setScene(scene);

            stage.initOwner(primaryStage);
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro ao Abrir Extrato", "Não foi possível carregar a tela de extrato.");
        }
    }
}