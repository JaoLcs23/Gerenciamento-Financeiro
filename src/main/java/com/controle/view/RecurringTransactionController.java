package com.controle.view;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
import com.controle.model.TransacaoRecorrente;
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

public class RecurringTransactionController extends BaseController {

    private int selectedRecurringId = 0;

    @FXML private Label fullScreenHintLabel;

    @FXML private TextField descriptionField;
    @FXML private TextField valueField;
    @FXML private ComboBox<TipoCategoria> typeComboBox;
    @FXML private ComboBox<Categoria> categoryComboBox;
    @FXML private TextField diaDoMesField;
    @FXML private DatePicker dataInicioPicker;
    @FXML private DatePicker dataFimPicker;

    @FXML private TableView<TransacaoRecorrente> recurringTransactionTable;
    @FXML private TableColumn<TransacaoRecorrente, Integer> colId;
    @FXML private TableColumn<TransacaoRecorrente, String> colDescription;
    @FXML private TableColumn<TransacaoRecorrente, Double> colValue;
    @FXML private TableColumn<TransacaoRecorrente, Categoria> colCategory;
    @FXML private TableColumn<TransacaoRecorrente, Integer> colDiaDoMes;
    @FXML private TableColumn<TransacaoRecorrente, LocalDate> colDataInicio;
    @FXML private TableColumn<TransacaoRecorrente, LocalDate> colDataFim;
    @FXML private TableColumn<TransacaoRecorrente, LocalDate> colUltimoProcessamento;

    @FXML private Button addRecurringButton;
    @FXML private Button updateRecurringButton;
    @FXML private Button deleteRecurringButton;
    @FXML private Button newRecurringButton;

    @FXML private Label descriptionErrorLabel;
    @FXML private Label valueErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label categoryErrorLabel;
    @FXML private Label diaDoMesErrorLabel;
    @FXML private Label dataInicioErrorLabel;
    @FXML private Label dataFimErrorLabel;

    @FXML private TextField searchField;

    private GastoPessoalService service;
    private ObservableList<TransacaoRecorrente> recurringTransactionsData = FXCollections.observableArrayList();
    private ObservableList<Categoria> categoriesList = FXCollections.observableArrayList();

    public RecurringTransactionController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        typeComboBox.getItems().setAll(TipoCategoria.values());
        loadCategoriesForComboBox();

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterCategoriesByType(newValue);
        });

        setupTableColumns();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadRecurringTransactions(newValue);
        });

        recurringTransactionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showRecurringDetails(newValue));

        loadRecurringTransactions("");
        setFormMode(false);
        clearAllErrors();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDiaDoMes.setCellValueFactory(new PropertyValueFactory<>("diaDoMes"));
        colDataInicio.setCellValueFactory(new PropertyValueFactory<>("dataInicio"));
        colDataFim.setCellValueFactory(new PropertyValueFactory<>("dataFim"));
        colUltimoProcessamento.setCellValueFactory(new PropertyValueFactory<>("dataUltimoProcessamento"));

        colValue.setCellFactory(column -> new TableCell<>() {
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

        colCategory.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        recurringTransactionTable.setItems(recurringTransactionsData);
        recurringTransactionTable.setPlaceholder(new Label("Nenhuma recorrência encontrada"));
    }

    private void showRecurringDetails(TransacaoRecorrente tr) {
        clearAllErrors();
        if (tr != null) {
            selectedRecurringId = tr.getId();
            descriptionField.setText(tr.getDescricao());
            valueField.setText(String.format(Locale.US, "%.2f", tr.getValor()));
            typeComboBox.getSelectionModel().select(tr.getTipo());
            categoryComboBox.getSelectionModel().select(tr.getCategoria());
            diaDoMesField.setText(String.valueOf(tr.getDiaDoMes()));
            dataInicioPicker.setValue(tr.getDataInicio());
            dataFimPicker.setValue(tr.getDataFim()); // Pode ser nulo
            setFormMode(true);
        } else {
            handleNewRecurringTransaction(null);
        }
    }

    private void setFormMode(boolean isEditing) {
        addRecurringButton.setDisable(isEditing);
        updateRecurringButton.setDisable(!isEditing);
        deleteRecurringButton.setDisable(!isEditing);
        newRecurringButton.setDisable(false);
    }

    @FXML
    private void handleNewRecurringTransaction(ActionEvent event) {
        descriptionField.clear();
        valueField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
        diaDoMesField.clear();
        dataInicioPicker.setValue(null);
        dataFimPicker.setValue(null);
        selectedRecurringId = 0;
        recurringTransactionTable.getSelectionModel().clearSelection();
        setFormMode(false);
        clearAllErrors();
        searchField.clear();
        loadRecurringTransactions("");
    }

    @FXML
    private void handleAddOrUpdateRecurringTransaction(ActionEvent event) {
        clearAllErrors();

        String description = descriptionField.getText();
        TipoCategoria type = typeComboBox.getSelectionModel().getSelectedItem();
        Categoria category = categoryComboBox.getSelectionModel().getSelectedItem();
        LocalDate dataInicio = dataInicioPicker.getValue();
        LocalDate dataFim = dataFimPicker.getValue();

        double value = 0.0;
        int diaDoMes = 0;
        boolean isValid = true;

        if (description == null || description.trim().isEmpty()) {
            showFieldError(descriptionField, descriptionErrorLabel, "Descrição é obrigatória.");
            isValid = false;
        }

        try {
            value = Double.parseDouble(valueField.getText().replace(",", "."));
            if (value <= 0) {
                showFieldError(valueField, valueErrorLabel, "Valor deve ser positivo.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showFieldError(valueField, valueErrorLabel, "Valor inválido. Use números.");
            isValid = false;
        }

        try {
            diaDoMes = Integer.parseInt(diaDoMesField.getText());
            if (diaDoMes < 1 || diaDoMes > 31) {
                showFieldError(diaDoMesField, diaDoMesErrorLabel, "Dia deve ser entre 1 e 31.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showFieldError(diaDoMesField, diaDoMesErrorLabel, "Dia inválido.");
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
        if (dataInicio == null) {
            showFieldError(dataInicioPicker, dataInicioErrorLabel, "Data de início é obrigatória.");
            isValid = false;
        }
        if (dataFim != null && dataInicio != null && dataFim.isBefore(dataInicio)) {
            showFieldError(dataFimPicker, dataFimErrorLabel, "Data fim não pode ser antes do início.");
            isValid = false;
        }

        if (type != null && category != null && category.getTipo() != type) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Tipo da categoria não bate com o tipo da transação.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        try {
            TransacaoRecorrente tr = new TransacaoRecorrente();
            tr.setDescricao(description.trim());
            tr.setValor(value);
            tr.setTipo(type);
            tr.setCategoria(category);
            tr.setDiaDoMes(diaDoMes);
            tr.setDataInicio(dataInicio);
            tr.setDataFim(dataFim);

            if (selectedRecurringId == 0) {
                service.adicionarTransacaoRecorrente(tr);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação recorrente adicionada!");
            } else {
                tr.setId(selectedRecurringId);
                TransacaoRecorrente existente = service.buscarTransacaoRecorrentePorId(selectedRecurringId);
                if(existente != null) {
                    tr.setDataUltimoProcessamento(existente.getDataUltimoProcessamento());
                }

                service.atualizarTransacaoRecorrente(tr);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação recorrente atualizada!");
            }
            loadRecurringTransactions(searchField.getText());
            handleNewRecurringTransaction(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteRecurringTransaction(ActionEvent event) {
        if (selectedRecurringId == 0) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Selecione uma transação recorrente para excluir.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir Transação Recorrente?");
        confirmAlert.setContentText("Isso vai parar lançamentos futuros, mas não afeta transações já lançadas. Continuar?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.excluirTransacaoRecorrente(selectedRecurringId);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Transação recorrente excluída!");
                loadRecurringTransactions(searchField.getText());
                handleNewRecurringTransaction(null);
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Ocorreu um erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadRecurringTransactions(String searchTerm) {
        try {
            recurringTransactionsData.clear();
            recurringTransactionsData.addAll(service.listarTransacoesRecorrentesPorTermo(searchTerm));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erro ao carregar recorrências", e.getMessage()));
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
        clearFieldError(typeComboBox, typeErrorLabel);
        clearFieldError(categoryComboBox, categoryErrorLabel);
        clearFieldError(diaDoMesField, diaDoMesErrorLabel);
        clearFieldError(dataInicioPicker, dataInicioErrorLabel);
        clearFieldError(dataFimPicker, dataFimErrorLabel);
    }
}