package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.johnewart.barista.core.*;
import net.johnewart.barista.data.CookbookDAO;
import net.johnewart.barista.data.EnvironmentDAO;
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

@Path("/environments")
@Produces(MediaType.APPLICATION_JSON)
public class EnvironmentResource {
    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentResource.class);
    private final CookbookDAO cookbookDAO;
    private final EnvironmentDAO environmentDAO;

    public EnvironmentResource(CookbookDAO cookbookDAO, EnvironmentDAO environmentDAO) {
        this.cookbookDAO = cookbookDAO;
        this.environmentDAO = environmentDAO;
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
        if(environmentDAO.getByName(environment.getName()) != null) {
            return Response.status(409).build();
        } else {
            environmentDAO.add(environment);
            final Map<String, String> environmentURL = ImmutableMap.of("uri", String.format("http://localhost:9090/environments/%s", environment.getName()));
            validateEnvironment(environment);
            return Response
                    .status(201)
                    .entity(environmentURL)
                    .build();
        }
    }

    @PUT
    @Timed(name = "environment-update")
    @Path("{name:[a-zA-Z_]+$}")
    public Response query(@PathParam("name") String environmentName,
                          Environment environment) {
        Environment existing = environmentDAO.getByName(environmentName);
        if(existing != null) {
            existing.update(environment);
        } else {
            environmentDAO.add(environment);
        }

        return Response.ok(environment).build();


    }

    @GET
    @Timed(name = "environment-fetch")
    @Path("{name:[a-zA-Z_]+$}")
    public Environment fetch(@PathParam("name") String environmentName) {
        final Environment environment = environmentDAO.getByName(environmentName);

        if(environment == null) {
            throw new WebApplicationException(404);
        }

        return environment;
    }

    @DELETE
    @Timed(name = "environment-delete")
    @Path("{name:[a-zA-Z_]+$}")
    public Response deleteEnvironment(@PathParam("name") String environmentName) {
        if(environmentName.equals("")) {
            environmentDAO.removeAll();
        } else {
            environmentDAO.removeByName(environmentName);
        }

        return Response.status(201).build();
    }

    @GET
    @Timed(name = "environment-recipes")
    @Path("{name:.*?}/recipes")
    public Response getRecipes(@PathParam("name") String environmentName) {
        Environment environment = environmentDAO.getByName(environmentName);

        if (environment == null && !environmentName.equals("_default")) {
            throw new ChefAPIException(404, String.format("Cannot load environment %s", environmentName));
        }

        List<String> recipeList = new LinkedList<>();

        if(environment != null) {
            Map<String, String> cookbookVersions = environment.getCookbookVersions();
        }

        return Response.ok().entity(new HashMap<String, String>()).build();
    }

    @GET
    @Timed(name = "environment-cookbooks")
    @Path("{name:.*?}/cookbooks{cookbookName:(/[a-zA-Z_]+)?}")
    public Response getCookbooks(@PathParam("name") String environmentName,
                                 @PathParam("cookbookName") String cookbookName,
                                 @QueryParam("num_versions") Optional<String> numberOfVersions) {

        LOG.debug("Fetching cookbooks for " + environmentName);


        int maxVersions;

        if(numberOfVersions.isPresent()) {
            if(numberOfVersions.get().equals("all")) {
                maxVersions = -1;
            } else {
                try {
                    maxVersions = Integer.parseInt(numberOfVersions.get());
                } catch (NumberFormatException nfe) {
                    throw new ChefAPIException(400, "You have requested an invalid number of versions (x >= 0 || 'all')");
                }
            }
        } else {
            maxVersions = 1;
        }

        Environment environment = environmentDAO.getByName(environmentName);

        if (environment == null && !environmentName.equals("_default")) {
            throw new ChefAPIException(404, String.format("Cannot load environment %s", environmentName));
        }

        final Map<String, CookbookLocation> results;

        if(cookbookName != null) {
            cookbookName = cookbookName.replace("/", "");
        }

        if(cookbookName != null && !cookbookName.isEmpty())
        {
            LOG.debug("Fetching only " + cookbookName);
            results = getCookbookResults(environment, maxVersions, cookbookName);
            if (results.keySet().size() == 0) {
                throw new ChefAPIException(404, String.format("Cannot find a cookbook named %s", cookbookName));
            }
        } else {
            results = getCookbookResults(environment, maxVersions);
        }


        return Response.ok(results).build();
    }

    private Map<String, CookbookLocation> getCookbookResults(final Environment environmentConstraint,
                                                             final int maxVersions,
                                                             final String cookbookName) {
        Map<String, Set<Cookbook>> cookbooks = new HashMap<>();
        cookbooks.put(cookbookName, cookbookDAO.findAllByName(cookbookName));
        return getCookbookResults(environmentConstraint, maxVersions, cookbooks);
    }

    private Map<String, CookbookLocation> getCookbookResults(final Environment environmentConstraint,
                                                             final int maxVersions) {
        Map<String, Set<Cookbook>> cookbooks = new HashMap<>();

        for(Cookbook cookbook : cookbookDAO.findAll()) {
            String key = cookbook.getCookbookName();

            if(!cookbooks.containsKey(key)) {
                cookbooks.put(key, new HashSet<Cookbook>());
            }

            cookbooks.get(key).add(cookbook);
        }

        return getCookbookResults(environmentConstraint, maxVersions, cookbooks);
    }

    private Map<String, CookbookLocation> getCookbookResults(final Environment environmentConstraint,
                                                             final int maxVersions,
                                                             final Map<String, Set<Cookbook>> cookbooks) {
        Map<String, CookbookLocation> cookbookLocations = new HashMap<>();

        /**
         * For each cookbook name, filter the cookbooks in that set and add them to the
         * results if appropriate
         */
        for (String cookbookName : cookbooks.keySet()) {
            final List<Cookbook> cookbookList = new ArrayList<>(cookbooks.get(cookbookName));
            final VersionConstraint constraint = getConstraintForCookbook(environmentConstraint, cookbookName);
            int capacity;

            Collections.sort(cookbookList, new Comparator<Cookbook>() {
                @Override
                public int compare(Cookbook o1, Cookbook o2) {
                    return o1.getSemanticVersion().compareTo(o2.getSemanticVersion());
                }
            });

            if(maxVersions == -1 ) {
                capacity = cookbookList.size();
            } else {
                capacity = maxVersions;
            }

            Iterator<Cookbook> it = cookbookList.iterator();

            if(it.hasNext()) {
                Cookbook currentCookbook = it.next();
                cookbookLocations.put(cookbookName, new CookbookLocation(currentCookbook));

                while(capacity > 0 && currentCookbook != null) {
                    SemanticVersion cookbookVersion = new SemanticVersion(currentCookbook.getVersion());

                    if(constraint.matches(cookbookVersion)) {
                        cookbookLocations.get(cookbookName).addVersion(currentCookbook);
                        capacity--;
                    }
                    if(it.hasNext()) {
                        currentCookbook = it.next();
                    } else {
                        currentCookbook = null;
                    }
                }
            }
        }

        return cookbookLocations;
    }

    private VersionConstraint getConstraintForCookbook(Environment environmentConstraint, String cookbookName) {
        if(environmentConstraint == null) {
            // Open constraint
            return new VersionConstraint();
        }

        Map<String, String> cookbookVersions = environmentConstraint.getCookbookVersions();

        if( cookbookVersions == null || !cookbookVersions.containsKey(cookbookName))
        {
            // Open constraint
            return new VersionConstraint();
        } else {
            return new VersionConstraint(cookbookVersions.get(cookbookName));
        }

    }

    private void validateEnvironment(Environment environment) throws ChefAPIException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        List<String> errorMessages = new ArrayList<>();
        Set<ConstraintViolation<Environment>> set = validator.validate(environment);

        for(ConstraintViolation<Environment> constraintViolation : set) {
            errorMessages.add(constraintViolation.getMessage());
        }

        if (errorMessages.size() > 0) {
            throw new ChefAPIException(errorMessages);
        }
    }
}
