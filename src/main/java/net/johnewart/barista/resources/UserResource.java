package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    private final UserDAO userDAO;

    public UserResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @POST
    @Timed(name = "user-create")
    public Response auth(User user) {
        if (userDAO.getByName(user.getName()) != null) {
            throw new ChefAPIException(409, String.format("User with username %s already exists.", user.getName()));
        } else {
            userDAO.store(user);
            UserResponse userResponse = new UserResponse(user);
            return Response
                .status(201)
                .entity(userResponse) //ImmutableMap.of("uri", URLGenerator.generateUrl("users/" + user.getName())))
                .build();
        }
    }

    @GET
    @Timed(name = "user-list")
    public Map<String, String> list() {
        Map<String, String> results = new HashMap<>();
        for(User user : userDAO.findAll()) {
            results.put(user.getName(), URLGenerator.generateUrl("users/" + user.getName()));
        }
        return results;
    }

    @GET
    @Timed(name = "user-fetch")
    @Path("{name:[a-zA-Z0-9_-]+}")
    public Map<String, Object> fetch(@PathParam("name") String userName) {
        User user = userDAO.getByName(userName);
        if(user != null) {
            //return ImmutableMap.of("uri", URLGenerator.generateUrl("users/" + user.getName()));
            Map<String, Object> results = new HashMap<>();
            results.put("name", userName);
            results.put("openid", null);
            results.put("admin", user.isAdmin());
            return results;
        } else {
            throw new ChefAPIException(404, String.format("User %s does not exist", userName));
        }
    }

    @DELETE
    @Timed(name = "user-delete")
    @Path("{name:[a-zA-Z0-9_-]+}")
    public Response deleteUser(@PathParam("name") String userName) {
        LOG.debug("Deleting " + userName);
        userDAO.removeByName(userName);
        return Response.status(200).build();
    }

    @PUT
    @Timed(name = "user-update")
    @Path("{name:[a-zA-Z0-9_-]+}")
    public Response update(@PathParam("name") String username,
                           User user) {
        User existing = userDAO.getByName(username);
        if(existing != null) {
            existing.update(user);
            userDAO.store(existing);
            return Response.status(200).entity(ImmutableMap.of("uri", URLGenerator.generateUrl("users/" + username))).build();
        } else {
            throw new ChefAPIException(404, String.format("No user with username '%s' found", username));
        }
    }

    class UserResponse extends User {
        public UserResponse(User user) {
            super(user);
        }

        @JsonProperty("uri")
        public String getURI() {
            return URLGenerator.generateUrl("users/" + this.getName());
        }
    }
}
