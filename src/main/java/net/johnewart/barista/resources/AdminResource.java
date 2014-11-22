package net.johnewart.barista.resources;

import io.dropwizard.views.View;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.ClientDAO;
import net.johnewart.barista.data.CookbookDAO;
import net.johnewart.barista.data.UserDAO;
import net.johnewart.barista.views.ClientsView;
import net.johnewart.barista.views.CookbooksView;
import net.johnewart.barista.views.DashboardView;
import net.johnewart.barista.views.UsersView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/admin")
@Produces(MediaType.TEXT_HTML)
public class AdminResource {
    private static final Logger LOG = LoggerFactory.getLogger(AdminResource.class);

    private final UserDAO userDAO;
    private final CookbookDAO cookbookDAO;
    private final ClientDAO clientDAO;

    public AdminResource(UserDAO userDAO, CookbookDAO cookbookDAO, ClientDAO clientDAO) {
        this.userDAO = userDAO;
        this.cookbookDAO = cookbookDAO;
        this.clientDAO = clientDAO;
    }

    @GET
    public View home() {
        return new DashboardView();
    }

    @GET
    @Path("users")
    public View users() {
        return new UsersView(userDAO);
    }

    @GET
    @Path("users/{name:.*?}/keys")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadUserKeys(@PathParam("name") String username) {

        final User u = userDAO.getByName(username);

        StreamingOutput streamOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException,
                    WebApplicationException {
                ZipOutputStream zos = new ZipOutputStream(os);
                OutputStreamWriter writer = new OutputStreamWriter(zos);

                if(u.getPublicKey() != null) {
                    zos.putNextEntry(new ZipEntry("public_key.pem"));
                    writer.write(u.getPublicKey());
                    writer.flush();
                    zos.closeEntry();
                }

                if(u.getPrivateKey() != null) {
                    zos.putNextEntry(new ZipEntry("private_key.pem"));
                    writer.write(u.getPrivateKey());
                    writer.flush();
                    zos.closeEntry();
                }

                zos.close();
            }
        };

        return Response.ok(streamOutput).header("Content-Disposition", "attachment; filename=\"keys.zip\"").build();
    }

    @GET
    @Path("clients/{name:.*?}/keys")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadClientKeys(@PathParam("name") String clientName) {

        final Client c = clientDAO.getByName(clientName);

        StreamingOutput streamOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException,
                    WebApplicationException {
                ZipOutputStream zos = new ZipOutputStream(os);
                OutputStreamWriter writer = new OutputStreamWriter(zos);

                if(c.getPublicKey() != null) {
                    zos.putNextEntry(new ZipEntry("public_key.pem"));
                    writer.write(c.getPublicKey());
                    writer.flush();
                    zos.closeEntry();
                }

                if(c.getPrivateKey() != null) {
                    zos.putNextEntry(new ZipEntry("private_key.pem"));
                    writer.write(c.getPrivateKey());
                    writer.flush();
                    zos.closeEntry();
                }

                zos.close();
            }
        };

        return Response.ok(streamOutput).header("Content-Disposition", "attachment; filename=\"" + clientName + "_keys.zip\"").build();
    }

    @GET
    @Path("cookbooks")
    public View cookbooks() {
        return new CookbooksView(cookbookDAO);
    }

    @GET
    @Path("clients")
    public View clients() {
        return new ClientsView(clientDAO);
    }
}
