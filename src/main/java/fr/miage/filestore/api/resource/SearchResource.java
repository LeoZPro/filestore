package fr.miage.filestore.api.resource;

import fr.miage.filestore.files.FileService;
import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.files.exception.FileServiceException;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("search")
public class SearchResource {

    private static final Logger LOGGER = Logger.getLogger(SearchResource.class.getName());

    @EJB
    private FileService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Node> search(@QueryParam("q") String query) throws FileServiceException {
        LOGGER.log(Level.INFO, "GET /api/search");
        return service.search(query);
    }
}
