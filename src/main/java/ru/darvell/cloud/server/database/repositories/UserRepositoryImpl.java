package ru.darvell.cloud.server.database.repositories;

import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.server.database.DBConnection;
import ru.darvell.cloud.server.exceptions.ModelException;
import ru.darvell.cloud.server.models.User;

import java.sql.*;
import java.util.Optional;

@Slf4j
public class UserRepositoryImpl implements UsersRepository {

    @Override
    public Optional<User> authUser(String login, String password) {
        User user = null;
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT id, login, password, work_dir_name\n" +
                    "FROM users\n" +
                    "WHERE login = ? AND password = ?");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                user = resultSetToUser(resultSet);
            }
            resultSet.close();
            ps.close();
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            log.error("Auth user error", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getuserByLogin(String login) {
        User user = null;
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT id, login, password, work_dir_name\n" +
                    "FROM users\n" +
                    "WHERE login = ?");
            ps.setString(1, login);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                user = resultSetToUser(resultSet);
            }
            resultSet.close();
            ps.close();
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            log.error("Auth user error", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserById(int id) {
        User user = null;
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT id, login, password, work_dir_name\n" +
                    "FROM users\n" +
                    "WHERE id = ?");
            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                user = resultSetToUser(resultSet);
            }
            resultSet.close();
            ps.close();
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            log.error("Get by id user error", e);
        }
        return Optional.empty();
    }

    @Override
    public User addUser(User user) throws ModelException {
        try {
            Connection connection = DBConnection.getSqliteDBConnection();
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users(login, password, work_dir_name) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getWorkDirName());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new ModelException("Error while create user");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new ModelException("Error while create user");
                }
            }
            return user;
        } catch (SQLException e) {
            log.error("Crate user error", e);
        }
        return user;
    }

    private User resultSetToUser(ResultSet resultSet) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("id"))
                .login(resultSet.getString("login"))
                .password(resultSet.getString("password"))
                .workDirName(resultSet.getString("work_dir_name"))
                .build();
    }
}
