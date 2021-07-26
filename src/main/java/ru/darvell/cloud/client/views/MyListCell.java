package ru.darvell.cloud.client.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.client.EditDialogController;

import java.io.IOException;
import java.net.URL;

@Slf4j
public class MyListCell extends ListCell<FileListItem> {

    private final ImageView directoryImage;
    private final ImageView fileImage;
    private final DeleteListAction deleteListAction;
    private final OkDialogAction renameAction;
    private final OkDialogAction newFolderAction;

    public MyListCell(DeleteListAction deleteListAction, OkDialogAction renameAction, OkDialogAction newFolderAction) {
        this.deleteListAction = deleteListAction;
        this.renameAction = renameAction;
        this.newFolderAction = newFolderAction;
        directoryImage = loadResource("/folder_icon.png");
        fileImage = loadResource("/file_icon.png");
    }

    private ImageView loadResource(String path) {
        URL file = getClass().getResource(path);
        if (file != null) {
            return new ImageView(file.toString());
        } else {
            return null;
        }
    }

    @Override
    protected void updateItem(FileListItem item, boolean empty) {
        super.updateItem(item, empty);

        MenuItem delete = new MenuItem("Удалить");
        MenuItem rename = new MenuItem("Переименовать");
        MenuItem createFolder = new MenuItem("Новая папка");
        delete.setOnAction(event -> deleteListAction.delete(item.getName()));
        rename.setOnAction(event -> showEditDialog(item.getName(), renameAction));
        createFolder.setOnAction(event -> showCreateFolderDialog(newFolderAction));

        if (empty) {
            setText(null);
            setGraphic(null);
            setContextMenu(null);
        } else {
            setContextMenu(new ContextMenu(delete, rename, createFolder));
            setText(item.getName());
            if (item.isFolder() && directoryImage != null) {
                setGraphic(directoryImage);
            }

            if (!item.isFolder() && fileImage != null) {
                setGraphic(fileImage);
            }
        }
    }

    private void showEditDialog(String oldValue, OkDialogAction okDialogAction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx_templates/edit_dialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Переименовать");
            EditDialogController editDialogController = loader.getController();
            editDialogController.setOkAction(okDialogAction);
            editDialogController.setOldValue(oldValue);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            log.error("Cannot load main form template", ex);
        }
    }

    private void showCreateFolderDialog(OkDialogAction okDialogAction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx_templates/edit_dialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Новая папка");
            EditDialogController editDialogController = loader.getController();
            editDialogController.setOkAction(okDialogAction);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            log.error("Cannot load main form template", ex);
        }
    }

}
