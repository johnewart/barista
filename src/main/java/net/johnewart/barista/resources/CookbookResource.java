package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.data.CookbookDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/cookbooks")
@Produces(MediaType.APPLICATION_JSON)
public class CookbookResource {
    private static final Logger LOG = LoggerFactory.getLogger(CookbookResource.class);
    private final CookbookDAO cookbookDAO;

    public CookbookResource(CookbookDAO cookbookDAO) {
        this.cookbookDAO = cookbookDAO;
    }



    @POST
    @Timed(name = "cookbook-create")
    public Response create(Cookbook cookbook) {
        return Response
                .status(201)
                .entity(cookbook)
                .build();
    }

    @PUT
    @Timed(name = "cookbook-update")
    @Path("{name:[a-zA-Z_]+}/{version:.*?}")
    public Response update(Cookbook cookbook) {
        Cookbook existing = cookbookDAO.getByName(cookbook.getName());

        if(existing == null) {
            cookbookDAO.add(cookbook);
        } else {
            //update existing
        }

        return Response.status(200).entity(existing).build();
    }


    @GET
    @Timed(name = "cookbook-list")
    public List<Cookbook> list() {
        List<Cookbook> Cookbooks = new ArrayList<>();
        Cookbooks.add(new Cookbook());
        return Cookbooks;
    }

    @DELETE
    @Timed(name = "cookbook-delete-version")
    @Path("{name:[a-zA-Z_]+}/{version:.*}")
    public Response deleteCookbook(@PathParam("name") String cookbookName,
                                   @PathParam("version") String cookbookVersion) {

        LOG.debug("Deleting version " + cookbookVersion + " of " + cookbookName);
        cookbookDAO.removeByNameAndVersion(cookbookName, cookbookVersion);
        return Response.status(201).build();
    }

    @DELETE
    @Timed(name = "cookbook-delete")
    @Path("{name:[a-zA-Z_]+$}")
    public Response deleteCookbook(@PathParam("name") String cookbookName) {
        LOG.debug("Deleting " + cookbookName);
        cookbookDAO.removeByName(cookbookName);
        return Response.status(201).build();
    }


}
