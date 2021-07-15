package ru.darvell.cloud.common.commands;

import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

import static ru.darvell.cloud.common.CommandType.REGISTER;

public class RegisterMessage extends AbstractCommand {

    @Override
    public CommandType getType() {
        return REGISTER;
    }
}
