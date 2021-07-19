package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage extends AbstractCommand {

    private String message;

    @Override
    public CommandType getType() {
        return CommandType.ERROR_MESSAGE;
    }
}
