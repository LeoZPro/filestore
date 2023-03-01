package fr.miage.filestore.api.resource;

import fr.miage.filestore.api.dto.FileStoreStatus;
import fr.miage.filestore.api.filter.OnlyOwner;
import fr.miage.filestore.api.template.Template;
import fr.miage.filestore.api.template.TemplateContent;
import fr.miage.filestore.auth.AuthenticationService;
import fr.miage.filestore.auth.entity.Profile;
import fr.miage.filestore.config.FileStoreConfig;
import fr.miage.filestore.metrics.MetricsService;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/status")
@OnlyOwner
public class StatusResource {

    private static final Logger LOGGER = Logger.getLogger(StatusResource.class.getName());

    @Inject
    private FileStoreConfig config;

    @EJB
    MetricsService metrics;

    @EJB
    private AuthenticationService authenticationService;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public FileStoreStatus getStatus() {
        LOGGER.log(Level.INFO, "GET /api/status");
        return buildStatus();
    }

    @GET
    @Template(name = "status")
    @Produces(MediaType.TEXT_HTML)
    public Response getStatusHtml() {
        LOGGER.log(Level.INFO, "GET /api/status (html)");
        TemplateContent<Map<String, Object>> content = new TemplateContent<>();
        Map<String, Object> value = new HashMap<>();
        value.put("ctx", config.instance().ctx());
        value.put("profile", authenticationService.getConnectedProfile());
        value.put("status", this.buildStatus());
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
