package com.controle.view;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
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
import java.util.Optional;

public class CategoryController extends BaseController {

    private int selectedCategoryId = 0;

    @FXML private TextField categoryNameField;
    @FXML private ComboBox<TipoCategoria> categoryTypeComboBox;
    @FXML private TableView<Categoria> categoryTable;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colName;
    @FXML private TableColumn<Categoria, TipoCategoria> colType;
    @FXML private Button addCategoryButton;
    @FXML private Button updateCategoryButton;
    @FXML private Button deleteCategoryButton;
    @FXML private Button newCategoryButton;
    @FXML private Label categoryNameErrorLabel;
    @FXML private Label categoryTypeErrorLabel;
    @FXML private Label fullScreenHintLabel;

    private final GastoPessoalService service;
    private final ObservableList<Categoria> categoriasData = FXCollections.observableArrayList();

    public CategoryController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {

        categoryTypeComboBox.getItems().setAll(TipoCategoria.values());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colType.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        categoryTable.setItems(categoriasData);
        categoryTable.setPlaceholder(new Label("Nenhuma categoria encontrada"));

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> showCategoryDetails(newV)
        );

        loadCategories("");
        setFormMode(false);
        clearAllErrors();
    }

    private void showCategoryDetails(Categoria categoria) {
        clearAllErrors();
        if (categoria != null) {
            selectedCategoryId = categoria.getId();
            categoryNameField.setText(categoria.getNome());
            categoryTypeComboBox.getSelectionModel().select(categoria.getTipo());
            setFormMode(true);
        } else {
            handleNewCategory(null);
        }
    }

    private void setFormMode(boolean isEditing) {
        addCategoryButton.setDisable(isEditing);
        updateCategoryButton.setDisable(!isEditing);
        deleteCategoryButton.setDisable(!isEditing);
        newCategoryButton.setDisable(false);
    }

    @FXML
    private void handleNewCategory(ActionEvent event) {
        categoryNameField.clear();
        categoryTypeComboBox.getSelectionModel().clearSelection();
        selectedCategoryId = 0;
        categoryTable.getSelectionModel().clearSelection();
        setFormMode(false);
        clearAllErrors();
        loadCategories("");
    }

    @FXML
    private void handleAddOrUpdateCategory(ActionEvent event) {
        clearAllErrors();

        String name = categoryNameField.getText();
        TipoCategoria type = categoryTypeComboBox.getSelectionModel().getSelectedItem();

        boolean isValid = true;
        if (name == null || name.trim().isEmpty()) {
            showFieldError(categoryNameField, categoryNameErrorLabel, "Nome da categoria é obrigatório.");
            isValid = false;
        }
        if (type == null) {
            showFieldError(categoryTypeComboBox, categoryTypeErrorLabel, "Selecione o tipo da categoria.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        try {
            if (selectedCategoryId == 0) {
                service.adicionarCategoria(name, type);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Categoria '" + name + "' adicionada com sucesso!");
            } else {
                Categoria categoriaAtualizada = new Categoria(selectedCategoryId, name, type);
                service.atualizarCategoria(categoriaAtualizada);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Categoria '" + name + "' atualizada com sucesso!");
            }
            loadCategories("");
            handleNewCategory(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCategory(ActionEvent event) {
        if (selectedCategoryId == 0) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione uma categoria na tabela para excluir.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir Categoria?");
        confirmAlert.setContentText("Tem certeza que deseja excluir a categoria selecionada?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.excluirCategoria(selectedCategoryId);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Categoria excluída com sucesso!");
                loadCategories("");
                handleNewCategory(null);
            } catch (RuntimeException e) {
                e.printStackTrace();
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (msg.contains("REFERENCE constraint") || msg.contains("referential integrity constraint violation")) {
                    showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Não é possível excluir esta categoria pois ela já está em uso por transações ou orçamentos.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Ocorreu um erro ao excluir a categoria: " + msg);
                }
            }
        }
    }

    private void loadCategories(String searchTerm) {
        try {
            categoriasData.clear();
            categoriasData.addAll(service.listarCategoriasPorTermo(searchTerm));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erro ao carregar categorias", e.getMessage()));
        }
    }

    @Override
    protected void clearAllErrors() {
        clearFieldError(categoryNameField, categoryNameErrorLabel);
        clearFieldError(categoryTypeComboBox, categoryTypeErrorLabel);
    }
}