package com.controle.view;

import com.controle.model.Conta;
import com.controle.service.GastoPessoalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

public class MovimentacaoController extends BaseController {

    @FXML private ComboBox<Conta> contaOrigemComboBox;
    @FXML private ComboBox<Conta> contaDestinoComboBox;
    @FXML private TextField valorField;
    @FXML private DatePicker dataPicker;
    @FXML private Label origemErrorLabel;
    @FXML private Label destinoErrorLabel;
    @FXML private Label valorErrorLabel;
    @FXML private Label dataErrorLabel;

    private GastoPessoalService service;
    private ObservableList<Conta> contasOrigemList = FXCollections.observableArrayList();
    private ObservableList<Conta> contasDestinoList = FXCollections.observableArrayList();

    public MovimentacaoController() {
        this.service = new GastoPessoalService();
    }

    @FXML
    public void initialize() {
        loadContas();
        contaOrigemComboBox.setItems(contasOrigemList);
        contaDestinoComboBox.setItems(contasDestinoList);

        configureComboBoxDisplay(contaOrigemComboBox);
        configureComboBoxDisplay(contaDestinoComboBox);

        dataPicker.setValue(LocalDate.now());
        clearAllErrors();
    }

    private void loadContas() {
        try {
            contasOrigemList.setAll(service.listarTodasContas());
            contasDestinoList.setAll(service.listarTodasContas());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as contas: " + e.getMessage());
        }
    }

    private void configureComboBoxDisplay(ComboBox<Conta> comboBox) {
        comboBox.setCellFactory(cell -> new ListCell<Conta>() {
            @Override
            protected void updateItem(Conta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
        comboBox.setButtonCell(new ListCell<Conta>() {
            @Override
            protected void updateItem(Conta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
    }

    @FXML
    private void handleMovimentar(ActionEvent event) {
        clearAllErrors();
        if (!validateFields()) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Por favor, corrija os campos destacados.");
            return;
        }

        Conta origem = contaOrigemComboBox.getValue();
        Conta destino = contaDestinoComboBox.getValue();
        double valor = Double.parseDouble(valorField.getText().replace(",", "."));
        LocalDate data = dataPicker.getValue();

        try {
            service.transferirFundos(origem, destino, valor, data);

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Movimentação registrada com sucesso!");
            handleLimpar(null);

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro na Movimentação", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        Conta origem = contaOrigemComboBox.getValue();
        Conta destino = contaDestinoComboBox.getValue();

        if (origem == null) {
            showFieldError(contaOrigemComboBox, origemErrorLabel, "Conta de origem é obrigatória.");
            isValid = false;
        }
        if (destino == null) {
            showFieldError(contaDestinoComboBox, destinoErrorLabel, "Conta de destino é obrigatória.");
            isValid = false;
        }
        if (origem != null && destino != null && origem.getId() == destino.getId()) {
            showFieldError(contaDestinoComboBox, destinoErrorLabel, "Contas não podem ser iguais.");
            isValid = false;
        }

        try {
            double valor = Double.parseDouble(valorField.getText().replace(",", "."));
            if (valor <= 0) {
                showFieldError(valorField, valorErrorLabel, "Valor deve ser positivo.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showFieldError(valorField, valorErrorLabel, "Valor inválido.");
            isValid = false;
        }

        if (dataPicker.getValue() == null) {
            showFieldError(dataPicker, dataErrorLabel, "Data é obrigatória.");
            isValid = false;
        } else if (dataPicker.getValue().isAfter(LocalDate.now())) {
            showFieldError(dataPicker, dataErrorLabel, "Data não pode ser futura.");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void handleLimpar(ActionEvent event) {
        contaOrigemComboBox.getSelectionModel().clearSelection();
        contaDestinoComboBox.getSelectionModel().clearSelection();
        valorField.clear();
        dataPicker.setValue(LocalDate.now());
        clearAllErrors();
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
        clearFieldError(contaOrigemComboBox, origemErrorLabel);
        clearFieldError(contaDestinoComboBox, destinoErrorLabel);
        clearFieldError(valorField, valorErrorLabel);
        clearFieldError(dataPicker, dataErrorLabel);
    }
}