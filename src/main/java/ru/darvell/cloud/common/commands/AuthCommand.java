package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCommand extends AbstractCommand {

    private String login;
    private String password;

    @Override
    public CommandType getType() {
        return CommandType.AUTH_MESSAGE;
    }
}
