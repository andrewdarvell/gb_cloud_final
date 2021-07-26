package ru.darvell.cloud.client.views;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

import java.net.URL;

public class MyListCell extends ListCell<FileListItem> {

    private final ImageView directoryImage;
    private final ImageView fileImage;
    private final DeleteListAction deleteListAction;

    public MyListCell(DeleteListAction deleteListAction) {
        this.deleteListAction = deleteListAction;
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
        delete.setOnAction(event -> deleteListAction.delete(item.getName()));

        if (empty) {
            setText(null);
            setGraphic(null);
            setContextMenu(null);
        } else {
            setContextMenu(new ContextMenu(delete));
            setText(item.getName());
            if (item.isFolder() && directoryImage != null) {
                setGraphic(directoryImage);
            }

            if (!item.isFolder() && fileImage != null) {
                setGraphic(fileImage);
            }
        }
    }


}
