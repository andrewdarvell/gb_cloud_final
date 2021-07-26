package ru.darvell.cloud.client.views;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class FileListItem implements Serializable {

    private String name;
    private boolean folder;

}
