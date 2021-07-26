package ru.darvell.cloud.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.client.views.FileListItem;
import ru.darvell.cloud.client.views.MyListCell;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.commands.*;
import ru.darvell.cloud.common.utils.FileTransmitter;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MainWindowController implements Initializable {

    private static final int FILE_PACKAGE_SIZE = 8 * 1024;

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    private FileTransmitter fileTransmitter;

    @FXML
    public TextField localPathText;

    @FXML
    public TextField remotePathText;

    @FXML
    public ListView<FileListItem> localList;

    @FXML
    public ListView<FileListItem> remoteList;

    @FXML
    public ProgressIndicator progress;

    @FXML
    public Button sendButton;

    @FXML
    public Button receiveButton;

    private Path rootPath = Paths.get(System.getProperty("user.home")).toAbsolutePath();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Main window init");
        initCellFactory();
        addNavigationListeners();
    }

    public void onRootTextEnter() {
        log.debug("Enter on root navigation");
        Path newRootPath = rootPath.resolve(localPathText.getText());
        if (Files.isDirectory(newRootPath) && Files.exists(newRootPath)) {
            updateRootPath(newRootPath);
            updateLocalFileList();
        }
    }

    public void setWorkingSocket(Socket socket) {
        try {
            log.debug("Send data to main window process");
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            initReader();
            os.writeObject(new ListCommand());
            os.flush();
            fileTransmitter = new FileTransmitter(rootPath, this::deleteFile, FILE_PACKAGE_SIZE);
            updateLocalPathText();
        } catch (IOException e) {
            log.error("Cannot work with server", e);
        }
    }

    private void updateRootPath(Path path) {
        this.rootPath = path;
        this.fileTransmitter.updateRoot(path);
    }

    private void initCellFactory() {
        localList.setCellFactory(param -> new MyListCell(fileName -> {
            Path path = rootPath.resolve(fileName);
            deleteFile(path);
            updateLocalFileList();
        }, res -> {
            renameLocalFile(
                    localList.getSelectionModel().getSelectedItem().getName()
                    , res);
            updateLocalFileList();
        }, res -> {
            createLocalFolder(res);
            updateLocalFileList();
        }));

        remoteList.setCellFactory(param -> new MyListCell(fileName -> {
            sendDeleteFileCommand(fileName);
            sendListCommand();
        }, res -> sendRenameCommand(
                remoteList.getSelectionModel().getSelectedItem().getName()
                , res
        ), res -> sendCreateFolderCommand(res)));
    }

    private void createLocalFolder(String folderName) {
        try {
            Path newFolderPath = rootPath.resolve(folderName);
            if (!Files.exists(newFolderPath)) {
                Files.createDirectory(newFolderPath);
            }
        } catch (IOException e) {
            log.error("Error while create folder", e);
        }
    }

    private void renameLocalFile(String oldFileName, String newFileName) {
        try {
            Files.move(
                    rootPath.resolve(oldFileName),
                    rootPath.resolve(newFileName)
            );
        } catch (IOException e) {
            log.error("Error while rename file", e);
        }
    }

    private void initReader() {
        Thread readThread = new Thread(() -> {
            try {
                while (true) {
                    AbstractCommand command = (AbstractCommand) is.readObject();
                    log.debug(command.getType().toString());
                    switch (command.getType()) {
                        case LIST_MESSAGE:
                            ListMessage listMessage = (ListMessage) command;
                            Platform.runLater(() -> {
                                remotePathText.setText(listMessage.getCurrPath());
                                remoteList.getItems().clear();
                                remoteList.getItems().addAll(listMessage.getFiles());
                            });
                            break;
                        case ERROR_MESSAGE:
                            ErrorMessage errorMessage = (ErrorMessage) command;
                            log.info(errorMessage.getMessage());
                            break;
                        case FILE_MESSAGE:
                            fileTransmitter.receiveFile((FileMessage) command,
                                    p -> Platform.runLater(() -> progress.setProgress(p)),
                                    () -> Platform.runLater(() -> {
                                        setDisablingButton(false);
                                        updateLocalFileList();
                                    }));
                            break;
                        default:
                            log.debug("Receive unknown message");
                    }
                }
            } catch (Exception e) {
                log.error("Error while handling answer from server", e);
            }
        });
        readThread.setDaemon(true);
        readThread.start();

    }

    public void updateLocalPathText() {
        localPathText.setText(rootPath.toString());
        updateLocalFileList();
    }

    private void updateLocalFileList() {
        localList.getItems().clear();
        try (Stream<Path> files = Files.list(rootPath)) {
            localList.getItems().addAll(
                    files.map(i -> new FileListItem(i.getFileName().toString(), Files.isDirectory(i)))
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            log.error("error while gettings filelist", e);
        }
    }

    private void addNavigationListeners() {
        localList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                FileListItem item = localList.getSelectionModel().getSelectedItem();
                Path newPath = rootPath.resolve(item.getName());
                if (Files.isDirectory(newPath)) {
                    updateRootPath(newPath);
                    updateLocalPathText();
                }
            }
        });

        remoteList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                FileListItem item = remoteList.getSelectionModel().getSelectedItem();
                sendCdCommand(item.getName());
                log.debug("send cd command to server");
            }
        });
    }

    private void sendCdCommand(String dir) {
        try {
            os.writeObject(new CdCommand(dir));
            os.flush();
        } catch (IOException e) {
            log.error("Cannot send CD command", e);
        }
    }

    private void sendRenameCommand(String oldValue, String newValue) {
        try {
            os.writeObject(new RenameCommand(oldValue, newValue));
            os.flush();
        } catch (IOException e) {
            log.error("Cannot send rename command", e);
        }
    }

    private void sendCreateFolderCommand(String folderName) {
        try {
            os.writeObject(new CreateFolderCommand(folderName));
            os.flush();
        } catch (IOException e) {
            log.error("Cannot send create folder command", e);
        }
    }

    public void clickLocalUpButton() {
        if (rootPath.getParent() != null) {
            updateRootPath(rootPath.getParent());
            log.debug("New root path {}", rootPath);
        }
        updateLocalPathText();
    }

    public void clickRemoteUpButton() {
        try {
            os.writeObject(new UpCommand());
            os.flush();
        } catch (IOException e) {
            log.error("Cannot send UP command", e);
        }
    }

    public void doSendToServerAction() {
        FileListItem item = localList.getSelectionModel().getSelectedItem();
        if (item != null) {
            Path path = rootPath.resolve(item.getName());
            if (!Files.isDirectory(path)) {
                setDisablingButton(true);
                sendFileToServer(path);
            }
        }
    }

    public void doRequestFileAction() {
        try {
            FileListItem item = remoteList.getSelectionModel().getSelectedItem();
            os.writeObject(new RequestFileMessage(item.getName()));
            setDisablingButton(true);
        } catch (IOException e) {
            log.error("error while request file from server", e);
        }
    }

    public void sendFileToServer(Path filePath) {
        fileTransmitter.sendFile(filePath,
                fileMessage -> {
                    os.writeObject(fileMessage);
                    os.flush();
                },
                p -> Platform.runLater(() -> progress.setProgress(p)),
                () -> setDisablingButton(false)
        );
    }

    public void sendListCommand() {
        try {
            os.writeObject(new ListCommand());
            os.flush();
        } catch (IOException e) {
            log.error("Cannot send list command", e);
        }
    }

    public void sendDeleteFileCommand(String fileName) {
        try {
            os.writeObject(new DeleteCommand(fileName));
            os.flush();
        } catch (IOException e) {
            log.error("Cannot send delete command", e);
        }
    }


    private void deleteFile(Path path) {
        try {
            if (Files.isDirectory(path)) {
                deleteFolder(path);
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            log.error("Error while delete existing file", e);
        }
    }

    private void deleteFolder(Path path) {
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void setDisablingButton(boolean disable) {
        sendButton.setDisable(disable);
        receiveButton.setDisable(disable);
    }


}
