package fr.miage.filestore.api.filter;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Properties;

@Provider
public class VersionFilter implements ContainerResponseFilter {

    private static String VERSION = "unknown";

    @Context
    private ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("filestore-version", getVersion());
    }

    public String getVersion() {
        if (VERSION.equals("unknown")) {
            try {
                Properties prop = new Properties();
                prop.load(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"));
                VERSION = prop.getProperty("Implementation-Version");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return VERSION;
    }

}
