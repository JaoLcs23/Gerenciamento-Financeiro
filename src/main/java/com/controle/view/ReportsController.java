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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    @FXML private Button exportPdfButton;
    @FXML private LineChart<String, Number> patrimonioLineChart;
    @FXML private CategoryAxis xAxisDate;
    @FXML private NumberAxis yAxisValue;

    private GastoPessoalService service;
    private ObservableList<CategorySummary> categorySummaryData = FXCollections.observableArrayList();
    private Locale brLocale = new Locale("pt", "BR");
    private DateTimeFormatter lineChartDateFormatter = DateTimeFormatter.ofPattern("dd/MM");

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
                    setText(NumberFormat.getCurrencyInstance(brLocale).format(item));
                }
            }
        });

        categoryExpensesTable.setItems(categorySummaryData);
        categoryExpensesTable.setPlaceholder(new Label("Nenhum dado para o período"));

        categoryPieChart.setLabelsVisible(false);

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

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(brLocale);

            totalBalanceLabel.setText(String.format("Balanço: %s", currencyFormat.format(totalBalance)));
            totalBalanceLabel.getStyleClass().removeAll("balance-label-positive", "balance-label-negative");
            if (totalBalance >= 0) {
                totalBalanceLabel.getStyleClass().add("balance-label-positive");
            } else {
                totalBalanceLabel.getStyleClass().add("balance-label-negative");
            }

            totalIncomeLabel.setText(String.format("Receitas: %s", currencyFormat.format(totalIncome)));
            totalExpensesLabel.setText(String.format("Despesas: %s", currencyFormat.format(totalExpenses)));

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Gerar Balanço", "Ocorreu um erro ao gerar o balanço: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        if (!validateDatePickers(startDatePicker, endDatePicker, startDateErrorLabel, endDateErrorLabel)) {
            showAlert(Alert.AlertType.WARNING, "Datas Inválidas", "Por favor, corrija as datas do relatório.");
            exportPdfButton.setDisable(true);
            return;
        }

        updateTotalBalance();

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        try {
            Map<String, Double> expensesMap = service.calcularDespesasPorCategoria(startDate, endDate);
            categorySummaryData.clear();
            expensesMap.forEach((name, amount) -> categorySummaryData.add(new CategorySummary(name, amount)));
            categoryExpensesTable.refresh();
            updateCategoryPieChart(expensesMap);

            updatePatrimonioLineChart(startDate, endDate);

            exportPdfButton.setDisable(false);

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao Gerar Relatório", "Ocorreu um erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
            exportPdfButton.setDisable(true);
        }
    }

    private void updatePatrimonioLineChart(LocalDate startDate, LocalDate endDate) {
        patrimonioLineChart.getData().clear();

        try {
            Map<LocalDate, Double> dadosEvolucao = service.getPatrimonioEvolucao(startDate, endDate);

            if(dadosEvolucao.isEmpty()) {
                patrimonioLineChart.setTitle("Sem dados de patrimônio para o período");
                yAxisValue.setLowerBound(0);
                yAxisValue.setUpperBound(100);
                yAxisValue.setTickUnit(20);
                return;
            }

            patrimonioLineChart.setTitle("Evolução do Patrimônio");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Patrimônio");

            double minPatrimonio = Double.MAX_VALUE;
            double maxPatrimonio = Double.MIN_VALUE;

            for (Map.Entry<LocalDate, Double> entry : dadosEvolucao.entrySet()) {
                String dataFormatada = entry.getKey().format(lineChartDateFormatter);
                double valor = entry.getValue();
                series.getData().add(new XYChart.Data<>(dataFormatada, valor));

                if (valor < minPatrimonio) minPatrimonio = valor;
                if (valor > maxPatrimonio) maxPatrimonio = valor;
            }

            patrimonioLineChart.getData().add(series);

            double range = maxPatrimonio - minPatrimonio;
            double padding;

            if (range == 0) {
                padding = maxPatrimonio * 0.1;
                if (padding == 0) padding = 100;
            } else {
                padding = range * 0.1;
            }

            yAxisValue.setLowerBound(minPatrimonio - padding);
            yAxisValue.setUpperBound(maxPatrimonio + padding);

            double tickUnit = (yAxisValue.getUpperBound() - yAxisValue.getLowerBound()) / 5;
            yAxisValue.setTickUnit(tickUnit);

            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(
                        data.getXValue() + "\n" +
                                NumberFormat.getCurrencyInstance(brLocale).format(data.getYValue())
                );
                Tooltip.install(data.getNode(), tooltip);
            }

        } catch (Exception e) {
            patrimonioLineChart.setTitle("Erro ao carregar gráfico de patrimônio");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        // ... (seu método de exportar PDF - sem mudanças) ...
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Salvar PDF em...");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory == null) {
            showAlert(Alert.AlertType.INFORMATION, "Cancelado", "Exportação de PDF cancelada.");
            return;
        }

        String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = "Relatorio_Financeiro_" + dateString + ".pdf";
        File file = new File(selectedDirectory, fileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float y = 750;

            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18, 150, y, "Relatório Financeiro");
            y -= 30;

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12, 70, y,
                    "Período de: " + startDate.format(dtf) + " a " + endDate.format(dtf));
            y -= 25;

            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14, 70, y, "Balanço do Período");
            y -= 20;
            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12, 70, y, totalIncomeLabel.getText());
            y -= 20;
            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12, 70, y, totalExpensesLabel.getText());
            y -= 20;
            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12, 70, y, totalBalanceLabel.getText());
            y -= 30;

            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14, 70, y, "Despesas por Categoria");
            y -= 20;

            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12, 70, y, "Categoria");
            writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12, 350, y, "Valor");
            y -= 15;

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(brLocale);
            for (CategorySummary summary : categorySummaryData) {
                writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12, 70, y, summary.getCategoryName());
                writeText(contentStream, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12, 350, y, currencyFormat.format(summary.getTotalAmount()));
                y -= 15;
            }

            contentStream.close();
            document.save(file);

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Relatório PDF salvo com sucesso em:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro ao Salvar PDF", "Não foi possível salvar o arquivo PDF: " + e.getMessage());
        }
    }

    private void writeText(PDPageContentStream contentStream, PDType1Font font, int fontSize, float x, float y, String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
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