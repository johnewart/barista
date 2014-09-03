package net.johnewart.barista.data;

import net.johnewart.barista.core.User;

import java.util.List;

public interface UserDAO {
    List<User> findAll();
    void store(User user);
    User getByName(String userName);
    User removeByName(String userName);
    void removeAll();
}
