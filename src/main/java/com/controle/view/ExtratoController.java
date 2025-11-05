package com.controle.view;

import com.controle.model.Conta;
import com.controle.model.TipoCategoria;
import com.controle.model.Transacao;
import com.controle.service.GastoPessoalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class ExtratoController {

    @FXML private Label contaNomeLabel;
    @FXML private Label saldoInicialLabel;
    @FXML private Label totalEntradasLabel;
    @FXML private Label totalSaidasLabel;
    @FXML private Label saldoAtualLabel;
    @FXML private TableView<Transacao> transacoesTable;
    @FXML private TableColumn<Transacao, LocalDate> colData;
    @FXML private TableColumn<Transacao, String> colDescricao;
    @FXML private TableColumn<Transacao, Double> colValor;

    private GastoPessoalService service;
    private ObservableList<Transacao> transacoesData = FXCollections.observableArrayList();
    private Locale brLocale = new Locale("pt", "BR");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(brLocale);

    public ExtratoController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colValor.setCellFactory(column -> new TableCell<Transacao, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("balance-label-positive", "balance-label-negative");
                } else {
                    Transacao transacao = getTableView().getItems().get(getIndex());

                    if (transacao.getTipo() == TipoCategoria.RECEITA) {
                        setText(currencyFormat.format(item));
                        if (!getStyleClass().contains("balance-label-positive")) {
                            getStyleClass().add("balance-label-positive");
                        }
                    } else {
                        setText(currencyFormat.format(-item));
                        if (!getStyleClass().contains("balance-label-negative")) {
                            getStyleClass().add("balance-label-negative");
                        }
                    }
                }
            }
        });

        transacoesTable.setItems(transacoesData);
        transacoesTable.setPlaceholder(new Label("Nenhuma transação encontrada para esta conta."));
    }

    public void initData(Conta conta, double saldoAtualCalculado) {
        if (conta == null) {
            return;
        }

        contaNomeLabel.setText(conta.getNome());
        saldoInicialLabel.setText(currencyFormat.format(conta.getSaldoInicial()));

        saldoAtualLabel.setText(currencyFormat.format(saldoAtualCalculado));

        if (saldoAtualCalculado < 0) {
            saldoAtualLabel.getStyleClass().add("balance-label-negative");
        } else {
            saldoAtualLabel.getStyleClass().add("balance-label-positive");
        }

        try {
            double totalEntradas = service.getTotalReceitasPorConta(conta.getId());
            double totalSaidas = service.getTotalDespesasPorConta(conta.getId());

            totalEntradasLabel.setText(currencyFormat.format(totalEntradas));
            totalSaidasLabel.setText(currencyFormat.format(-totalSaidas));

        } catch (RuntimeException e) {
            totalEntradasLabel.setText("Erro");
            totalSaidasLabel.setText("Erro");
            e.printStackTrace();
        }

        try {
            transacoesData.setAll(service.listarTransacoesPorConta(conta.getId()));
        } catch (RuntimeException e) {
            transacoesTable.setPlaceholder(new Label("Erro ao carregar transações."));
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFechar() {
        Stage stage = (Stage) transacoesTable.getScene().getWindow();
        stage.close();
    }
}