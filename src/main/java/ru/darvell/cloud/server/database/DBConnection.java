package ru.darvell.cloud.server.database;


import ru.darvell.cloud.server.exceptions.ServerException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getSqliteDBConnection() throws ServerException{
        try{
            return DriverManager.getConnection("jdbc:sqlite:serverDB/base.db");
        } catch (SQLException e) {
            throw new ServerException("Cannot connect to database", e);
        }
    }
}
