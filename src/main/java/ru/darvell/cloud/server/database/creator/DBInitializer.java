package ru.darvell.cloud.server.database.creator;


import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.server.database.DBConnection;
import ru.darvell.cloud.server.exceptions.ServerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class DBInitializer {

    public static final String NONE_VERSION = "none";
    public static final String WORK_VERSION = "V1";

    private final Connection connection;

    public DBInitializer() throws ServerException {
        connection = DBConnection.getSqliteDBConnection();
    }

    public void doInitial() throws ServerException {
        String version = getDBVersion();
        if (version.equals(NONE_VERSION)) {
            InitializerV1Version initializerV1Version = new InitializerV1Version();
            initializerV1Version.doInit();
        }
    }

    private String getDBVersion() {
        String version;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT version FROM version");
            ResultSet rs = ps.executeQuery();
            version = rs.getString("version");
            rs.close();
            ps.close();
            return version;
        } catch (SQLException e) {
            log.debug("Cannot get db version", e);
        }
        return NONE_VERSION;
    }

}
