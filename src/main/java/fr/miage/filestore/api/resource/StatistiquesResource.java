package fr.miage.filestore.api.resource;

import fr.miage.filestore.api.dto.FileStoreStatus;
import fr.miage.filestore.api.filter.OnlyOwner;
import fr.miage.filestore.api.template.Template;
import fr.miage.filestore.api.template.TemplateContent;
import fr.miage.filestore.auth.AuthenticationService;
import fr.miage.filestore.config.FileStoreConfig;
import fr.miage.filestore.files.FileService;
import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.files.exception.NodeNotFoundException;
import fr.miage.filestore.metrics.MetricsService;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.validation.constraints.Null;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/statistiques")
@OnlyOwner
public class StatistiquesResource {
    private static final Logger LOGGER = Logger.getLogger(StatusResource.class.getName());
    @Inject
    private FileStoreConfig config;
    @EJB
    MetricsService metrics;
    @EJB
    private FileService service;
    @EJB
    private AuthenticationService authenticationService;

    @GET
    @Path("{id}")
    @Template(name = "statistiques")
    @Produces(MediaType.TEXT_HTML)
    public Response getStatistiquesHtml(@PathParam("id") String id) throws NodeNotFoundException {
        LOGGER.log(Level.INFO, "GET /api/statistiques (html)");
        Node node = service.get(id);
        TemplateContent<Map<String, Object>> content = new TemplateContent<>();
        Map<String, Object> value = new HashMap<>();
        value.put("ctx", config.instance().ctx());
        value.put("profile", authenticationService.getConnectedProfile());
        value.put("parent", node);
        value.put("status", this.buildStatus());
        value.put("nodes", service.list(id));
        value.put("nb_nodes", service.list("root").size());
        value.put("nb_total_nodes", service.countAllNode());
        value.put("nb_total_download", service.countAllDownload());
        content.setContent(value);
        return Response.ok(content).build();
    }

    private FileStoreStatus buildStatus() {
        FileStoreStatus status = new FileStoreStatus();
        status.setNbCpus(Runtime.getRuntime().availableProcessors());
        status.setTotalMemory(Runtime.getRuntime().totalMemory());
        status.setAvailableMemory(Runtime.getRuntime().freeMemory());
        status.setMaxMemory(Runtime.getRuntime().maxMemory());
        status.setLatestMetrics(metrics.listLatestMetrics());
        status.setMetrics(metrics.listMetrics());
        return status;
    }
}
