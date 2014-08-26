package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.data.NodeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
public class ClientResource {
    private static final Logger LOG = LoggerFactory.getLogger(ClientResource.class);

    public ClientResource( ) {
    }

    @POST
    @Timed(name = "client-auth")
    public Response auth(Client client) {
        return Response
                .status(201)
                .entity(client)
                .build();
    }

    @GET
    @Timed(name = "client-list")
    public List<Client> list() {
        List<Client> clients = new ArrayList<>();
        clients.add(new Client());
        return clients;
    }

    @DELETE
    @Timed(name = "client-delete")
    @Path("{name:.*}")
    public Response deleteClient(@PathParam("name") String clientName) {
        LOG.debug("Deleting " + clientName);
        return Response.status(201).build();
    }

}
