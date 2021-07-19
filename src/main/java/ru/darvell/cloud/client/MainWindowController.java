package ru.darvell.cloud.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class MainWindowController implements Initializable {

    @FXML
    public TreeView<String> localTree;

    private final Path rootPath = Paths.get(System.getProperty("user.home")).toAbsolutePath();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TreeItem<String> rootItem = new TreeItem("Inbox");
        localTree.setRoot(rootItem);
//        try {
//            localTree.getRoot().getChildren().addAll(
//                    Files.list(rootPath)
//                            .map(Path::getFileName)
//                            .map(Path::toString)
//                            .map(TreeItem::new)
//                            .collect(Collectors.toList())
//            );
//
//        } catch (IOException exception) {
//            exception.printStackTrace();
//        }
    }


}
