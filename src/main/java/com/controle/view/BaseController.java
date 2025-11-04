package com.controle.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseController {

    protected Stage primaryStage;

    @FXML protected Label fullScreenHintLabel;

    private static Timer hintTimer = null;
    private static AtomicInteger hintCounter = new AtomicInteger(0);

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
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
            alert.showAndWait();
        });
    }

    protected void showFieldError(Control control, Label errorLabel, String message) {
        Platform.runLater(() -> {
            if (control != null && errorLabel != null) { // Adicionado null check
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
            if (control != null && errorLabel != null) { // Adicionado null check
                control.getStyleClass().remove("text-field-error");
                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        });
    }

    protected abstract void clearAllErrors();
}