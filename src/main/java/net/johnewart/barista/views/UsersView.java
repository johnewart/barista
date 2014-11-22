package net.johnewart.barista.views;

import io.dropwizard.views.View;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.UserDAO;

import java.util.List;

public class UsersView extends View {

    private final UserDAO userDAO;

    public UsersView(UserDAO userDAO) {
        super("/views/ftl/users.ftl");
        this.userDAO = userDAO;
    }

    public List<User> getUsers() {
        return userDAO.findAll();
    }

}
