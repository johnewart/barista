package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import net.johnewart.barista.core.Databag;
import net.johnewart.barista.core.DatabagItem;
import net.johnewart.barista.data.DatabagDAO;
import net.johnewart.barista.exceptions.ChefAPIException;
import net.johnewart.barista.utils.URLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/data")
@Produces(MediaType.APPLICATION_JSON)
public class DatabagResource {
    private static final Logger LOG = LoggerFactory.getLogger(DatabagResource.class);

    private final DatabagDAO databagDAO;

    public DatabagResource(DatabagDAO databagDAO) {
        this.databagDAO = databagDAO;
    }

    @GET
    @Timed(name = "databag-list")
    public Map<String, String> list() {
        List<Databag> databags = databagDAO.findAll();
        Map<String, String> result = new HashMap<>();

        for(Databag databag : databags) {
            result.put(databag.getName(),
                    URLGenerator.generateUrl("data/" + databag.getName()));
        }

        return result;

    }

    @GET
    @Timed(name = "databag-fetch-item")
    @Path("{name:[a-zA-Z0-9.:_-]+}/{itemName:[a-zA-Z0-9:._-]+}")
    public Response fetchItem(@PathParam("name") String databagName,
                              @PathParam("itemName") String itemName) {
        Databag databag = databagDAO.getByName(databagName);

        if(databag != null) {
            DatabagItem item = databag.getItems().get(itemName);
            if(item != null) {
                return Response.ok(item.toGetResponse()).build();
            } else {
                throw new ChefAPIException(404, String.format("Can't find item '%s' in databag '%s'", itemName, databagName));
            }
        } else {
            throw new ChefAPIException(404, String.format("Can't find databag with name '%s'", databagName));
        }

    }

    @GET
    @Timed(name = "databag-fetch")
    @Path("{name:[a-zA-Z0-9.:_-]+}")
    public Map<String, String> fetch(@PathParam("name") String databagName) {
        Databag databag = databagDAO.getByName(databagName);
        if(databag != null) {
            Map<String, String> results = new HashMap<>();

            for(String databagItemName : databag.getItems().keySet()) {
                results.put(
                        databagItemName,
                        URLGenerator.generateUrl(String.format("data/%s/%s", databagName, databagItemName))
                );
            }

            return results;
        } else {
            throw new ChefAPIException(404, String.format("Can't find databag with name '%s'", databagName));
        }
    }

    /*@POST
    @Timed(name = "databag-create-item-path")
    @Path("{name:[a-zA-Z0-9.:_-]+}/{itemName:[a-zA-Z0-9:_-]+}")
    public Response createItemViaPath(@PathParam("name") String databagName,
                                      @PathParam("itemName") String itemName,
                                      Map<String, Object> rawData) {

        if(databagDAO.getByName(databagName) == null) {
            throw new ChefAPIException(405, "Can't do that!");
        }

        rawData.put("id", itemName);
        return createItem(databagName, rawData);
    }*/

    @POST
    @Timed(name = "databag-create-item")
    @Path("{name:[a-zA-Z0-9.:_-]+}")
    public Response createItem(@PathParam("name") String databagName, Map<String, Object> rawData) {
        final Databag existing = databagDAO.getByName(databagName);
        final Databag databag;

        if(existing == null) {
            //if(rawData.keySet().size() > 0) {
                //databag = new Databag(databagName);
            //} else {
                throw new ChefAPIException(404, String.format("Can't find a databag named '%s'", databagName));
            //}
        } else {
            databag = existing;
        }

        // Do the write.
        Map<String, Object> rawItem = (Map<String, Object>) rawData.get("raw_data");
        final String id;
        if(rawItem == null) {
            id = (String)rawData.get("id");
        } else {
            id = (String)rawItem.get("id");
        }

        if(databag.getItems().get(id) == null) {
            final DatabagItem item;
            if(rawItem != null) {
                item = new DatabagItem(id, databagName, rawItem);
            } else {
                item = new DatabagItem(id, databagName, rawData);
            }

            databag.getItems().put(id, item);
            databagDAO.store(databag);
            return Response.status(201).entity(item.toPutResponse()).build();
        } else {
            throw new ChefAPIException(409, String.format("Can't create data bag item '%s', already exists", id));
        }
    }

    @POST
    @Timed(name = "databag-create")
    public Response auth(Databag databag) {
        if(databagDAO.getByName(databag.getName()) == null) {
            databagDAO.store(databag);
            return Response
                .status(201)
                .entity(ImmutableMap.of("uri", URLGenerator.generateUrl("data/" + databag.getName())))
                .build();
        } else {
            throw new ChefAPIException(409, String.format("Can't replace data bag named '%s'", databag.getName()));
        }
    }

    @DELETE
    @Timed(name = "databag-delete")
    @Path("{name:[a-zA-Z0-9.:_-]+}")
    public Response deleteDatabag(@PathParam("name") String databagName) {
        LOG.debug("Deleting " + databagName);
        Databag removed = databagDAO.removeByName(databagName);
        if(removed != null)  {
            return Response.status(200).entity(removed.toResponseMap()).build();
        } else {
            throw new ChefAPIException(404, String.format("Can't find data bag named '%s'", databagName));
        }
    }

    @DELETE
    @Timed(name = "databag-delete-item")
    @Path("{name:[a-zA-Z0-9.:_-]+}/{itemName:[a-zA-Z0-9:._-]+}")
    public Response deleteDatabagItem(@PathParam("name") String databagName,
                                      @PathParam("itemName") String itemName) {

        Databag databag = databagDAO.getByName(databagName);
        if(databag != null) {
            if(databag.getItems().containsKey(itemName)) {
                DatabagItem item = databag.getItems().remove(itemName);
                databagDAO.store(databag);

                // Our own special snowflake here... o.O
                Map<String, Object> results = new HashMap<>();
                // XXX huh?
                results.put("name", String.format("data_bag_item_%s_%s", databagName, itemName));
                results.put("json_class", "Chef::DataBagItem");
                results.put("chef_type", "data_bag_item");
                results.put("data_bag", databagName);
                results.put("raw_data", item.getData());

                return Response.ok(results).build();
            } else {
                throw new ChefAPIException(404, String.format("Databag %s does not contain an item '%s'", databagName, itemName));
            }
        } else {
            throw new ChefAPIException(404, String.format("Can't find databag named '%s'", databagName));
        }
    }

    @PUT
    @Timed(name = "databag-update-item")
    @Path("{name:[a-zA-Z0-9.:_-]+}/{itemName:[a-zA-Z0-9:._-]+}")
    public Response updateDatabagItem(@PathParam("name") String databagName,
                                      @PathParam("itemName") String itemName,
                                      Map<String, Object> updateData) {

        Databag databag = databagDAO.getByName(databagName);
        if(databag != null) {
            if(databag.getItems().containsKey(itemName)) {
                DatabagItem item =  databag.getItems().get(itemName);
                item.setData(updateData);
                databagDAO.store(databag);
                //TODO: This response is just bananas
                return Response.ok(item.toPutResponse()).build();
            } else {
                throw new ChefAPIException(404, String.format("Databag %s does not contain an item '%s'", databagName, itemName));
            }
        } else {
            throw new ChefAPIException(404, String.format("Can't find databag named '%s'", databagName));
        }
    }
}
