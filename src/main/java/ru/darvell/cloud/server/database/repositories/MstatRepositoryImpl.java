package ru.darvell.cloud.server.database.repositories;

import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.server.database.DBConnection;
import ru.darvell.cloud.server.exceptions.ModelException;
import ru.darvell.cloud.server.models.FileStat;

import java.sql.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class MstatRepositoryImpl implements MstatRepository {

    @Override
    public FileStat addStatistic(FileStat fileStat) {
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO mstat(user_id, file_name, file_path, last_update) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, fileStat.getUserId());
                ps.setString(2, fileStat.getFileName());
                ps.setString(3, fileStat.getFilePath());
                ps.setString(4, fileStat.getLastUpdateInStr());

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new ModelException("Error while create user");
                }
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        fileStat.setId(generatedKeys.getInt(1));
                    } else {
                        throw new ModelException("Error while create user");
                    }
                }

            }
            connection.close();
            return fileStat;
        } catch (SQLException e) {
            log.error("Create FileStat error", e);
        }
        return fileStat;
    }

    @Override
    public void updateStatistic(int userId, String filePath) {
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE mstat SET last_update = ? WHERE user_id = ? AND file_path=?")) {

                ps.setString(1, FileStat.simpleDateFormat.format(new Date(System.currentTimeMillis())));
                ps.setInt(2, userId);
                ps.setString(3, filePath);

                ps.executeUpdate();
            }
            connection.close();

        } catch (SQLException e) {
            log.error("Update FileStat error", e);
        }
    }

    @Override
    public  List<FileStat> getAll() {
        List<FileStat> fileStats = new LinkedList<>();
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, user_id, file_name, file_path, last_update\n" +
                    "FROM mstat\n")) {
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    FileStat fileStat = resultSetToFileStat(resultSet);
                    fileStats.add(fileStat);
                }
                resultSet.close();
            }
        } catch (SQLException e) {
            log.error("Get by id FileStat error", e);
        }
        return fileStats;
    }

    @Override
    public  List<FileStat> getByUserIdAndFilePath(int userId, String filePath) {
        List<FileStat> fileStats = new LinkedList<>();
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, user_id, file_name, file_path, last_update\n" +
                            "FROM mstat\n" +
                            "WHERE user_id = ? AND file_path=?")) {

                ps.setInt(1, userId);
                ps.setString(2, filePath);
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    FileStat fileStat = resultSetToFileStat(resultSet);
                    fileStats.add(fileStat);
                }
                resultSet.close();
            }
        } catch (SQLException e) {
            log.error("Get FileStat error", e);
        }
        return fileStats;
    }

    private FileStat resultSetToFileStat(ResultSet resultSet) throws SQLException {
        FileStat fileStat = FileStat.builder()
                .id(resultSet.getInt("id"))
                .userId(resultSet.getInt("user_id"))
                .fileName(resultSet.getString("file_name"))
                .filePath(resultSet.getString("file_path"))
                .build();
        fileStat.setLastUpdate(resultSet.getString("file_path"));
        return fileStat;
    }

    @Override
    public void delete(int id) {
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM mstat WHERE id = ?")){
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("delete file stat error", e);
        }
    }
}
