package net.johnewart.barista.resources;

import com.codahale.metrics.annotation.Timed;
import net.johnewart.barista.data.storage.FileStorageEngine;
import net.johnewart.barista.exceptions.ChefAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

@Path("/file_store")
@Produces(MediaType.APPLICATION_JSON)
public class FileStoreResource {
    private static final Logger LOG = LoggerFactory.getLogger(FileStoreResource.class);
    private final FileStorageEngine fileStorageEngine;

    public FileStoreResource(FileStorageEngine fileStorageEngine) {
        this.fileStorageEngine = fileStorageEngine;
    }

    @GET
    @Timed(name = "filestore-fetch")
    @Path("{checksum:[a-fA-F0-9]+}")
    public Response fetch(@PathParam("checksum") String checksum) {
       if(fileStorageEngine.contains(checksum)) {
           try {
               BufferedReader br = new BufferedReader(
                       new InputStreamReader(fileStorageEngine.getResource(checksum)));
               String data = "";
               String line = null;
               char[] buffer = new char[1024];
               int bytesread = 0;
               while((bytesread = br.read(buffer)) != -1) {
                   data += String.valueOf(buffer, 0, bytesread);
               }
               br.close();
               return Response.ok(data).build();
           } catch (IOException e) {
               throw new WebApplicationException(500);
           }
       } else {
           throw new ChefAPIException(404, "Not found");
       }
    }

    @PUT
    @Timed(name = "filestore-upload")
    @Path("{checksum:[a-fA-F0-9]+}")
    public Response storeFile(@PathParam("checksum") String fileChecksum,
                              String stuff) {

        try {
            if(!fileStorageEngine.contains(fileChecksum)) {
                fileStorageEngine.store(fileChecksum, stuff);
            }
            return Response.ok(new HashMap<String, String>()).build();
        } catch (IOException e) {
            LOG.error("Problem writing data: ", e);
            throw new WebApplicationException(500);
        }
    }
}
