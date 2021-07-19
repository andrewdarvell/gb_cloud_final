package ru.darvell.cloud.common.commands;

import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

public class SuccessAuthMessage extends AbstractCommand {
    @Override
    public CommandType getType() {
        return CommandType.SUCCESS_AUTH_MESSAGE;
    }
}
