package ru.darvell.cloud.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.client.views.OkDialogAction;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class EditDialogController implements Initializable {

    @FXML
    public Label hintLabel;

    @FXML
    public TextField textField;

    private OkDialogAction okDialogAction;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hintLabel.setText("Введите новое имя");
    }

    public void setOkAction(OkDialogAction okAction) {
        this.okDialogAction = okAction;
    }

    public void setOldValue(String value) {
        textField.setText(value);
    }

    public void okAction() {
        okDialogAction.onOk(textField.getText());
        close();
    }

    public void cancellAction() {
        close();
    }

    public void close() {
        Stage stage = (Stage) hintLabel.getScene().getWindow();
        stage.close();
    }
}
