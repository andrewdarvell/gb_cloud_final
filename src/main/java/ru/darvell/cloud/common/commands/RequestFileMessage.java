package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

@Data
@AllArgsConstructor
public class RequestFileMessage extends AbstractCommand {

    private final String name;

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
