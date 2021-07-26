package ru.darvell.cloud.common.commands;

import lombok.*;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

import static ru.darvell.cloud.common.CommandType.REGISTER_MESSAGE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RegisterCommand extends AbstractCommand {

    private String login;
    private String password;

    @Override
    public CommandType getType() {
        return REGISTER_MESSAGE;
    }
}
