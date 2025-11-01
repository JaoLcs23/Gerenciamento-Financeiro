package com.controle.view;

import com.controle.model.Categoria;
import com.controle.model.Orcamento;
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

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controlador para a tela de gerenciamento de Orçamentos.
 * Estende BaseController.
 */
public class BudgetController extends BaseController {

    private int selectedBudgetId = 0;

    // Componentes FXML do Formulário
    @FXML private ComboBox<Categoria> categoryComboBox;
    @FXML private TextField valorLimiteField;
    @FXML private ComboBox<Integer> mesComboBox;
    @FXML private ComboBox<Integer> anoComboBox;

    // Componentes FXML da Tabela e Filtros
    @FXML private TableView<Orcamento> budgetTable;
    @FXML private TableColumn<Orcamento, Integer> colId;
    @FXML private TableColumn<Orcamento, Categoria> colCategoria;
    @FXML private TableColumn<Orcamento, Double> colValorLimite;
    @FXML private TableColumn<Orcamento, Integer> colMes;
    @FXML private TableColumn<Orcamento, Integer> colAno;
    @FXML private ComboBox<Integer> filtroMesComboBox;
    @FXML private ComboBox<Integer> filtroAnoComboBox;

    // Botões
    @FXML private Button addBudgetButton;
    @FXML private Button updateBudgetButton;
    @FXML private Button deleteBudgetButton;
    @FXML private Button newBudgetButton;

    // Labels de Erro
    @FXML private Label categoryErrorLabel;
    @FXML private Label valorLimiteErrorLabel;
    @FXML private Label mesErrorLabel;
    @FXML private Label anoErrorLabel;

    // Camada de Serviço e Listas
    private GastoPessoalService service;
    private ObservableList<Orcamento> orcamentosData = FXCollections.observableArrayList();
    private ObservableList<Categoria> categoriesList = FXCollections.observableArrayList();

    public BudgetController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        // Popula os ComboBoxes de Mês e Ano
        ObservableList<Integer> meses = FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
        mesComboBox.setItems(meses);
        filtroMesComboBox.setItems(meses);

        int anoAtual = LocalDate.now().getYear();
        ObservableList<Integer> anos = FXCollections.observableArrayList(anoAtual - 1, anoAtual, anoAtual + 1, anoAtual + 2);
        anoComboBox.setItems(anos);
        filtroAnoComboBox.setItems(anos);

        // Define os filtros padrão
        filtroMesComboBox.setValue(LocalDate.now().getMonthValue());
        filtroAnoComboBox.setValue(anoAtual);

        // Carrega apenas categorias de DESPESA no formulário
        loadCategoriesForComboBox();

        // Configura as Colunas da Tabela
        setupTableColumns();

        // Listeners dos filtros
        filtroMesComboBox.setOnAction(e -> loadBudgets());
        filtroAnoComboBox.setOnAction(e -> loadBudgets());

        // Listener da seleção da tabela
        budgetTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBudgetDetails(newValue));

        loadBudgets(); // Carga inicial
        setFormMode(false);
        clearAllErrors();
    }

    /**
     * Carrega as categorias do tipo DESPESA no ComboBox do formulário.
     */
    private void loadCategoriesForComboBox() {
        categoriesList.clear();
        // Filtra para mostrar APENAS categorias de despesa
        categoriesList.addAll(service.listarTodasCategorias().stream()
                .filter(c -> c.getTipo() == TipoCategoria.DESPESA)
                .collect(Collectors.toList()));
        categoryComboBox.setItems(categoriesList);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colValorLimite.setCellValueFactory(new PropertyValueFactory<>("valorLimite"));
        colMes.setCellValueFactory(new PropertyValueFactory<>("mes"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("ano"));

        // Formatação de Células
        colValorLimite.setCellFactory(column -> new TableCell<>() {
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

        colCategoria.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        budgetTable.setItems(orcamentosData);
    }

    /**
     * Carrega/Recarrega os orçamentos na tabela com base nos filtros.
     */
    private void loadBudgets() {
        orcamentosData.clear();
        Integer mes = filtroMesComboBox.getValue();
        Integer ano = filtroAnoComboBox.getValue();
        orcamentosData.addAll(service.listarOrcamentosPorPeriodo(mes, ano));
    }

    private void showBudgetDetails(Orcamento orcamento) {
        clearAllErrors();
        if (orcamento != null) {
            selectedBudgetId = orcamento.getId();
            categoryComboBox.setValue(orcamento.getCategoria());
            valorLimiteField.setText(String.format(Locale.US, "%.2f", orcamento.getValorLimite()));
            mesComboBox.setValue(orcamento.getMes());
            anoComboBox.setValue(orcamento.getAno());
            setFormMode(true);
        } else {
            handleNewBudget(null);
        }
    }

    private void setFormMode(boolean isEditing) {
        addBudgetButton.setDisable(isEditing);
        updateBudgetButton.setDisable(!isEditing);
        deleteBudgetButton.setDisable(!isEditing);
        newBudgetButton.setDisable(false);
    }

    @FXML
    private void handleNewBudget(ActionEvent event) {
        categoryComboBox.getSelectionModel().clearSelection();
        valorLimiteField.clear();
        mesComboBox.getSelectionModel().clearSelection();
        anoComboBox.getSelectionModel().clearSelection();
        selectedBudgetId = 0;
        budgetTable.getSelectionModel().clearSelection();
        setFormMode(false);
        clearAllErrors();
    }

    @FXML
    private void handleAddOrUpdateBudget(ActionEvent event) {
        clearAllErrors();

        // Coleta e Validação
        Categoria categoria = categoryComboBox.getValue();
        Integer mes = mesComboBox.getValue();
        Integer ano = anoComboBox.getValue();
        double valor = 0.0;

        boolean isValid = true;

        if (categoria == null) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Categoria é obrigatória.");
            isValid = false;
        } else if (categoria.getTipo() != TipoCategoria.DESPESA) {
            showFieldError(categoryComboBox, categoryErrorLabel, "Orçamento só para Despesas.");
            isValid = false;
        }

        try {
            valor = Double.parseDouble(valorLimiteField.getText().replace(",", "."));
            if (valor <= 0) {
                showFieldError(valorLimiteField, valorLimiteErrorLabel, "Valor deve ser positivo.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showFieldError(valorLimiteField, valorLimiteErrorLabel, "Valor inválido. Use números.");
            isValid = false;
        }

        if (mes == null) {
            showFieldError(mesComboBox, mesErrorLabel, "Mês é obrigatório.");
            isValid = false;
        }
        if (ano == null) {
            showFieldError(anoComboBox, anoErrorLabel, "Ano é obrigatório.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        // Persistência
        try {
            Orcamento orcamento = new Orcamento(categoria, valor, mes, ano);

            if (selectedBudgetId == 0) {
                service.adicionarOrcamento(orcamento);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Orçamento adicionado!");
            } else {
                orcamento.setId(selectedBudgetId);
                service.atualizarOrcamento(orcamento);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Orçamento atualizado!");
            }
            loadBudgets(); // Recarrega a tabela
            handleNewBudget(null); // Limpa o formulário
        } catch (RuntimeException e) {
            // Captura o erro de UNIQUE (UQ_Categoria_Mes_Ano) do DAO
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteBudget(ActionEvent event) {
        if (selectedBudgetId == 0) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Selecione um orçamento na tabela para excluir.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir Orçamento?");
        confirmAlert.setContentText("Tem certeza que deseja excluir este orçamento?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.excluirOrcamento(selectedBudgetId);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Orçamento excluído!");
                loadBudgets();
                handleNewBudget(null);
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Ocorreu um erro: " + e.getMessage());
                e.printStackTrace();
            }
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
        clearFieldError(categoryComboBox, categoryErrorLabel);
        clearFieldError(valorLimiteField, valorLimiteErrorLabel);
        clearFieldError(mesComboBox, mesErrorLabel);
        clearFieldError(anoComboBox, anoErrorLabel);
    }
}