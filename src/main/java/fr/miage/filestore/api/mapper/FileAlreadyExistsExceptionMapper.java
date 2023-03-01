package fr.miage.filestore.api.mapper;

import fr.miage.filestore.files.exception.NodeAlreadyExistsException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class FileAlreadyExistsExceptionMapper implements ExceptionMapper<NodeAlreadyExistsException> {

    @Override
    public Response toResponse(NodeAlreadyExistsException e) {
        return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
    }
}