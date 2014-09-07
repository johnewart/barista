package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.NodeDAO;
import net.johnewart.barista.exceptions.ChefAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/nodes")
@Produces(MediaType.APPLICATION_JSON)
public class NodeResource {
    private static final Logger LOG = LoggerFactory.getLogger(NodeResource.class);

    private NodeDAO nodeDAO;

    public NodeResource(NodeDAO nodeDAO) {
        this.nodeDAO = nodeDAO;
    }


    @PUT
    @Timed(name = "node-update")
    @Path("{name:.*}")
    public Response update(@PathParam("name") String nodeName, Node node) {
        Node existingNode = nodeDAO.getByName(nodeName);

        if (existingNode == null) {
            //return Response.status(404).build();
            nodeDAO.store(node);
            return Response.status(201).entity(node).build();
        } else {
            if(node.getName() != null && !node.getName().equals(existingNode.getName())) {
                throw new ChefAPIException("Node name mismatch.");
            } else {

                existingNode.updateFrom(node);
                validateNode(existingNode);
                node.normalizeRunList();
                nodeDAO.store(existingNode);
                return Response.status(200).entity(existingNode).build();
            }
        }
    }

    @POST
    @Timed(name = "node-create")
    public Response create(Node node) {
        if(nodeDAO.getByName(node.getName()) == null) {
            validateNode(node);
            node.normalizeRunList();
            nodeDAO.store(node);
            return Response
                    .status(201)
                    .entity(ImmutableMap.of("uri", "http://localhost:9090/nodes/" + node.getName()))
                    .build();
        } else {
            throw new ChefAPIException(409, "Node already exists");
        }
    }

    @GET
    @Timed(name = "node-list")
    public java.util.Map<String, String> listNodes(@Auth User user) {
        Map<String, String> nodes = new HashMap<>();

        for (Node node : nodeDAO.findAll()) {
            StringBuilder builder = new StringBuilder("http://localhost:9090/nodes/");
            builder.append(node.getName());
            nodes.put(node.getName(), builder.toString());
        }

        return nodes;
    }

    @DELETE
    @Timed(name = "node-delete-all")
    public Response deleteNodes() {
        nodeDAO.removeAll();
        return Response.status(200).build();
    }

    @DELETE
    @Timed(name = "node-delete")
    @Path("{name:.*}")
    public Response deleteNode(@PathParam("name") String nodeName) {
        LOG.debug("Deleting " + nodeName);
        Node removedNode = nodeDAO.removeByName(nodeName);
        if(removedNode != null) {
            return Response.status(200).entity(removedNode).build();
        } else {
            throw new ChefAPIException(404, String.format("node '%s' not found", nodeName));
        }
    }

    @GET
    @Timed(name = "node-fetch")
    @Path("{name:.*}")
    public Response fetch(@PathParam("name") String nodeName) {
        LOG.debug("Fetching " + nodeName);

        Node node = nodeDAO.getByName(nodeName);
        if (node == null) {
            return Response.status(404).entity(new HashMap<String, String>()).build();
        } else {
            return Response
                    .status(200)
                    .entity(node)
                    .build();
        }
    }


    private void validateNode(Node node) throws ChefAPIException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        List<String> errorMessages = new ArrayList<>();
        Set<ConstraintViolation<Node>> set = validator.validate(node);

        for(ConstraintViolation<Node> constraintViolation : set) {
            errorMessages.add(constraintViolation.getMessage());
        }

        if(!Node.isValidRunList(node.getRunList())) {
            errorMessages.add("Field 'run_list' is not a valid run list");
        }

        if (errorMessages.size() > 0) {
            throw new ChefAPIException(errorMessages);
        }
    }
}
