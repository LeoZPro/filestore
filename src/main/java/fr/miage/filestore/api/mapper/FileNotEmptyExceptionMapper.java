package fr.miage.filestore.api.mapper;

import fr.miage.filestore.files.exception.NodeNotEmptyException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class FileNotEmptyExceptionMapper implements ExceptionMapper<NodeNotEmptyException> {

    @Override
    public Response toResponse(NodeNotEmptyException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}