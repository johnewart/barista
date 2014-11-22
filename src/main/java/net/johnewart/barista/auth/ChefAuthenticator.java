package net.johnewart.barista.auth;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.UserDAO;

public class ChefAuthenticator implements Authenticator<String, User> {

    private final UserDAO userDAO;

    public ChefAuthenticator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public Optional<User> authenticate(String userid) throws AuthenticationException {
        User user = userDAO.getByName(userid);

        if (user != null) {
            return Optional.of(user);
        } else {
            return Optional.absent();
        }
    }
}