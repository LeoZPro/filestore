package fr.miage.filestore.api.filter;

import fr.miage.filestore.metrics.MetricsService;

import javax.ejb.EJB;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class MetricsFilter implements ContainerRequestFilter {

    @EJB
    MetricsService metrics;

    @Override
    public void filter(ContainerRequestContext ctx) {
        if (metrics.getLatestMetric("upload") > 20) {
            ctx.abortWith(Response.status(Response.Status.TOO_MANY_REQUESTS).build());
        }
    }

}
