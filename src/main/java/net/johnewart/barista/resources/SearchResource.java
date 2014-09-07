package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import net.johnewart.barista.core.User;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
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

    private final SolrServer solrServer;

    public SearchResource(final SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    @POST
    public Response searchData() {
        //return search();
        return Response.ok().build();
    }

    @GET
    @Timed(name = "search")
    @Path("{index:\\w+}")
    public Response search(@PathParam("index") String searchIndex,
                           @QueryParam("q") String queryString,
                           @QueryParam("sort") Optional<String> sort,
                           @QueryParam("start") Integer start,
                           @QueryParam("rows") Integer rows) {

        try {
            SolrQuery query = new SolrQuery();
            query.setQuery(queryString);
            QueryResponse rsp = solrServer.query(query);
            SolrDocumentList docs = rsp.getResults();

            for(SolrDocument doc : docs) {
                LOG.debug("Document: " + doc.toString());
            }

            return Response
                    .status(200)
                    .build();
        } catch (SolrServerException e) {
            LOG.error("Unable to perform query", e);
            return Response.status(503).build();
        }
    }

}
