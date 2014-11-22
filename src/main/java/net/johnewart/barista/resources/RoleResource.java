package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import net.johnewart.barista.core.Role;
import net.johnewart.barista.core.RunList;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.EnvironmentDAO;
import net.johnewart.barista.data.RoleDAO;
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

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
public class RoleResource {
    private static final Logger LOG = LoggerFactory.getLogger(RoleResource.class);

    private RoleDAO roleDAO;
    private final EnvironmentDAO environmentDAO;

    public RoleResource(RoleDAO roleDAO, EnvironmentDAO environmentDAO) {
        this.roleDAO = roleDAO;
        this.environmentDAO = environmentDAO;
    }


    @PUT
    @Timed(name = "role-update")
    @Path("{name:[A-Za-z0-9_:-]+}")
    public Response update(@PathParam("name") String roleName, Role role) {
        Role existingRole = roleDAO.getByName(roleName);

        if (existingRole == null) {
            throw new ChefAPIException(404, String.format("Cannot load role %s", roleName));
        } else {
            if(role.getName() != null && !role.getName().equals(existingRole.getName())) {
                throw new ChefAPIException("Role name mismatch.");
            } else {
                existingRole.update(role);
                validateRole(existingRole);
                roleDAO.store(existingRole);
                // TODO: update existing role
                return Response.status(200).entity(existingRole).build();
            }
        }
    }

    @POST
    @Timed(name = "role-create")
    public Response create(Role role) {
        if(roleDAO.getByName(role.getName()) == null) {
            validateRole(role);
            roleDAO.store(role);
            return Response
                    .status(201)
                    .entity(ImmutableMap.of("uri", "http://localhost:9090/roles/" + role.getName()))
                    .build();
        } else {
            throw new ChefAPIException(409, "Role already exists");
        }
    }

    @GET
    @Timed(name = "role-list")
    public Map<String, String> listRoles() {
        Map<String, String> roles = new HashMap<>();

        for (Role role : roleDAO.findAll()) {
            StringBuilder builder = new StringBuilder("http://localhost:9090/roles/");
            builder.append(role.getName());
            roles.put(role.getName(), builder.toString());
        }

        return roles;
    }

    @DELETE
    @Timed(name = "role-delete-all")
    public Response deleteRoles() {
        roleDAO.removeAll();
        return Response.status(200).build();
    }

    @DELETE
    @Timed(name = "role-delete")
    @Path("{name:[A-Za-z0-9_:-]+}")
    public Response deleteRole(@PathParam("name") String roleName) {
        LOG.debug("Deleting " + roleName);
        Role removedRole = roleDAO.removeByName(roleName);
        if(removedRole != null) {
            return Response.status(200).entity(removedRole).build();
        } else {
            throw new ChefAPIException(404, String.format("Cannot load role %s", roleName));
        }
    }

    @DELETE
    @Timed(name = "role-delete-environments")
    @Path("{name:[A-Za-z0-9_:-]+}/environments")
    public Response deleteRoleEnvironments(@PathParam("name") String roleName) {
        LOG.debug("Deleting environments for " + roleName);

        Role role = roleDAO.getByName(roleName);

        if(role != null) {
            role.clearEnvRunLists();
            roleDAO.store(role);
            return Response.status(200).entity(role).build();
        } else {
            throw new ChefAPIException(404, String.format("Cannot load role %s", roleName));
        }
    }

    @DELETE
    @Timed(name = "role-delete-environments")
    @Path("{name:[A-Za-z0-9_:-]+}/environments/{environmentName:[A-Za-z0-9_:-]+}")
    public Response deleteRoleEnvironments(@PathParam("name") String roleName,
                                           @PathParam("environmentName") String environmentName) {
        LOG.debug("Deleting env: " + environmentName + " for " + roleName);

/*        if(user.isAdmin())
            return Response.status(405).build();*/
            //throw new ChefAPIException(405, "Can't do that as an admin");

        Role role = roleDAO.getByName(roleName);

        if(role != null) {
            role.clearEnvRunList(environmentName);
            roleDAO.store(role);
            return Response.status(200).entity(role).build();
        } else {
            throw new ChefAPIException(404, String.format("Cannot load role %s", roleName));
        }
    }

    @GET
    @Timed(name = "role-fetch")
    @Path("{name:[A-Za-z0-9_:-]+}")
    public Response fetch(@PathParam("name") String roleName) {
        LOG.debug("Fetching " + roleName);

        Role role = roleDAO.getByName(roleName);

        if (role == null) {
            throw new ChefAPIException(404, String.format("Cannot load role %s", roleName));
        } else {
            return Response
                    .status(200)
                    .entity(role)
                    .build();
        }
    }

    @GET
    @Timed(name = "role-fetch-environments")
    @Path("{name:[A-Za-z0-9_:-]+}/environments")
    public Response fetchEnvironments(@PathParam("name") String roleName) {

        LOG.debug("Fetching environments for " + roleName );

        Role role = roleDAO.getByName(roleName);

        if (role == null) {
            throw new ChefAPIException(404, String.format("Cannot load role %s", roleName));
        } else {

            List<String> environmentNames = new LinkedList<>();
            environmentNames.add("_default");

            for(String environmentName : role.getEnvRunLists().keySet()) {
                environmentNames.add(environmentName);
            }

            return Response
                    .status(200)
                    .entity(environmentNames)
                    .build();
        }
    }

    @GET
    @Timed(name = "role-fetch-environment")
    @Path("{name:[A-Za-z0-9_:-]+}/environments/{environmentName:[A-Za-z0-9_:-]+}")
    public Response fetch(@PathParam("name") String roleName,
                          @PathParam("environmentName") String environmentName) {

        LOG.debug("Fetching " + roleName + " in environment " + environmentName);

        Role role = roleDAO.getByName(roleName);

        if(environmentDAO.getByName(environmentName) == null) {
            throw new ChefAPIException(404, String.format("Can't find environment '%s'", environmentName));
        }

        if (role == null) {
            throw new ChefAPIException(404, String.format("Cannot load role %s", roleName));
        } else {

            RunList runlist;
            if(environmentName.equals("_default")) {
                runlist = role.getRunList();
            } else {
                runlist = role.getEnvRunLists().get(environmentName);
            }

            //if(runlist == null) {
                //throw new ChefAPIException(404, String.format("Can't find environment '%s' for role '%s'", environmentName, roleName));
            //}

            if (runlist == null || runlist.size() == 0) {
                // Client expects a nil value in place of an empty list
                Map<String, List<String>> result = new HashMap<>();
                result.put("run_list", null);
                return Response
                        .status(200)
                        .entity(result)
                        .build();
            } else {
                return Response
                        .status(200)
                        .entity(ImmutableMap.of("run_list", runlist))
                        .build();
            }
        }
    }


    private void validateRole(Role role) throws ChefAPIException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        List<String> errorMessages = new ArrayList<>();
        Set<ConstraintViolation<Role>> set = validator.validate(role);

        for(ConstraintViolation<Role> constraintViolation : set) {
            errorMessages.add(constraintViolation.getMessage());
        }

        if (errorMessages.size() > 0) {
            throw new ChefAPIException(errorMessages);
        }
    }

    private void adminNotAllowed() {

    }
}
