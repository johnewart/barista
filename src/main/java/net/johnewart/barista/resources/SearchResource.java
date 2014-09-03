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

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {
    private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);

    public SearchResource() {
    }

    @POST
    public Response searchData() {
        return search();
    }

    @GET
    @Timed(name = "search")
    public Response search() {
        return Response
                .status(200)
                .build();
    }
}
