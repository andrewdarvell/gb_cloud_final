package ru.darvell.cloud.server.database.repositories;


import ru.darvell.cloud.server.models.User;

import java.util.Optional;

public interface UsersRepository {

    Optional<User> authUser(String login, String password);
    Optional<User> getuserByLogin(String login);
    Optional<User> getUserById(int id);
    User addUser(User user);


}
