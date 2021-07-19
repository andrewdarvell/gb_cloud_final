package ru.darvell.cloud.common.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;

@Builder
@Data
public class FileMessage extends AbstractCommand {

    private String fileName;
    private byte[] fileData;
    private int size;

    private long currMessageNumber;
    private long messagesCount;
    private boolean multipart;
    private boolean finalPart;

    @Override
    public CommandType getType() {
        return CommandType.FILE_MESSAGE;
    }
}
