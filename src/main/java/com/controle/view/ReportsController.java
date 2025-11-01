package com.controle.view;

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
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

//Controlador da tela de relatórios financeiros
public class ReportsController {

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML private Label totalBalanceLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;

    @FXML private DatePicker balanceStartDatePicker;
    @FXML private DatePicker balanceEndDatePicker;

    @FXML private Label balanceStartDateErrorLabel;
    @FXML private Label balanceEndDatePickerErrorLabel;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Label startDateErrorLabel;
    @FXML private Label endDateErrorLabel;


    @FXML private TableView<CategorySummary> categoryExpensesTable;
    @FXML private TableColumn<CategorySummary, String> colCategoryName;
    @FXML private TableColumn<CategorySummary, Double> colCategoryAmount;

    // Componente PieChart
    @FXML private PieChart categoryPieChart;


    private GastoPessoalService service;
    private ObservableList<CategorySummary> categorySummaryData = FXCollections.observableArrayList();

    public ReportsController() {
        this.service = new GastoPessoalService(); //
    }

    @FXML
    public void initialize() {
        colCategoryName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colCategoryAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colCategoryAmount.setCellFactory(column -> new TableCell<CategorySummary, Double>() {
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

        categoryExpensesTable.setItems(categorySummaryData);

        // Define as datas padrão para o balanço e categorias (ex: mês atual)
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        balanceStartDatePicker.setValue(firstDayOfCurrentMonth);
        balanceEndDatePicker.setValue(today);
        startDatePicker.setValue(firstDayOfCurrentMonth);
        endDatePicker.setValue(today);

        clearAllErrors();

        // --- INÍCIO DA CORREÇÃO ---
        // Garante que a tabela de transações está atualizada ANTES de gerar o relatório
        System.out.println("ReportsController: Verificando transações recorrentes pendentes...");
        service.processarTransacoesRecorrentes(); //
        // --- FIM DA CORREÇÃO ---

        handleGenerateBalanceReport(null);
        handleGenerateCategoryReport(null);
    }

    private void updateTotalBalance() {
        LocalDate startDate = balanceStartDatePicker.getValue();
        LocalDate endDate = balanceEndDatePicker.getValue();

        // Garante que o balanço seja atualizado com as datas corretas, mesmo que a validacao ocorra
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            // Se as datas são inválidas, apenas mostra valores padrão ou 0
            totalBalanceLabel.setText("Balanço: R$ 0.00");
            totalIncomeLabel.setText("Receitas: R$ 0.00");
            totalExpensesLabel.setText("Despesas: R$ 0.00");
            totalBalanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: gray;");
            return;
        }

        try {
            double totalBalance = service.calcularBalancoTotal(startDate, endDate);
            double totalIncome = service.calcularTotalReceitas(startDate, endDate);
            double totalExpenses = service.calcularTotalDespesas(startDate, endDate);

            totalBalanceLabel.setText(String.format("Balanço: R$ %.2f", totalBalance));
            if (totalBalance >= 0) {
                totalBalanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: green;");
            } else {
                totalBalanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: red;");
            }

            totalIncomeLabel.setText(String.format("Receitas: R$ %.2f", totalIncome));
            totalExpensesLabel.setText(String.format("Despesas: R$ %.2f", totalExpenses));

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Gerar Balanço", "Ocorreu um erro ao gerar o balanço: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateBalanceReport(ActionEvent event) {
        clearFieldError(balanceStartDatePicker, balanceStartDateErrorLabel);
        clearFieldError(balanceEndDatePicker, balanceEndDatePickerErrorLabel);

        LocalDate startDate = balanceStartDatePicker.getValue();
        LocalDate endDate = balanceEndDatePicker.getValue();

        boolean isValid = true;
        if (startDate == null) {
            showFieldError(balanceStartDatePicker, balanceStartDateErrorLabel, "Data de início obrigatória.");
            isValid = false;
        }
        if (endDate == null) {
            showFieldError(balanceEndDatePicker, balanceEndDatePickerErrorLabel, "Data de fim obrigatória.");
            isValid = false;
        } else if (startDate != null && startDate.isAfter(endDate)) {
            showFieldError(balanceStartDatePicker, balanceStartDateErrorLabel, "Início não pode ser após o fim.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Datas Inválidas", "Por favor, corrija as datas do balanço.");
            return;
        }

        updateTotalBalance(); // Atualiza o balanço com as datas validadas
    }

    //Gera o relatório de despesas por categoria com base nas datas selecionadas
    @FXML
    private void handleGenerateCategoryReport(ActionEvent event) {
        clearFieldError(startDatePicker, startDateErrorLabel);
        clearFieldError(endDatePicker, endDateErrorLabel);

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        System.out.println("Relatório de Categorias: Data Início = " + startDate + ", Data Fim = " + endDate);

        boolean isValid = true;
        if (startDate == null) {
            showFieldError(startDatePicker, startDateErrorLabel, "Data de início obrigatória.");
            isValid = false;
        }
        if (endDate == null) {
            showFieldError(endDatePicker, endDateErrorLabel, "Data de fim obrigatória.");
            isValid = false;
        } else if (startDate != null && startDate.isAfter(endDate)) {
            showFieldError(startDatePicker, startDateErrorLabel, "Início não pode ser após o fim.");
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Datas Inválidas", "Por favor, corrija as datas do relatório de categorias.");
            return;
        }

        try {
            Map<String, Double> expensesMap = service.calcularDespesasPorCategoria(startDate, endDate);

            System.out.println("Relatório de Categorias: Mapa de despesas recebido (tamanho) = " + expensesMap.size());
            expensesMap.forEach((k, v) -> System.out.println("  " + k + ": " + v));

            categorySummaryData.clear();
            expensesMap.forEach((name, amount) -> categorySummaryData.add(new CategorySummary(name, amount)));

            categoryExpensesTable.refresh(); // Força o refresh da tabela

            //Atualiza o gráfico de pizza
            updateCategoryPieChart(expensesMap);

            System.out.println("Relatório de Categorias: categorySummaryData populado (tamanho) = " + categorySummaryData.size());

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Gerar Relatório", "Ocorreu um erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Atualiza o gráfico de pizza com os dados de despesas por categoria
    private void updateCategoryPieChart(Map<String, Double> expensesMap) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        if (expensesMap.isEmpty()) {
            categoryPieChart.setData(FXCollections.emptyObservableList()); // Limpa o gráfico se nao houver dados
            categoryPieChart.setTitle("Distribuição de Despesas (Nenhum dado)");
            return;
        }

        double totalExpenses = expensesMap.values().stream().mapToDouble(Double::doubleValue).sum();

        expensesMap.forEach((categoryName, amount) -> {
            // Calcula a porcentagem para exibir no nome da fatia
            String label = String.format("%s (%.2f%%)", categoryName, (amount / totalExpenses * 100));
            pieChartData.add(new PieChart.Data(label, amount));
        });

        categoryPieChart.setData(pieChartData);
        categoryPieChart.setTitle("Distribuição de Despesas por Categoria"); // Redefine o titulo
    }

    @FXML
    private void handleGoBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/controle/view/MainMenuView.fxml"));
            Parent root = loader.load();

            MenuController menuController = loader.getController();
            menuController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            // Carregando o CSS na nova Scene
            scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm()); //

            primaryStage.setScene(scene);
            primaryStage.setTitle("Controle de Gastos Pessoais - Menu Principal");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar o menu principal: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar o menu principal.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Métodos Auxiliares para Feedback de Validação ---

    private void showFieldError(Control control, Label errorLabel, String message) {
        if (!control.getStyleClass().contains("text-field-error")) {
            control.getStyleClass().add("text-field-error");
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearFieldError(Control control, Label errorLabel) {
        control.getStyleClass().remove("text-field-error");
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private void clearAllErrors() {
        clearFieldError(balanceStartDatePicker, balanceStartDateErrorLabel);
        clearFieldError(balanceEndDatePicker, balanceEndDatePickerErrorLabel);
        clearFieldError(startDatePicker, startDateErrorLabel);
        clearFieldError(endDatePicker, endDateErrorLabel);
    }

    // Classe auxiliar para o TableView de resumo de categorias
    public static class CategorySummary {
        private final String categoryName;
        private final Double totalAmount;

        public CategorySummary(String categoryName, Double totalAmount) {
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        @Override
        public String toString() {
            return "CategorySummary{" +
                    "categoryName='" + categoryName + '\'' +
                    ", totalAmount=" + totalAmount +
                    '}';
        }
    }
}