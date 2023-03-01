package fr.miage.filestore.api.resource;

import fr.miage.filestore.api.dto.FileUploadForm;
import fr.miage.filestore.api.filter.OnlyOwner;
import fr.miage.filestore.api.template.Template;
import fr.miage.filestore.api.template.TemplateContent;
import fr.miage.filestore.auth.AuthenticationService;
import fr.miage.filestore.config.FileStoreConfig;
import fr.miage.filestore.files.FileService;
import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.files.exception.*;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/files")
@OnlyOwner
public class FilesResource {

    private static final Logger LOGGER = Logger.getLogger(FilesResource.class.getName());

    @Inject
    private FileStoreConfig config;

    @EJB
    private FileService service;

    @EJB
    private AuthenticationService authenticationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response root(@Context UriInfo uriInfo) throws NodeNotFoundException {
        LOGGER.log(Level.INFO, "GET /api/files");
        Node node = service.get("");
        URI root = uriInfo.getRequestUriBuilder().path(node.getId()).build();
        return Response.seeOther(root).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response rootView(@Context UriInfo uriInfo) throws NodeNotFoundException {
        LOGGER.log(Level.INFO, "GET /api/files (html)");
        Node node = service.get("");
        URI root = uriInfo.getRequestUriBuilder().path(node.getId()).path("content").build();
        return Response.seeOther(root).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Node get(@PathParam("id") String id) throws NodeNotFoundException {
        LOGGER.log(Level.INFO, "GET /api/files/" + id);
        Node node = service.get(id);
        return node;
    }

    @GET
    @Path("{id}/content")
    @Produces(MediaType.APPLICATION_JSON)
    public Response content(@PathParam("id") String id) throws NodeNotFoundException {
        LOGGER.log(Level.INFO, "GET /api/files/" + id + "/content");
        Node node = service.get(id);
        if (node.getType().equals(Node.Type.TREE)) {
            return Response.ok(service.list(node.getId())).build();
        } else {
            return Response.ok(service.getContent(id))
                    .header("Content-Type", node.getMimetype())
                    .header("Content-Length", node.getSize())
                    .header("Content-Disposition", "attachment; filename=" + node.getName()).build();
        }
    }

    @GET
    @Path("{id}/content")
    @Template(name = "files")
    @Produces(MediaType.TEXT_HTML)
    public Response contentView(@PathParam("id") String id, @QueryParam("download") @DefaultValue("false") boolean download) throws NodeNotFoundException {
        LOGGER.log(Level.INFO, "GET /api/files/" + id + "/content (html)");
        Node node = service.get(id);
        if (node.getType().equals(Node.Type.TREE)) {
            service.updateFolderStatistique(id);
            TemplateContent<Map<String, Object>> content = new TemplateContent<>();
            Map<String, Object> value = new HashMap<>();
            value.put("ctx", config.instance().ctx());
            value.put("profile", authenticationService.getConnectedProfile());
            value.put("parent", node);
            value.put("path", service.path(id));
            value.put("nodes", service.list(id));
            value.put("nb_nodes", service.list(id).size());
            content.setContent(value);
            return Response.ok(content).build();
        } else {
            // Ici besoin d'incrémenter le nombre de download du fichier / d'entrée dans le dossier
            service.updateFileStatistique(id);
            return Response.ok(service.getContent(id))
                    .header("Content-Type", node.getMimetype())
                    .header("Content-Length", node.getSize())
                    .header("Content-Disposition", ((download) ? "attachment; " : "") + "filename=" + node.getName()).build();
        }
    }


    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response add(@PathParam("id") String id, @MultipartForm @Valid FileUploadForm form, @Context UriInfo info) throws NodeNotFoundException, NodeTypeException, NodeAlreadyExistsException, ContentException, FileServiceException {
        LOGGER.log(Level.INFO, "POST /api/files/" + id);
        Node node;
        if (form.getData() != null) {
            node = service.add(id, form.getName(), form.getData());
        } else {
            node = service.add(id, form.getName());
        }
        URI createdUri = info.getBaseUriBuilder().path(FilesResource.class).path(node.getId()).build();
        return Response.created(createdUri).build();
    }

    @POST
    @Path("{id}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addView(@PathParam("id") String id, @MultipartForm @Valid FileUploadForm form, @Context UriInfo info) throws NodeNotFoundException, NodeTypeException, NodeAlreadyExistsException, ContentException, FileServiceException {
        LOGGER.log(Level.INFO, "POST /api/files/" + id + " (html)");
        if (form.getData() != null) {
            service.add(id, form.getName(), form.getData());
        } else {
            service.add(id, form.getName());
        }
        URI createdUri = info.getBaseUriBuilder().path(FilesResource.class).path(id).path("content").build();
        return Response.seeOther(createdUri).build();
    }

    @PUT
    @Path("{id}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response update(@PathParam("id") String id, @PathParam("name") String name, @MultipartForm FileUploadForm form) throws NodeNotEmptyException, NodeNotFoundException, NodeTypeException, NodeAlreadyExistsException, ContentException, FileServiceException {
        LOGGER.log(Level.INFO, "PUT /api/files/" + id + "/" + name);
        service.remove(id, name);
        service.add(id, name, form.getData());
        return Response.noContent().build();
    }


    @DELETE
    @Path("{id}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id, @PathParam("name") String name) throws NodeNotEmptyException, NodeNotFoundException, FileServiceException {
        LOGGER.log(Level.INFO, "DELETE /api/files/" + name);
        service.remove(id, name);
        return Response.noContent().build();
    }

}
