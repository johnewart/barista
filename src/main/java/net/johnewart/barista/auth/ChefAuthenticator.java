package net.johnewart.barista.auth;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import net.johnewart.barista.core.User;

public class ChefAuthenticator implements Authenticator<String, User> {
    @Override
    public Optional<User> authenticate(String userid) throws AuthenticationException {
        if ("admin".equals(userid)) {
            return Optional.of(new User("admin"));
        }

        return Optional.absent();
    }
}