package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

@AllArgsConstructor
@Data
public class DeleteCommand extends AbstractCommand {

    private final String fileName;

    @Override
    public CommandType getType() {
        return CommandType.DELETE_REQUEST;
    }
}
