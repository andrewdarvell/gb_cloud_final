package ru.darvell.cloud.server.database.repositories;

import ru.darvell.cloud.server.models.FileStat;

import java.util.List;

public interface MstatRepository {

    FileStat addStatistic(FileStat fileStat);
    void updateStatistic(int userId, String filePath);
    List<FileStat> getAll();
     List<FileStat> getByUserIdAndFilePath(int userId, String filePath);
    void delete(int id);
}
