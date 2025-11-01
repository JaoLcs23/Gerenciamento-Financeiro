package com.controle.view;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
import com.controle.model.Transacao;
import com.controle.service.GastoPessoalService;
import com.controle.model.Orcamento;
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

public class TransactionController extends BaseController {

    private int selectedTransactionId = 0;

    @FXML private TextField descriptionField;
    @FXML private TextField valueField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<TipoCategoria> typeComboBox;
    @FXML private ComboBox<Categoria> categoryComboBox;
    @FXML private TableView<Transacao> transactionTable;
    @FXML private TableColumn<Transacao, Integer> colId;
    @FXML private TableColumn<Transacao, String> colDescription;
    @FXML private TableColumn<Transacao, Double> colValue;
    @FXML private TableColumn<Transacao, LocalDate> colDate;
    @FXML private TableColumn<Transacao, TipoCategoria> colType;
    @FXML private TableColumn<Transacao, Categoria> colCategory;

    @FXML private Button addTransactionButton;
    @FXML private Button updateTransactionButton;
    @FXML private Button deleteTransactionButton;
    @FXML private Button newTransactionButton;

    @FXML private Label descriptionErrorLabel;
    @FXML private Label valueErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label categoryErrorLabel;
    @FXML private TextField searchField;


    private GastoPessoalService service;
    private ObservableList<Transacao> transactionsData = FXCollections.observableArrayList();
    private ObservableList<Categoria> categoriesList = FXCollections.observableArrayList();

    public TransactionController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        typeComboBox.getItems().setAll(TipoCategoria.values());

        loadCategoriesForComboBox();

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterCategoriesByType(newValue);
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colValue.setCellFactory(column -> new TableCell<Transacao, Double>() {
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

        colDate.setCellValueFactory(new PropertyValueFactory<>("data"));
        colType.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCategory.setCellFactory(column -> new TableCell<Transacao, Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNome());
                }
            }
        });

        transactionTable.setItems(transactionsData);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadTransactions(newValue);
        });

        loadTransactions("");
        setFormMode(false);
        clearAllErrors();

        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTransactionDetails(newValue));
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
        try {
            value = Double.parseDouble(valueField.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            showFieldError(valueField, valueErrorLabel, "Valor inválido. Use números e '.' para decimais.");
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }
        LocalDate date = datePicker.getValue();
        TipoCategoria type = typeComboBox.getSelectionModel().getSelectedItem();
        Categoria category = categoryComboBox.getSelectionModel().getSelectedItem();

        boolean isValid = true;
        if (description == null || description.trim().isEmpty()) {
            showFieldError(descriptionField, descriptionErrorLabel, "Descrição é obrigatória.");
            isValid = false;
        }
        if (value <= 0) {
            showFieldError(valueField, valueErrorLabel, "Valor deve ser positivo.");
            isValid = false;
        }
        if (date == null) {
            showFieldError(datePicker, dateErrorLabel, "Data é obrigatória.");
            isValid = false;
        } else if (date.isAfter(LocalDate.now())) {
            showFieldError(datePicker, dateErrorLabel, "Data não pode ser futura.");
            isValid = false;
        }
        if (type == null) {
            showFieldError(typeComboBox, typeErrorLabel, "Tipo é obrigatório.");
            isValid = false;
        }
        if (category == null) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Categoria é obrigatória.");
            isValid = false;
        }

        if (type != null && category != null && category.getTipo() != type) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Tipo da categoria não corresponde ao tipo da transação.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        // LÓGICA DE VERIFICAÇÃO DE ORÇAMENTO
        if (type == TipoCategoria.DESPESA) {
            Orcamento orcamento = service.getOrcamentoCategoria(category, date.getMonthValue(), date.getYear());

            if (orcamento != null) {
                double gastoAtual = service.getGastoAtualCategoria(category, date.getMonthValue(), date.getYear());

                double valorAntigo = 0.0;
                if (selectedTransactionId > 0) {
                    Transacao transacaoOriginal = service.buscarTransacaoPorId(selectedTransactionId);

                    if (transacaoOriginal != null && transacaoOriginal.getCategoria().getId() == category.getId()) {
                        valorAntigo = transacaoOriginal.getValor();
                    }
                }

                double gastoSemEstaTransacao = gastoAtual - valorAntigo;
                double novoTotalProjetado = gastoSemEstaTransacao + value;

                if (novoTotalProjetado > orcamento.getValorLimite()) {
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
                            orcamento.getValorLimite(),
                            gastoSemEstaTransacao,
                            novoTotalProjetado,
                            novoTotalProjetado - orcamento.getValorLimite()
                    ));

                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return; // Usuário clicou em "Cancelar"
                    }
                }
            }
        }

        try {
            if (selectedTransactionId == 0) {
                service.adicionarTransacao(description, value, date, type, category.getNome());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação adicionada com sucesso!");
            } else {
                Transacao transacaoAtualizada = new Transacao(selectedTransactionId, description, value, date, type, category);
                service.atualizarTransacao(transacaoAtualizada, category.getNome());
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

    private void filterCategoriesByType(TipoCategoria selectedType) {
        if (selectedType == null) {
            categoryComboBox.setItems(categoriesList);
            return;
        }
        ObservableList<Categoria> filteredList = FXCollections.observableArrayList();
        for (Categoria cat : categoriesList) {
            if (cat.getTipo() == selectedType) {
                filteredList.add(cat);
            }
        }
        categoryComboBox.setItems(filteredList);
        categoryComboBox.getSelectionModel().clearSelection();
    }

    private void loadTransactions(String searchTerm) {
        transactionsData.clear();
        transactionsData.addAll(service.listarTransacoesPorTermo(searchTerm));
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
            Platform.runLater(() -> primaryStage.setMaximized(true));
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
    }
}