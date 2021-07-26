package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public class CreateFolderCommand extends AbstractCommand {

    private String newName;

    @Override
    public CommandType getType() {
        return CommandType.CREATE_FOLDER;
    }
}
