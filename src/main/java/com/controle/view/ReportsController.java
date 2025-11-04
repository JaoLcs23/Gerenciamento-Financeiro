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
import javafx.application.Platform;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

public class ReportsController extends BaseController {

    @FXML private Label fullScreenHintLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label startDateErrorLabel;
    @FXML private Label endDateErrorLabel;
    @FXML private TableView<CategorySummary> categoryExpensesTable;
    @FXML private TableColumn<CategorySummary, String> colCategoryName;
    @FXML private TableColumn<CategorySummary, Double> colCategoryAmount;
    @FXML private PieChart categoryPieChart;

    private GastoPessoalService service;
    private ObservableList<CategorySummary> categorySummaryData = FXCollections.observableArrayList();

    public ReportsController() {
        this.service = new GastoPessoalService();
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
        categoryExpensesTable.setPlaceholder(new Label("Nenhum dado para o período"));

        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(firstDayOfCurrentMonth);
        endDatePicker.setValue(today);

        clearAllErrors();

        Platform.runLater(() -> {
            System.out.println("ReportsController: Verificando transações recorrentes pendentes...");
            service.processarTransacoesRecorrentes();
            handleGenerateReport(null);
        });
    }

    // Atualiza os labels do balanço (Receita, Despesa, Saldo)
    private void updateTotalBalance() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            totalBalanceLabel.setText("Balanço: R$ 0.00");
            totalIncomeLabel.setText("Receitas: R$ 0.00");
            totalExpensesLabel.setText("Despesas: R$ 0.00");
            totalBalanceLabel.getStyleClass().removeAll("balance-label-positive", "balance-label-negative");
            return;
        }

        try {
            double totalBalance = service.calcularBalancoTotal(startDate, endDate);
            double totalIncome = service.calcularTotalReceitas(startDate, endDate);
            double totalExpenses = service.calcularTotalDespesas(startDate, endDate);

            totalBalanceLabel.setText(String.format("Balanço: R$ %.2f", totalBalance));
            totalBalanceLabel.getStyleClass().removeAll("balance-label-positive", "balance-label-negative");
            if (totalBalance >= 0) {
                totalBalanceLabel.getStyleClass().add("balance-label-positive");
            } else {
                totalBalanceLabel.getStyleClass().add("balance-label-negative");
            }

            totalIncomeLabel.setText(String.format("Receitas: R$ %.2f", totalIncome));
            totalExpensesLabel.setText(String.format("Despesas: R$ %.2f", totalExpenses));

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Gerar Balanço", "Ocorreu um erro ao gerar o balanço: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        // Valida os seletores de data
        if (!validateDatePickers(startDatePicker, endDatePicker, startDateErrorLabel, endDateErrorLabel)) {
            showAlert(Alert.AlertType.WARNING, "Datas Inválidas", "Por favor, corrija as datas do relatório.");
            return;
        }

        // Atualiza o Balanço
        updateTotalBalance();

        // Atualiza a Tabela e o Gráfico de Categorias
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        try {
            Map<String, Double> expensesMap = service.calcularDespesasPorCategoria(startDate, endDate);
            categorySummaryData.clear();
            expensesMap.forEach((name, amount) -> categorySummaryData.add(new CategorySummary(name, amount)));
            categoryExpensesTable.refresh();
            updateCategoryPieChart(expensesMap);

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Gerar Relatório", "Ocorreu um erro ao gerar o relatório de categorias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateDatePickers(DatePicker start, DatePicker end, Label startError, Label endError) {
        clearFieldError(start, startError);
        clearFieldError(end, endError);

        LocalDate startDate = start.getValue();
        LocalDate endDate = end.getValue();
        boolean isValid = true;

        if (startDate == null) {
            showFieldError(start, startError, "Data de início obrigatória.");
            isValid = false;
        }
        if (endDate == null) {
            showFieldError(end, endError, "Data de fim obrigatória.");
            isValid = false;
        } else if (startDate != null && startDate.isAfter(endDate)) {
            showFieldError(start, startError, "Início não pode ser após o fim.");
            isValid = false;
        }
        return isValid;
    }

    private void updateCategoryPieChart(Map<String, Double> expensesMap) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        if (expensesMap.isEmpty()) {
            categoryPieChart.setData(FXCollections.emptyObservableList());
            categoryPieChart.setTitle("Distribuição de Despesas (Nenhum dado)");
            return;
        }

        double totalExpenses = expensesMap.values().stream().mapToDouble(Double::doubleValue).sum();

        expensesMap.forEach((categoryName, amount) -> {
            String label = String.format("%s (%.2f%%)", categoryName, (amount / totalExpenses * 100));
            pieChartData.add(new PieChart.Data(label, amount));
        });

        categoryPieChart.setData(pieChartData);
        categoryPieChart.setTitle("Distribuição de Despesas por Categoria");
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
        // Modificado para limpar apenas os seletores de data restantes
        clearFieldError(startDatePicker, startDateErrorLabel);
        clearFieldError(endDatePicker, endDateErrorLabel);
    }

    public static class CategorySummary {
        private final String categoryName;
        private final Double totalAmount;

        public CategorySummary(String categoryName, Double totalAmount) {
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
        }
        public String getCategoryName() { return categoryName; }
        public Double getTotalAmount() { return totalAmount; }
    }
}