package ru.darvell.cloud.server.database.creator;

import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.server.database.DBConnection;
import ru.darvell.cloud.server.exceptions.ServerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static ru.darvell.cloud.server.database.creator.DBInitializer.WORK_VERSION;

@Slf4j
public class InitializerV1Version {

    private Connection connection;

    public InitializerV1Version() {
        connection = DBConnection.getSqliteDBConnection();

    }

    void doInit() throws ServerException {
        try {
            connection.setAutoCommit(false);
            initVersionTable();
            writeVersion();
            createUsersTable();
            writeTestUser();
            createMultipartStatTable();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throw new ServerException("Cannot rollback create database", e);
            }
            throw new ServerException("Errors when init database", e);
        }
    }

    private void initVersionTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS version (version string)");
    }

    private void writeVersion() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO version VALUES (?)");
        ps.setString(1, WORK_VERSION);
        ps.executeUpdate();
        ps.close();
    }

    private void createUsersTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id integer PRIMARY KEY AUTOINCREMENT," +
                "login string," +
                "password string," +
                "work_dir_name string" +
                ")");
    }

    private void writeTestUser() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO users(login, password, work_dir_name) VALUES ('l1','p1','l1')");
        ps.executeUpdate();
        ps.close();
    }

    private void createMultipartStatTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS mstat (" +
                "id integer PRIMARY KEY AUTOINCREMENT," +
                "user_id integer," +
                "file_name string," +
                "file_path string," +
                "last_update string" +
                ")");
    }

}
