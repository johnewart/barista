package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.data.ClientDAO;
import net.johnewart.barista.data.NodeDAO;
import net.johnewart.barista.exceptions.ChefAPIException;
import net.johnewart.barista.utils.URLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
public class ClientResource {
    private static final Logger LOG = LoggerFactory.getLogger(ClientResource.class);
    private final ClientDAO clientDAO;

    public ClientResource(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    @POST
    @Timed(name = "client-create")
    public Response create(Client client) {

        if(clientDAO.getByName(client.getName()) != null) {
            throw new ChefAPIException(409, String.format("Client with name '%s' already exists.", client.getName()));
        } else {
            if(client.getPublicKey() == null && client.getPrivateKey() == null) {
                client.generateKeys();
            }

            clientDAO.store(client);

            Map<String, String> response = new HashMap<>();
            response.put("uri", URLGenerator.generateUrl("clients/" + client.getName()));
            response.put("private_key", client.getPrivateKey());
            response.put("public_key", client.getPublicKey());

            return Response
                    .status(201)
                    .entity(response)
                    .build();
        }
    }

    @GET
    @Timed(name = "client-list")
    public Map<String, String> list() {
        List<Client> clients = clientDAO.findAll();

        Map<String, String> results = new HashMap<>();
        for(Client client : clients)  {
            if(!client.getName().equals("admin"))
                results.put(client.getName(), URLGenerator.generateUrl("clients/" + client.getName()));
        }

        return results;
    }

    @DELETE
    @Timed(name = "client-delete")
    @Path("{name:.*}")
    public Response deleteClient(@PathParam("name") String clientName) {
        LOG.debug("Deleting " + clientName);
        if(clientDAO.getByName(clientName) != null) {
            clientDAO.removeByName(clientName);
            return Response.status(200).build();
        } else {
            throw new ChefAPIException(404, String.format("Can't find client named '%s'", clientName));
        }
    }

    @PUT
    @Timed(name = "client-update")
    @Path("{name:.*}")
    // Stupid PUT data, WTF can we not be consistent about key content? how about ?regenerate_key instead of setting
    // the key data to true... o_O
    public Response updateClient(@PathParam("name") String clientName,
                                 @Context HttpServletRequest httpRequest,
                                 Client client) {

        final Client existing = clientDAO.getByName(clientName);
        final int returnCode;

        if (existing == null)
            throw new ChefAPIException(404, String.format("Can't find a client with name '%s'", clientName));

        if (!client.getName().equals(clientName)) {
            // Renaming
            if (clientDAO.getByName(client.getName()) != null) {
                // Existing one with the desired name
                throw new ChefAPIException(409, String.format("A client with the desired name '%s' already exists.", client.getName()));
            } else {
                // Rename is ok, remove old one and return 201
                clientDAO.removeByName(clientName);
                returnCode = 201;
            }
        } else {
            returnCode = 200;
        }

        boolean updatingPublicKey = false;
        boolean returnPrivateKey = false;

        if (client.getPublicKey() != null) {
            updatingPublicKey = true;
        }

        if (client.getPrivateKey() != null) {
            if (!client.getPrivateKey().equals("false")) {
                client.generateKeys();
                updatingPublicKey = true;
                returnPrivateKey = true;
            }
        }


        existing.update(client);
        clientDAO.store(existing);

        Map<String, Object> results = new HashMap<>();
        results.put("name", clientName);
        //if(updatingPublicKey)
        results.put("public_key", existing.getPublicKey());
        if (returnPrivateKey)
            results.put("private_key", existing.getPrivateKey());
        return Response.status(returnCode).entity(results).build();

    }

    @GET
    @Timed(name = "client-fetch")
    @Path("{name:.*}")
    public Response fetchClient(@PathParam("name") String clientName) {
        Client client = clientDAO.getByName(clientName);
        if(client != null) {
            /**
             * {"name"=>"pedant_temporary_client_1409445522-851383000-44660",
             "chef_type"=>"client",
             "json_class"=>"Chef::ApiClient",
             "admin"=>false,
             "validator"=>true,
             "public_key"=>/^(-----BEGIN RSA PUBLIC KEY-----|-----BEGIN PUBLIC KEY-----)/}
             */
            Map<String, Object> results = new HashMap<>();
            results.put("name", clientName);
            results.put("chef_type", "client");
            results.put("json_class", "Chef::ApiClient");
            results.put("admin", client.isAdmin());
            results.put("validator", client.isValidator());
            results.put("public_key", client.getPublicKey());
            //results.put("private_key", client.getPrivateKey());
            //return Response.ok(
            //        ImmutableMap.of("name", clientName, "public_key", client.getPublicKey())).build();
            return Response.ok(results).build();
            //URLGenerator.generateUrl("clients/" + clientName))).build();
        } else {
            throw new ChefAPIException(404, String.format("Can't find client with name '%s'", clientName));
        }
    }

}
