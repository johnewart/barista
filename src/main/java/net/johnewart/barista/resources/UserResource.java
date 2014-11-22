package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    public Response create(User user) {
        if (userDAO.getByName(user.getName()) != null) {
            throw new ChefAPIException(409, String.format("User with username %s already exists.", user.getName()));
        } else {
            if (user.getPublicKey() == null) {
                user.generateKeys();
            }
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
            results.put("public_key", user.getPublicKey());
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
        User u = userDAO.removeByName(userName);
        if (u == null) {
            return Response.status(404).build();
        } else {
            return Response.status(200).entity(u).build();
        }
    }

    @PUT
    @Timed(name = "user-update")
    @Path("{name:[a-zA-Z0-9_-]+}")
    public Response update(@PathParam("name") String username,
                           User user) {
        User existing = userDAO.getByName(username);

        if(existing != null) {
            Map<String, String> response = new HashMap<>();

            boolean publicKeyUpdated = false;
            if(user.getPublicKey() != null) {
                if(user.getPublicKey().equals("true")) {
                    user.generateKeys();
                    publicKeyUpdated = true;
                } else {
                    user.setPrivateKey(null);
                    publicKeyUpdated = true;
                }
            }

            if(user.getPrivateKey() != null && user.getPrivateKey().equals("true")) {
                user.generateKeys();
                publicKeyUpdated = true;
                response.put("private_key", existing.getPrivateKey());
            }

            existing.update(user);
            userDAO.store(existing);
            if(publicKeyUpdated)
                response.put("public_key", existing.getPublicKey());

            response.put("uri", URLGenerator.generateUrl("users/" + username));
            return Response.status(200).entity(response).build();
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
