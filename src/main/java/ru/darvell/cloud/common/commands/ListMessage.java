package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

import java.util.List;

@AllArgsConstructor
@Data
public class ListMessage extends AbstractCommand {

    private final String currPath;
    private final List<String> files;

    @Override
    public CommandType getType() {
        return CommandType.LIST_MESSAGE;
    }
}
