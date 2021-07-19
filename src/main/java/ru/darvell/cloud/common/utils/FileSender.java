package ru.darvell.cloud.common.utils;

import ru.darvell.cloud.common.commands.FileMessage;

import java.io.IOException;

@FunctionalInterface
public interface FileSender {
    void send(FileMessage fileMessage) throws IOException;
}
