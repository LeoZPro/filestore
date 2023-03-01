package fr.miage.filestore.api.resource;

import fr.miage.filestore.api.filter.OnlyOwner;
import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.files.exception.NodeNotFoundException;
import fr.miage.filestore.store.BinaryStore;
import fr.miage.filestore.zip.ZipService;
import fr.miage.filestore.zip.ZipServiceException;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

@Path("/action")
@OnlyOwner
public class ActionResource {

    private static final Logger LOGGER = Logger.getLogger(ActionResource.class.getName());

    @EJB
    private ZipService zipService;

    @POST
    @Path("zip/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response Zip(@PathParam("id") String id) throws ZipServiceException, NodeNotFoundException {
        zipService.zip(id);
        return Response.noContent().build();
    }

    @POST
    @Path("unzip/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response Unzip(@PathParam("id") String id) throws ZipServiceException {
        zipService.unzip(id);
        return Response.noContent().build();
    }
}
