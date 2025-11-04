package com.controle.view;

import com.controle.model.*;
import com.controle.service.GastoPessoalService;
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
import javafx.application.Platform;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.ListCell;

public class TransactionController extends BaseController {

    private int selectedTransactionId = 0;

    @FXML private Label fullScreenHintLabel;
    @FXML private TextField descriptionField;
    @FXML private TextField valueField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<TipoCategoria> typeComboBox;
    @FXML private ComboBox<Categoria> categoryComboBox;
    @FXML private ComboBox<Conta> accountComboBox;
    @FXML private TableView<Transacao> transactionTable;
    @FXML private TableColumn<Transacao, Integer> colId;
    @FXML private TableColumn<Transacao, String> colDescription;
    @FXML private TableColumn<Transacao, Double> colValue;
    @FXML private TableColumn<Transacao, LocalDate> colDate;
    @FXML private TableColumn<Transacao, TipoCategoria> colType;
    @FXML private TableColumn<Transacao, Categoria> colCategory;
    @FXML private TableColumn<Transacao, Conta> colAccount;
    @FXML private Button addTransactionButton;
    @FXML private Button updateTransactionButton;
    @FXML private Button deleteTransactionButton;
    @FXML private Button newTransactionButton;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label valueErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label categoryErrorLabel;
    @FXML private Label accountErrorLabel;
    @FXML private TextField searchField;

    private GastoPessoalService service;
    private ObservableList<Transacao> transactionsData = FXCollections.observableArrayList();
    private ObservableList<Categoria> categoriesList = FXCollections.observableArrayList();
    private ObservableList<Conta> accountsList = FXCollections.observableArrayList();

    public TransactionController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        typeComboBox.getItems().setAll(TipoCategoria.values());

        loadCategoriesForComboBox();
        loadAccountsForComboBox();

        configureComboBoxDisplay();

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterCategoriesByType(newValue);
            filterAccountsByType(newValue);
        });

        setupTableColumns();

        transactionTable.setItems(transactionsData);
        transactionTable.setPlaceholder(new Label("Nenhuma transação encontrada"));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadTransactions(newValue);
        });

        loadTransactions("");
        setFormMode(false);
        clearAllErrors();

        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTransactionDetails(newValue));
    }

    private void configureComboBoxDisplay() {
        categoryComboBox.setCellFactory(cell -> new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
        categoryComboBox.setButtonCell(new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        // Formata o ComboBox de Conta
        accountComboBox.setCellFactory(cell -> new ListCell<Conta>() {
            @Override
            protected void updateItem(Conta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
        accountComboBox.setButtonCell(new ListCell<Conta>() {
            @Override
            protected void updateItem(Conta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colValue.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(item)); }
            }
        });
        colDate.setCellValueFactory(new PropertyValueFactory<>("data"));
        colType.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCategory.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        colAccount.setCellValueFactory(new PropertyValueFactory<>("conta"));
        colAccount.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Conta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
    }

    private void showTransactionDetails(Transacao transacao) {
        clearAllErrors();
        if (transacao != null) {
            selectedTransactionId = transacao.getId();
            descriptionField.setText(transacao.getDescricao());
            valueField.setText(String.format(Locale.US, "%.2f", transacao.getValor()));
            datePicker.setValue(transacao.getData());
            typeComboBox.getSelectionModel().select(transacao.getTipo());
            categoryComboBox.getSelectionModel().select(transacao.getCategoria());
            accountComboBox.getSelectionModel().select(transacao.getConta());
            setFormMode(true);
        } else {
            handleNewTransaction(null);
        }
    }

    private void setFormMode(boolean isEditing) {
        addTransactionButton.setDisable(isEditing);
        updateTransactionButton.setDisable(!isEditing);
        deleteTransactionButton.setDisable(!isEditing);
        newTransactionButton.setDisable(false);
    }

    @FXML
    private void handleNewTransaction(ActionEvent event) {
        descriptionField.clear();
        valueField.clear();
        datePicker.setValue(null);
        typeComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
        accountComboBox.getSelectionModel().clearSelection();
        selectedTransactionId = 0;
        transactionTable.getSelectionModel().clearSelection();
        setFormMode(false);
        clearAllErrors();
        searchField.clear();
        loadTransactions("");
    }

    @FXML
    private void handleAddOrUpdateTransaction(ActionEvent event) {
        clearAllErrors();

        String description = descriptionField.getText();
        double value;
        try { value = Double.parseDouble(valueField.getText().replace(",", ".")); }
        catch (NumberFormatException e) {
            showFieldError(valueField, valueErrorLabel, "Valor inválido.");
            return;
        }
        LocalDate date = datePicker.getValue();
        TipoCategoria type = typeComboBox.getSelectionModel().getSelectedItem();
        Categoria category = categoryComboBox.getSelectionModel().getSelectedItem();
        Conta conta = accountComboBox.getSelectionModel().getSelectedItem();

        boolean isValid = true;
        if (description == null || description.trim().isEmpty()) { showFieldError(descriptionField, descriptionErrorLabel, "Descrição é obrigatória."); isValid = false; }
        if (value <= 0) { showFieldError(valueField, valueErrorLabel, "Valor deve ser positivo."); isValid = false; }
        if (date == null) { showFieldError(datePicker, dateErrorLabel, "Data é obrigatória."); isValid = false; }
        else if (date.isAfter(LocalDate.now())) { showFieldError(datePicker, dateErrorLabel, "Data não pode ser futura."); isValid = false; }
        if (type == null) { showFieldError(typeComboBox, typeErrorLabel, "Tipo é obrigatório."); isValid = false; }
        if (conta == null) { showFieldError(accountComboBox, accountErrorLabel, "Conta é obrigatória."); isValid = false; }

        if (type != null && category != null && category.getTipo() != type) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Tipo da categoria não corresponde ao tipo da transação.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        String categoriaNome = (category != null) ? category.getNome() : null;
        String contaNome = conta.getNome();

        if (type == TipoCategoria.DESPESA && category != null) {
            Orcamento orcamento = service.getOrcamentoCategoria(category, date.getMonthValue(), date.getYear());

            if (orcamento != null) {
                double gastoAtual = service.getGastoAtualCategoria(category, date.getMonthValue(), date.getYear());
                double valorAntigo = 0.0;

                if (selectedTransactionId > 0) {
                    Transacao transacaoOriginal = service.buscarTransacaoPorId(selectedTransactionId);
                    if (transacaoOriginal != null && transacaoOriginal.getCategoria() != null && transacaoOriginal.getCategoria().getId() == category.getId()) {
                        valorAntigo = transacaoOriginal.getValor();
                    }
                }

                double gastoAjustado = gastoAtual - valorAntigo;
                double novoTotalProjetado = gastoAjustado + value;
                double limite = orcamento.getValorLimite();

                if (novoTotalProjetado > limite) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Orçamento Excedido");
                    confirmAlert.setHeaderText("Atenção: Esta transação ultrapassa o seu orçamento!");
                    confirmAlert.setContentText(String.format(
                            "Categoria: %s\n" +
                                    "Limite do Mês: R$ %.2f\n\n" +
                                    "Gasto Atual: R$ %.2f\n" +
                                    "Com esta transação: R$ %.2f\n" +
                                    "(Valor excedido: R$ %.2f)\n\n" +
                                    "Deseja salvar mesmo assim?",
                            category.getNome(),
                            limite,
                            gastoAjustado,
                            novoTotalProjetado,
                            novoTotalProjetado - limite
                    ));

                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return;
                    }
                }
            }
        }

        try {
            if (selectedTransactionId == 0) {
                service.adicionarTransacao(description, value, date, type, categoriaNome, contaNome);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação adicionada com sucesso!");
            } else {
                Transacao transacaoAtualizada = new Transacao(selectedTransactionId, description, value, date, type, category, conta);
                service.atualizarTransacao(transacaoAtualizada, categoriaNome, contaNome);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação atualizada com sucesso!");
            }
            loadTransactions(searchField.getText());
            handleNewTransaction(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteTransaction(ActionEvent event) {
        if (selectedTransactionId == 0) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione uma transação na tabela para excluir.");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir Transação?");
        confirmAlert.setContentText("Tem certeza que deseja excluir a transação selecionada?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.excluirTransacao(selectedTransactionId);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação excluída com sucesso!");
                loadTransactions(searchField.getText());
                handleNewTransaction(null);
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Ocorreu um erro ao excluir a transação: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadCategoriesForComboBox() {
        categoriesList.clear();
        categoriesList.addAll(service.listarTodasCategorias());
        categoryComboBox.setItems(categoriesList);
    }

    private void loadAccountsForComboBox() {
        accountsList.clear();
        accountsList.addAll(service.listarTodasContas());
        accountComboBox.setItems(accountsList);
    }

    private void filterCategoriesByType(TipoCategoria selectedType) {
        if (selectedType == null) {
            categoryComboBox.setItems(categoriesList);
            return;
        }
        ObservableList<Categoria> filteredList = categoriesList.stream()
                .filter(cat -> cat.getTipo() == selectedType)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        categoryComboBox.setItems(filteredList);
        categoryComboBox.getSelectionModel().clearSelection();
    }

    private void filterAccountsByType(TipoCategoria selectedType) {
        if (selectedType == null) {
            accountComboBox.setItems(accountsList);
            return;
        }

        ObservableList<Conta> filteredList;
        if (selectedType == TipoCategoria.DESPESA) {
            filteredList = accountsList.stream()
                    .filter(conta -> conta.getTipo() == TipoConta.CONTA_CORRENTE ||
                            conta.getTipo() == TipoConta.DINHEIRO || // <-- ALTERADO AQUI
                            conta.getTipo() == TipoConta.CARTAO_DE_CREDITO)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        } else {
            filteredList = accountsList.stream()
                    .filter(conta -> conta.getTipo() != TipoConta.CARTAO_DE_CREDITO)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
        accountComboBox.setItems(filteredList);
        accountComboBox.getSelectionModel().clearSelection();
    }

    private void loadTransactions(String searchTerm) {
        try {
            transactionsData.clear();
            transactionsData.addAll(service.listarTransacoesPorTermo(searchTerm));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erro ao carregar transações", e.getMessage()));
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
            System.err.println("Erro ao carregar o menu principal: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar o menu principal.");
        }
    }

    @Override
    protected void clearAllErrors() {
        clearFieldError(descriptionField, descriptionErrorLabel);
        clearFieldError(valueField, valueErrorLabel);
        clearFieldError(datePicker, dateErrorLabel);
        clearFieldError(typeComboBox, typeErrorLabel);
        clearFieldError(categoryComboBox, categoryErrorLabel);
        clearFieldError(accountComboBox, accountErrorLabel);
    }
}