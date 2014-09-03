package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import net.johnewart.barista.core.*;
import net.johnewart.barista.data.CookbookDAO;
import net.johnewart.barista.data.EnvironmentDAO;
import net.johnewart.barista.data.RoleDAO;
import net.johnewart.barista.exceptions.ChefAPIException;
import net.johnewart.barista.utils.URLGenerator;
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

@Path("/environments")
@Produces(MediaType.APPLICATION_JSON)
public class EnvironmentResource {
    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentResource.class);

    private final CookbookDAO cookbookDAO;
    private final EnvironmentDAO environmentDAO;
    private final RoleDAO roleDAO;

    public EnvironmentResource(CookbookDAO cookbookDAO, EnvironmentDAO environmentDAO, RoleDAO roleDAO) {
        this.cookbookDAO = cookbookDAO;
        this.environmentDAO = environmentDAO;
        this.roleDAO = roleDAO;
    }

    @GET
    @Timed(name = "environment-list")
    public Map<String, String> listEnvironments() {
        Map<String, String> environmentMap = new HashMap<>();

        for(Environment e : environmentDAO.findAll()) {
            environmentMap.put(e.getName(), URLGenerator.generateUrl(String.format("environments/%s", e.getName())));
        }

        return environmentMap;
    }

    @GET
    @Timed(name = "get-nodes")
    @Path("{name:\\w+}/nodes")
    public Map<String, String> getNodes(@PathParam("name") String environmentName) {
        Map<String, String> nodes = new HashMap<>();

        return nodes;
    }


    @POST
    @Timed(name = "environment-create")
    public Response create(Environment environment) {
        LOG.debug("Creating environment: " + environment.getName());
        if(environmentDAO.getByName(environment.getName()) != null) {
            throw new ChefAPIException(409, "Environment already exists.");
        } else {
            environment.setJsonClass("Chef::Environment");
            LOG.debug("Validating " + environment);
            validateEnvironment(environment);
            environmentDAO.store(environment);
            final Map<String, String> environmentURL = ImmutableMap.of("uri", String.format("http://localhost:9090/environments/%s", environment.getName()));
            return Response
                    .status(201)
                    .entity(environmentURL)
                    .build();
        }
    }

    @PUT
    @Timed(name = "environment-update")
    @Path("{name:[a-zA-Z0-9:_-]+$}")
    public Response query(@PathParam("name") String environmentName,
                          Environment environment) {

        if(environmentName.equals("_default")) {
            throw new ChefAPIException(405, "Can't update _default");
        }

        Environment existing = environmentDAO.getByName(environmentName);
        validateEnvironment(environment);

        final int responseCode;

        if(existing != null) {
            if(!environment.getName().equals(environmentName)) {
                if(environmentDAO.getByName(environment.getName()) != null || environment.getName().equals("_default")) {
                    throw new ChefAPIException(409, "Environment already exists");
                } else {
                    // Creating a new environment via rename
                    responseCode = 201;
                    environmentDAO.removeByName(environmentName);
                }
            } else {
                responseCode = 200;
            }
            existing.update(environment);
            environmentDAO.store(existing);
        } else {
            throw new ChefAPIException(404, String.format("Environment %s does not exist", environmentName));
        }

        return Response.status(responseCode).entity(environment).build();
    }

    @GET
    @Timed(name = "environment-fetch")
    @Path("{name:[a-zA-Z0-9:_-]+$}")
    public Environment fetch(@PathParam("name") String environmentName) {
        final Environment environment = environmentDAO.getByName(environmentName);

        if(environment == null) {
            throw new WebApplicationException(404);
        }

        return environment;
    }

    @DELETE
    @Timed(name = "environment-delete")
    @Path("{name:[a-zA-Z0-9:_-]+$}")
    public Response deleteEnvironment(@PathParam("name") String environmentName) {

        if(environmentName.equals("")) {
            environmentDAO.removeAll();
            return Response.status(200).build();
        } else if(environmentName.equals("_default")) {
            throw new ChefAPIException(405, "Cannot delete the _default environment");
        } else {
            Environment removed = environmentDAO.removeByName(environmentName);
            if(removed == null) {
                throw new ChefAPIException(404, "Environment " + environmentName + " does not exist.");
            } else {
                return Response.status(200).entity(removed).build();
            }
        }
    }


    @GET
    @Timed(name = "environment-roles")
    @Path("{name:.*?}/roles/{roleName:.*?}")
    public Response getRoles(@PathParam("name") String environmentName,
                             @PathParam("roleName") String roleName) {

        Environment environment = environmentDAO.getByName(environmentName);
        Role role = roleDAO.getByName(roleName);

        if(environment == null && !environmentName.equals("_default")) {
            throw new ChefAPIException(404, String.format("Cannot load environment %s", environmentName));
        }

        if(role != null) {
            return Response.ok(role).build();
        } else {
            throw new ChefAPIException(404, String.format("Cannot load role %s", roleName));
        }


    }

    @GET
    @Timed(name = "environment-recipes")
    @Path("{name:.*?}/recipes")
    public Response getRecipes(@PathParam("name") String environmentName) {
        Environment environment = environmentDAO.getByName(environmentName);

        if (environment == null && !environmentName.equals("_default")) {
            throw new ChefAPIException(404, String.format("Cannot load environment %s", environmentName));
        }

        Set<String> recipeList = new HashSet<>();
        List<Cookbook> cookbooks;

        if(environment != null) {
            Map<String, List<Cookbook>> cookbookMap =
                    cookbookDAO.findAllWithConstraints(environment.getVersionConstraints(), CookbookDAO.NO_LIMITS);
            cookbooks = new LinkedList<>();

            for(String cookbookName : cookbookMap.keySet()) {
                for(Cookbook cookbook : cookbookMap.get(cookbookName)) {
                    cookbooks.add(cookbook);
                }
            }

        } else {
            cookbooks = cookbookDAO.findAll();
        }

        for(Cookbook cookbook : cookbooks) {
            for(Recipe recipe : cookbook.getRecipes()) {
                String recipeFileName = recipe.getName();
                if(recipeFileName != null) {
                    String recipeName = recipeFileName.replace(".rb", "");
                    String fqName = String.format("%s::%s", cookbook.getCookbookName(), recipeName);
                    recipeList.add(fqName);
                }
            }
        }

        return Response.ok().entity(recipeList).build();
    }

    @GET
    @Timed(name = "environment-cookbooks")
    @Path("{name:.*?}/cookbooks{cookbookName:(/[a-zA-Z_]+)?}")
    public Response getCookbooks(@PathParam("name") String environmentName,
                                 @PathParam("cookbookName") String cookbookName,
                                 @QueryParam("num_versions") Optional<String> numberOfVersions) {

        LOG.debug("Fetching cookbook " + cookbookName + " for " + environmentName);


        int numVersions;

        if(numberOfVersions.isPresent()) {
            if(numberOfVersions.get().equals("all")) {
                numVersions = -1;
            } else {
                try {
                    numVersions = Integer.parseInt(numberOfVersions.get());
                } catch (NumberFormatException nfe) {
                    throw new ChefAPIException(400, "You have requested an invalid number of versions (x >= 0 || 'all')");
                }
            }
        } else {
            // four-part version /environments/ENV/cookbooks/COOKBOOK no numversions = all
            if (cookbookName != null && !cookbookName.isEmpty()) {
                numVersions = CookbookDAO.NO_LIMITS;
            } else {
                numVersions = 1;
            }
        }

        final Environment environment;

        if (environmentName.equals("_default")) {
            environment = new Environment("_default");
        } else {
            environment = environmentDAO.getByName(environmentName);
        }

        if (environment == null) {
            throw new ChefAPIException(404, String.format("Cannot load environment %s", environmentName));
        }

        if(cookbookName != null) {
            cookbookName = cookbookName.replace("/", "");
        }

        Set<String> cookbookNames = new HashSet<>();

        final boolean singleCookbook;
        if(cookbookName != null && !cookbookName.isEmpty())
        {
            LOG.debug("Fetching only " + cookbookName);
            // TODO: not the best way
            if(cookbookDAO.findAllByName(cookbookName).size() == 0) {
                throw new ChefAPIException(404, String.format("Cannot find a cookbook named %s", cookbookName));
            }

            cookbookNames.add(cookbookName);
            singleCookbook = true;
        } else {
            cookbookNames.addAll(cookbookDAO.findAllCookbookNames());
            singleCookbook = false;
        }

        Map<String, VersionConstraint> constraintMap = environment.getVersionConstraints();
        Map<String, CookbookLocation> results = new HashMap<>();

        for(String cName : cookbookNames) {
            VersionConstraint constraint = constraintMap.get(cName);
            List<Cookbook> cookbookList = cookbookDAO.findOneWithConstraints(cName, constraint, numVersions);

            CookbookLocation cookbookLocation = new CookbookLocation(cName);

            for(Cookbook currentCookbook : cookbookList) {
                cookbookLocation.addVersion(currentCookbook);
            }

            results.put(cName, cookbookLocation);
        }

        return Response.ok(results).build();
    }


    private void validateEnvironment(Environment environment) throws ChefAPIException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        List<String> errorMessages = new ArrayList<>();
        Set<ConstraintViolation<Environment>> set = validator.validate(environment);

        for(ConstraintViolation<Environment> constraintViolation : set) {
            errorMessages.add(constraintViolation.getMessage());
        }

        // Check the cookbook versions data
        // TODO: Model validator?
        if(environment.getCookbookVersions() != null) {
            for(String cookbookName : environment.getCookbookVersions().keySet()) {
                String versionString = environment.getCookbookVersions().get(cookbookName);

                if (cookbookName.isEmpty() || !cookbookName.matches("^[a-zA-Z_]+$")) {
                    errorMessages.add("Invalid key '" + cookbookName + "' for cookbook_versions");
                }

                if (!VersionConstraint.validate(versionString)) {
                    // Sigh.
                    if(versionString == null) {
                        versionString = "null";
                    }
                    errorMessages.add("Invalid value '" + versionString + "' for cookbook_versions");
                }
            }
        }

        if (errorMessages.size() > 0) {
            LOG.debug("Failed to validate environment" + environment + ": " + errorMessages);
            throw new ChefAPIException(errorMessages);
        }
    }
}
