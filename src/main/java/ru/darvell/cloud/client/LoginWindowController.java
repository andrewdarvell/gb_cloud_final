package ru.darvell.cloud.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;
import ru.darvell.cloud.common.commands.AuthCommand;
import ru.darvell.cloud.common.commands.ErrorMessage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class LoginWindowController implements Initializable {

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private Socket socket;

    @FXML
    public TextField loginText;

    @FXML
    public TextField passwordText;

    @FXML
    public Button singInButton;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread readThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        AbstractCommand command = (AbstractCommand) is.readObject();
                        if (command.getType() == CommandType.SUCCESS_AUTH_MESSAGE) {
                            Platform.runLater(this::showMainWindow);
                            Thread.currentThread().interrupt();
                        } else if (command.getType() == CommandType.ERROR_MESSAGE) {
                            log.info(((ErrorMessage) command).getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while handling answer from server");
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            log.error("Something wring while opening connection to server", e);
        }

    }

    public void signInAction() {
        singInButton.setDisable(true);
        try {
            os.writeObject(AuthCommand.builder()
                    .login(loginText.getText())
                    .password(passwordText.getText())
                    .build());
            os.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    public void showMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx_templates/root_client_template.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            MainWindowController mainWindowController = loader.getController();
            mainWindowController.setWorkingSocket(socket);
            stage.setScene(new Scene(root));
            stage.show();
            closeCurrentStage();
        } catch (IOException ex) {
            log.error("Cannot load main form template", ex);
        }
    }

    private void closeCurrentStage() {
        Stage stage = (Stage) singInButton.getScene().getWindow();
        stage.close();
    }
}
