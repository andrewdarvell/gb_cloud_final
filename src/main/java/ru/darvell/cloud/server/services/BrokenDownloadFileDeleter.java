package ru.darvell.cloud.server.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.server.database.repositories.MstatRepository;
import ru.darvell.cloud.server.database.repositories.MstatRepositoryImpl;
import ru.darvell.cloud.server.models.FileStat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Slf4j
public class BrokenDownloadFileDeleter implements Runnable {

    private static final long MAX_TIME_DIFF = 5 * 60 * 1000L;
    private final MstatRepository mstatRepository = new MstatRepositoryImpl();

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            List<FileStat> fileStats = mstatRepository.getAll();
            Date currDateTime = new Date(System.currentTimeMillis());
            for (FileStat fileStat: fileStats) {
                long timeDiff = currDateTime.getTime() - fileStat.getLastUpdate().getTime();
                log.debug("FileStat to check : {}", fileStat);
                log.debug("Timediff : {}", timeDiff);
                log.debug("currTime : {}", currDateTime.getTime());
                log.debug("lastUpdateTime : {}", fileStat.getLastUpdate().getTime());
                if (timeDiff > MAX_TIME_DIFF) {
                    try {
                        Files.delete(Paths.get(fileStat.getFilePath()));
                    } catch (IOException exception) {
                        log.error("Error while deleting broken file", exception);
                    }
                    mstatRepository.delete(fileStat.getId());
                }
            }
            try {
                Thread.sleep(1000L * 60 * 5);
            } catch (InterruptedException e) {
                log.error("Interrupted file deleter", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
