package com.controle.view;

import com.controle.model.Categoria;
import com.controle.model.Conta;
import com.controle.model.TipoCategoria;
import com.controle.model.TipoConta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.stage.Stage;

public abstract class BaseController {

    protected Stage primaryStage;

    @FXML protected Label fullScreenHintLabel;

    private static Timer hintTimer = null;
    private static AtomicInteger hintCounter = new AtomicInteger(0);

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    protected void navigateTo(String fxmlPath, String windowTitle) {
        if (primaryStage == null) {
            System.err.println("Erro de Navegação: primaryStage é nulo. Você esqueceu de chamar setPrimaryStage()?");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setPrimaryStage(this.primaryStage);
            }

            Scene scene = new Scene(root);

            try {
                scene.getStylesheets().add(getClass().getResource("/com/controle/view/style.css").toExternalForm());
            } catch (NullPointerException e) {
                System.err.println("Aviso: Não foi possível encontrar 'style.css'. A tela pode aparecer sem estilo.");
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle(windowTitle);
            primaryStage.show();

            applyFullScreen();
            showFullScreenHintTemporarily("Pressione ESC para sair.", 3000);

        } catch (IOException e) {
            System.err.println("Erro fatal ao carregar FXML: " + fxmlPath);
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela: " + fxmlPath);
        } catch (NullPointerException e) {
            System.err.println("Erro fatal: O arquivo FXML não foi encontrado em: " + fxmlPath);
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro Crítico", "Arquivo de interface não encontrado: " + fxmlPath);
        }
    }

    @FXML
    protected void handleGoBack(ActionEvent event) {
        navigateTo("/com/controle/view/MainMenuView.fxml", "Controle de Gastos Pessoais - Menu Principal");
    }


    protected void showFullScreenHint(String message) {
        if (fullScreenHintLabel != null) {
            Platform.runLater(() -> {
                fullScreenHintLabel.setText(message);
                fullScreenHintLabel.setVisible(true);
                fullScreenHintLabel.setManaged(true);
            });
        }
    }

    protected void hideFullScreenHint() {
        if (fullScreenHintLabel != null) {
            Platform.runLater(() -> {
                fullScreenHintLabel.setVisible(false);
                fullScreenHintLabel.setManaged(false);
            });
        }
    }

    protected void showFullScreenHintTemporarily(String message, int durationMillis) {
        hintCounter.incrementAndGet();
        showFullScreenHint(message);
        if (hintTimer != null) {
            hintTimer.cancel();
        }
        hintTimer = new Timer("FullScreenHintTimer", true);
        hintTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hintCounter.decrementAndGet() <= 0) {
                    hideFullScreenHint();
                }
            }
        }, durationMillis);
    }

    protected void applyFullScreen() {
        if (primaryStage != null) {
            Platform.runLater(() -> primaryStage.setFullScreen(true));
        }
    }

    protected void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            if (primaryStage != null) {
                alert.initOwner(primaryStage);
            }
            alert.showAndWait();
        });
    }

    protected void showFieldError(Control control, Label errorLabel, String message) {
        Platform.runLater(() -> {
            if (control != null && errorLabel != null) {
                if (!control.getStyleClass().contains("text-field-error")) {
                    control.getStyleClass().add("text-field-error");
                }
                errorLabel.setText(message);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        });
    }

    protected void clearFieldError(Control control, Label errorLabel) {
        Platform.runLater(() -> {
            if (control != null && errorLabel != null) {
                control.getStyleClass().remove("text-field-error");
                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        });
    }

    protected abstract void clearAllErrors();

    protected void setupCategoriaComboBoxFormatter(ComboBox<Categoria> comboBox) {
        comboBox.setCellFactory(cell -> new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
        comboBox.setButtonCell(new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
    }

    protected void setupContaComboBoxFormatter(ComboBox<Conta> comboBox) {
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

    protected void setupCategoriaFilter(ComboBox<TipoCategoria> typeComboBox, ComboBox<Categoria> categoryComboBox, ObservableList<Categoria> allCategories) {
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                categoryComboBox.setItems(allCategories);
            } else {
                ObservableList<Categoria> filteredList = allCategories.stream()
                        .filter(cat -> cat.getTipo() == newValue)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));
                categoryComboBox.setItems(filteredList);
            }
            categoryComboBox.getSelectionModel().clearSelection();
        });
    }

    protected void setupContaFilter(ComboBox<TipoCategoria> typeComboBox, ComboBox<Conta> accountComboBox, ObservableList<Conta> allAccounts) {
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                accountComboBox.setItems(allAccounts);
            } else {
                ObservableList<Conta> filteredList;
                if (newValue == TipoCategoria.DESPESA) {
                    filteredList = allAccounts.stream()
                            .filter(conta -> conta.getTipo() == TipoConta.CONTA_CORRENTE ||
                                    conta.getTipo() == TipoConta.DINHEIRO ||
                                    conta.getTipo() == TipoConta.CARTAO_DE_CREDITO)
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));
                } else {
                    filteredList = allAccounts.stream()
                            .filter(conta -> conta.getTipo() != TipoConta.CARTAO_DE_CREDITO)
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));
                }
                accountComboBox.setItems(filteredList);
            }
            accountComboBox.getSelectionModel().clearSelection();
        });
    }
}