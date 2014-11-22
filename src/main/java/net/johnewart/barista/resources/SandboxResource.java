package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.core.Sandbox;
import net.johnewart.barista.core.SandboxResponse;
import net.johnewart.barista.data.storage.FileStorageEngine;
import net.johnewart.barista.data.SandboxDAO;
import net.johnewart.barista.exceptions.ChefAPIException;
import net.johnewart.barista.utils.URLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/sandboxes")
@Produces(MediaType.APPLICATION_JSON)
public class SandboxResource {
    private static final Logger LOG = LoggerFactory.getLogger(SandboxResource.class);
    // TODO: make this variable and ensure existence
    private final String BASE_PATH = "/tmp/chef";
    private final SandboxDAO sandboxDAO;
    private final FileStorageEngine fileStorage;

    public SandboxResource(SandboxDAO sandboxDAO, FileStorageEngine fileStorage) {
        this.sandboxDAO = sandboxDAO;
        this.fileStorage = fileStorage;
    }

    @POST
    @Timed(name = "sandbox-create")
    public Response create(Sandbox sandbox) {
        Map<String, FileStatus> results = new HashMap<>();

        for(String checksum : sandbox.getChecksums().keySet()) {
            final File f = getFile(checksum);
            final FileStatus result;
            final String url = getUrl(checksum);
            if(!f.exists()) {
                result = new FileStatus(url, true);
            } else {
                result = new FileStatus(url, false);
            }

            results.put(checksum, result);
        }

        String uri = URLGenerator.generateUrl(String.format("sandboxes/%s", sandbox.getId()));
        SandboxResponse response = new SandboxResponse(uri, sandbox.getId(), results);
        sandboxDAO.store(sandbox);

        return Response
                .status(201)
                .entity(response)
                .build();
    }

    @PUT
    @Timed(name = "sandbox-update")
    @Path("{sandboxId:.*?}")
    public Response update(@PathParam("sandboxId") String sandboxId, Sandbox sandbox) {
        Sandbox existing = sandboxDAO.getById(sandboxId);
        Map<String, String> checksums = existing.getChecksums();

        if(checksums != null) {
            for(String checksum : checksums.keySet()) {
                if(!fileStorage.contains(checksum)) {
                    throw new ChefAPIException(503, String.format("Checksum not uploaded: %s", checksum));
                }
            }
        }

        existing.setCompleted(sandbox.isCompleted());
        sandboxDAO.store(existing);
        return Response.ok(existing).build();
    }

    @GET
    @Timed(name = "sandbox-list")
    public List<Sandbox> list() {
        List<Sandbox> sandboxes = new ArrayList<>();
        sandboxes.add(new Sandbox());
        return sandboxes;
    }

    @DELETE
    @Timed(name = "sandbox-delete")
    @Path("{name:.*}")
    public Response delete(@PathParam("name") String sandboxName) {
        LOG.debug("Deleting " + sandboxName);
        return Response.status(201).build();
    }

    private String getUrl(String checksum) {
        return URLGenerator.generateUrl(String.format("file_store/%s", checksum));
    }

    private File getFile(String checksum) {
        return new File(String.format("%s/%s", this.BASE_PATH, checksum));
    }

    public class FileStatus {

        @JsonProperty("needs_upload")
        public final boolean needsUpload;

        @JsonProperty("url")
        public final String url;

        public FileStatus(String url, boolean needsUpload) {
            this.url = url;
            this.needsUpload = needsUpload;
        }

    }
}
