package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

import static ru.darvell.cloud.common.CommandType.RENAME_REQUEST;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public class RenameCommand extends AbstractCommand {

    private String oldValue;
    private String newValue;

    @Override
    public CommandType getType() {
        return RENAME_REQUEST;
    }
}
