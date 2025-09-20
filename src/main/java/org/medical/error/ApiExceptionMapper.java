package org.medical.error;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ApiException exception) {
        String path = uriInfo != null && uriInfo.getPath() != null ? uriInfo.getPath() : "";
        return Response.status(exception.status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "error", exception.code,
                        "code", exception.code,
                        "message", exception.getMessage() != null ? exception.getMessage() : "",
                        "path", path
                ))
                .build();
    }
}
