package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import io.dropwizard.auth.Auth;
import net.johnewart.barista.core.*;
import net.johnewart.barista.data.CookbookDAO;
import net.johnewart.barista.data.storage.FileStorageEngine;
import net.johnewart.barista.exceptions.ChefAPIException;
import net.johnewart.barista.utils.URLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/cookbooks")
@Produces(MediaType.APPLICATION_JSON)
public class CookbookResource {
    private static final Logger LOG = LoggerFactory.getLogger(CookbookResource.class);
    private final CookbookDAO cookbookDAO;
    private final FileStorageEngine fileStorageEngine;

    public CookbookResource(CookbookDAO cookbookDAO, FileStorageEngine fileStorageEngine) {
        this.cookbookDAO = cookbookDAO;
        this.fileStorageEngine = fileStorageEngine;
    }

    @POST
    @Timed(name = "cookbook-create")
    public Response create(Cookbook cookbook) {
        cookbookDAO.store(cookbook);
        return Response
                .status(201)
                .entity(cookbook)
                .build();
    }

    @PUT
    @Timed(name = "cookbook-update")
    @Path("{name:[a-zA-Z0-9:_-]+}/{version:\\d+\\.\\d+(\\.\\d+)?}")
    public Response update(Cookbook cookbook,
                           @PathParam("name") String cookbookName,
                           @PathParam("version") String cookbookVersion,
                           @QueryParam("force") Optional<String> forceOption) {
        if(cookbook.getVersion() == null) {
            // WTF?
            LOG.debug("No vesion");
        }
        Cookbook existing = cookbookDAO.findByNameAndVersion(cookbookName, cookbookVersion);

        if(existing == null) {

            cookbookDAO.store(cookbook);
            return Response.status(201).entity(cookbook).build();
        } else {
            if(existing.isFrozen() && !forceOption.isPresent()) {
                throw new ChefAPIException(409, String.format("Cookbook '%s' is frozen.", cookbook.getCookbookName()));
            }

            existing.updateFrom(cookbook);
            cookbookDAO.store(existing);
            return Response.status(200).entity(existing).build();
        }


    }


    @GET
    @Timed(name = "cookbook-list")
    public Map<String, CookbookLocation> list(@QueryParam("num_versions") Optional<String> numVersionString) {
        int numVersions;
        if(numVersionString.isPresent()) {
            if(numVersionString.get().equals("all")) {
                numVersions = -1;
            } else {
                numVersions = Integer.parseInt(numVersionString.get());
            }

            return buildLocationHash(cookbookDAO.findAll(), numVersions);
        } else {
            return buildLocationHash(cookbookDAO.findAll(), 1);
        }
    }

    @GET
    @Timed(name = "cookbook-latest")
    @Path("_latest")
    public Map<String, String> getLatest() {
        Map<String, String> results = new HashMap<>();
        List<Cookbook> latestVersions =  cookbookDAO.findLatestVersions();

        for (Cookbook cookbook : latestVersions) {
            results.put(cookbook.getCookbookName(), URLGenerator.generateUrl(String.format("cookbooks/%s/%s",
                    cookbook.getCookbookName(), cookbook.getVersion())));
        }

        return results;
    }

    @DELETE
    @Timed(name = "cookbook-delete-version")
    @Path("{name:[a-zA-Z0-9:_-]+}/{version:\\d+\\.\\d+(\\.\\d+)?}")
    public Response deleteCookbook(@PathParam("name") String cookbookName,
                                   @PathParam("version") String cookbookVersion,
                                   @Auth User user) {

        if (user.isAdmin()) {
            throw new ChefAPIException(405, "Can't do that as an admin!");
        }

        LOG.debug("Deleting version " + cookbookVersion + " of " + cookbookName);
        Cookbook removed =  cookbookDAO.removeByNameAndVersion(cookbookName, cookbookVersion);

        if(removed != null) {
            for(net.johnewart.barista.core.CookbookResource recipe : removed.getRecipes()) {
                String hash = recipe.getChecksum();
                fileStorageEngine.remove(hash);
            }
            return Response.status(200).entity(removed).build();
        } else {
            LOG.debug("Cookbook wasn't found. 404'ing");
            throw new ChefAPIException(404, "Unable to find cookbook " + cookbookName + "@" + cookbookVersion);
        }
    }

    @DELETE
    @Timed(name = "cookbook-delete")
    @Path("{name:[a-zA-Z0-9:_-]+}")
    public Response deleteCookbook(@PathParam("name") String cookbookName) {
        LOG.debug("Deleting " + cookbookName);
        cookbookDAO.removeByName(cookbookName);
        return Response.status(200).build();
    }

    @GET
    @Timed(name = "cookbook-fetch-latest")
    @Path("{name:[a-zA-Z0-9:_-]+}/_latest")
    public Response getLatestCookbook(@PathParam("name") String cookbookName) {
        Cookbook cookbook = cookbookDAO.findLatestVersion(cookbookName);
        if (cookbook != null) {
            return Response.status(200).entity(cookbook).build();
        } else {
            throw new ChefAPIException(404, String.format("No version of %s", cookbookName));
        }
    }

    @GET
    @Timed(name = "cookbook-fetch-version")
    @Path("{name:[a-zA-Z0-9:_-]+}/{version:\\d+\\.\\d+(\\.\\d+)?}")
    public Response getCookbook(@PathParam("name") String cookbookName,
                                @PathParam("version") String cookbookVersion) {
        Cookbook cookbook = cookbookDAO.findByNameAndVersion(cookbookName, cookbookVersion);
        if (cookbook != null) {
            return Response.status(200).entity(cookbook).build();
        } else {
            throw new ChefAPIException(404, String.format("Can't find cookbook %s with version %s", cookbookName, cookbookVersion));
        }
    }

    @GET
    @Timed(name = "cookbook-fetch-all-versions")
    @Path("{name:[a-zA-Z0-9:_-]+}")
    public Response getCookbook(@PathParam("name") String cookbookName) {
        Set<Cookbook> cookbooks = cookbookDAO.findAllByName(cookbookName);

        if (cookbooks.size() > 0) {
            return Response.ok(buildLocationHash(cookbooks)).build();
        } else {
            throw new ChefAPIException(404, String.format("Can't find cookbook %s", cookbookName));
        }
    }

    @GET
    @Timed(name = "cookbook-recipes")
    @Path("_recipes")
    public List<String> getLatestRecipes() {
        List<Cookbook> latestVersions = cookbookDAO.findLatestVersions();
        List<String> recipes = new LinkedList<>();

        for(Cookbook cookbook : latestVersions) {
            for(net.johnewart.barista.core.CookbookResource recipe : cookbook.getRecipes()) {
                String recipeName = recipe.getName().replace(".rb", "");
                String fqName = String.format("%s::%s", cookbook.getCookbookName(), recipeName);
                recipes.add(fqName);
            }
        }

        Collections.sort(recipes);

        return recipes;
    }

    private Map<String, CookbookLocation> buildLocationHash(Collection<Cookbook> cookbooks) {
        return buildLocationHash(cookbooks, -1);
    }

    private Map<String, CookbookLocation> buildLocationHash(Collection<Cookbook> cookbooks, int numVersions) {

        List<Cookbook> cookbookList = new ArrayList<>(cookbooks);
        Collections.sort(cookbookList, new Comparator<Cookbook>() {
            @Override
            public int compare(Cookbook o1, Cookbook o2) {
                return o1.getSemanticVersion().compareTo(o2.getSemanticVersion());
            }
        });

        Map<String, CookbookLocation> results = new HashMap<>();

        for(Cookbook cookbook : cookbookList) {
            final String cookbookName = cookbook.getCookbookName();

            if(!results.containsKey(cookbookName)) {
                results.put(cookbookName, new CookbookLocation(cookbookName));
            }

            if(numVersions == -1 || results.get(cookbookName).size() < numVersions)
                results.get(cookbookName).addVersion(cookbook);
        }

        return results;
    }


}
