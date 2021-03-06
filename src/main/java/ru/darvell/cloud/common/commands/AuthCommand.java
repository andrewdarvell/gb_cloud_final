package ru.darvell.cloud.common.commands;

import lombok.*;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuthCommand extends AbstractCommand {

    private String login;
    private String password;

    @Override
    public CommandType getType() {
        return CommandType.AUTH_MESSAGE;
    }
}
