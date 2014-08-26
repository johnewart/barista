package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import net.johnewart.barista.core.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    public UserResource() {
    }

    @POST
    @Timed(name = "user-create")
    public Response auth(User user) {
        return Response
                .status(201)
                .entity(user)
                .build();
    }

    @GET
    @Timed(name = "user-list")
    public List<User> list() {
        List<User> Users = new ArrayList<>();
        Users.add(new User());
        return Users;
    }

    @DELETE
    @Timed(name = "user-delete")
    @Path("{name:.*}")
    public Response deleteUser(@PathParam("name") String userName) {
        LOG.debug("Deleting " + userName);
        return Response.status(201).build();
    }
}
