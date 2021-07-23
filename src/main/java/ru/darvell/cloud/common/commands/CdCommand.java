package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

@AllArgsConstructor
@Data
public class CdCommand extends AbstractCommand {

    private final String dir;

    @Override
    public CommandType getType() {
        return CommandType.CD_REQUEST;
    }
}
