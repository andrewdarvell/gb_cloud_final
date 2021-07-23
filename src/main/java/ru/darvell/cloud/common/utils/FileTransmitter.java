package ru.darvell.cloud.common.utils;

import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.common.commands.FileMessage;
import ru.darvell.cloud.server.database.repositories.MstatRepository;
import ru.darvell.cloud.server.database.repositories.MstatRepositoryImpl;
import ru.darvell.cloud.server.models.FileStat;
import ru.darvell.cloud.server.models.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;

import static java.nio.file.StandardOpenOption.APPEND;

@Slf4j
public class FileTransmitter {

    private final FileDeleter fileDeleter;
    private final int filePackageSize;
    private Path rootPath;

    private User user;
    private MstatRepository mstatRepository;

    public FileTransmitter(Path rootPath, FileDeleter fileDeleter, int filePackageSize) {
        this.fileDeleter = fileDeleter;
        this.filePackageSize = filePackageSize;
        this.rootPath = rootPath;
    }


    public void prepareServerFileCheckers(User user, Path path) {
        this.user = user;
        mstatRepository = new MstatRepositoryImpl();
        this.rootPath = path;
    }

    public void receiveFile(FileMessage fileMessage, ProgressUpdater progressUpdater, Finisher finisher) {
        if (fileMessage.getFileName() != null && !fileMessage.getFileName().isEmpty()) {
            Path path = rootPath.resolve(fileMessage.getFileName());
            if (!fileMessage.isMultipart()) {
                receiveSimpleFile(path, fileMessage);
                finisher.onFinish();
            } else {

                log.debug("receive file message part {}", fileMessage);
                try {
                    if (fileMessage.getCurrMessageNumber() == 0) {
                        fileDeleter.delete(path);
                        Files.createFile(path);
                    }
                    log.debug("messages count {}, current number {}", fileMessage.getMessagesCount(), fileMessage.getCurrMessageNumber());
                    progressUpdater.update((double) fileMessage.getCurrMessageNumber() / (fileMessage.getMessagesCount() - 1));
                    Files.write(path, fileMessage.getFileData(), APPEND);
                    if (fileMessage.isFinalPart()) {
                        finisher.onFinish();
                    }
                } catch (IOException  e) {
                    log.error("Error while writing file", e);
                }
            }
        }
    }

    public void receiveFileOnServer(FileMessage fileMessage, Finisher finisher) {
        if (fileMessage.getFileName() != null && !fileMessage.getFileName().isEmpty()) {
            Path path = rootPath.resolve(fileMessage.getFileName());
            if (!fileMessage.isMultipart()) {
                receiveSimpleFile(path, fileMessage);
                finisher.onFinish();
            } else {
                log.debug("receive file message part {}", fileMessage);
                try {
                    if (fileMessage.getCurrMessageNumber() == 0) {
                        fileDeleter.delete(path);
                        Files.createFile(path);
                        prepareLoadStatistics(fileMessage, path);
                    }
                    log.debug("messages count {}, current number {}", fileMessage.getMessagesCount(), fileMessage.getCurrMessageNumber());
                    Files.write(path, fileMessage.getFileData(), APPEND);
                    mstatRepository.updateStatistic(user.getId(), path.toString());

                    if (fileMessage.isFinalPart()) {
                        finisher.onFinish();
                        mstatRepository.getByUserIdAndFilePath(user.getId(), path.toString())
                                .forEach(fs -> mstatRepository.delete(fs.getId()));
                    }
                } catch (IOException e) {
                    log.error("Error while writing file", e);
                }
            }
        }
    }

    private void prepareLoadStatistics(FileMessage fileMessage, Path path){
        mstatRepository.getByUserIdAndFilePath(user.getId(), path.toString())
                .forEach(fs -> mstatRepository.delete(fs.getId()));

        FileStat fileStat = FileStat.builder()
                .fileName(fileMessage.getFileName())
                .filePath(path.toString())
                .userId(user.getId())
                .build();
        fileStat.setLastUpdate(new Date(System.currentTimeMillis()));
        mstatRepository.addStatistic(fileStat);
    }

    private void receiveSimpleFile(Path path, FileMessage fileMessage) {
        fileDeleter.delete(path);
        try {
            Files.write(path, fileMessage.getFileData());
        } catch (IOException e) {
            log.error("Error while writing file", e);
        }
    }

    public void sendFile(Path filePath, FileSender sender, ProgressUpdater progressUpdater, Finisher finisher) {
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
                finisher.onFinish();
            } else {
                sendMultipart(filePath, fileSize, sender, progressUpdater, finisher);
            }
        } catch (IOException e) {
            log.error("error while sending file to server", e);
        }
    }

    private void sendMultipart(Path filePath, long fileSize, FileSender sender, ProgressUpdater progressUpdater, Finisher finisher) {
        Thread thread = new Thread(() -> {
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
                            .messagesCount(messagesCount)
                            .finalPart(currMessageNumber == messagesCount - 1)
                            .build();
                    sender.send(fileMessage);
                    progressUpdater.update((double) currMessageNumber / messagesCount);
                }
                finisher.onFinish();
            } catch (IOException e) {
                log.error("error while sending multipart file to server", e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
