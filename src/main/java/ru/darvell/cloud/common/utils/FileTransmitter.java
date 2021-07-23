package ru.darvell.cloud.common.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.common.commands.FileMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.file.StandardOpenOption.APPEND;

@Slf4j
@AllArgsConstructor
public class FileTransmitter {

    private final FileDeleter fileDeleter;
    private final int filePackageSize;

    public void receiveFile(FileMessage fileMessage, Path rootPath, ProgressUpdater progressUpdater){
        if (fileMessage.getFileName() != null && !fileMessage.getFileName().isEmpty()) {
            Path path = rootPath.resolve(fileMessage.getFileName());
            if (!fileMessage.isMultipart()) {
                fileDeleter.delete(path);
                try {
                    Files.write(path, fileMessage.getFileData());
                } catch (IOException e) {
                    log.error("Error while writing file", e);
                }
            } else {
                log.debug("receive file message part {}", fileMessage);
                try {
                    if (fileMessage.getCurrMessageNumber() == 0) {
                        fileDeleter.delete(path);
                        Files.createFile(path);
                    }
                    progressUpdater.update((double) fileMessage.getCurrMessageNumber() / fileMessage.getMessagesCount());
                    Files.write(path, fileMessage.getFileData(), APPEND);
                } catch (IOException e) {
                    log.error("Error while writing file", e);
                }
            }
        }
    }

    public void sendFile(Path filePath, FileSender sender, ProgressUpdater progressUpdater) {
        try {
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();
            if (fileSize <= filePackageSize) {
                byte[] bytes = Files.readAllBytes(filePath);
                FileMessage fileMessage = FileMessage.builder()
                        .multipart(false)
                        .fileName(fileName)
                        .fileData(bytes)
                        .build();
                sender.send(fileMessage);
            } else {
                sendMultipart(filePath, fileSize, sender, progressUpdater);
            }
        } catch (IOException e) {
            log.error("error while sending file to server", e);
        }
    }

    private void sendMultipart(Path filePath, long fileSize, FileSender sender, ProgressUpdater progressUpdater){
        Thread thread = new Thread(()-> {
            try (FileInputStream inputStream = new FileInputStream(filePath.toAbsolutePath().toString())) {
                String fileName = filePath.getFileName().toString();

                long messagesCount = (long) Math.ceil((double) fileSize / filePackageSize);
                long currMessageNumber = 0;
                byte[] buffer = new byte[filePackageSize];
                int readBytes;
                while ((readBytes = inputStream.read(buffer)) != -1) {
                    FileMessage fileMessage = FileMessage.builder()
                            .multipart(true)
                            .fileName(fileName)
                            .fileData(Arrays.copyOf(buffer, readBytes))
                            .size(readBytes)
                            .currMessageNumber(currMessageNumber++)
                            .finalPart(currMessageNumber == messagesCount - 1)
                            .build();
                   sender.send(fileMessage);
                   progressUpdater.update((double) currMessageNumber / messagesCount);

                }
            } catch (IOException e) {
                log.error("error while sending multipart file to server", e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
