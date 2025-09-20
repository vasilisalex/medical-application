package org.medical.error;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(BadRequestException exception) {
        String path = uriInfo != null && uriInfo.getPath() != null ? uriInfo.getPath() : "";
        String message = exception.getMessage() != null ? exception.getMessage() : "Bad request";
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "error", "bad_request",
                        "message", message,
                        "path", path
                ))
                .build();
    }
}

