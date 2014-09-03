package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.UserDAO;
import net.johnewart.barista.exceptions.ChefAPIException;
import net.johnewart.barista.utils.URLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/authenticate_user")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    private static final Logger LOG = LoggerFactory.getLogger(AuthResource.class);

    private final UserDAO userDAO;

    public AuthResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @POST
    @Timed(name = "user-authenticate")
    public Response auth(Map<String, String> credentials) {
        final String username = credentials.get("name");
        final String password = credentials.get("password");
        final User user = userDAO.getByName(username);

        if(username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new ChefAPIException(406, "Not enough auth data.");
        }

        if (user != null && user.getPassword().equals(password)) {
            //return Response.ok(user).build();
            return Response.ok(ImmutableMap.of("name", username, "verified", true)).build();
        } else {
            return Response.ok(ImmutableMap.of("name", username, "verified", false)).build();
        }
    }


}
