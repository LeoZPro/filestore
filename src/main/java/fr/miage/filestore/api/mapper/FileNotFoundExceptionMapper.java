package fr.miage.filestore.api.mapper;

import fr.miage.filestore.files.exception.NodeNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class FileNotFoundExceptionMapper implements ExceptionMapper<NodeNotFoundException> {

    @Override
    public Response toResponse(NodeNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
}