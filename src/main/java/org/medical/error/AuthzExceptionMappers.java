package org.medical.error;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

public class AuthzExceptionMappers {
    @Provider
    public static class NotAuthorizedMapper implements ExceptionMapper<NotAuthorizedException> {
        @Context
        UriInfo uriInfo;

        @Override
        public Response toResponse(NotAuthorizedException exception) {
            String path = uriInfo != null && uriInfo.getPath() != null ? uriInfo.getPath() : "";
            String message = exception.getMessage() != null ? exception.getMessage() : "Unauthorized";
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(
                            "error", "unauthorized",
                            "message", message,
                            "path", path
                    ))
                    .build();
        }
    }

    @Provider
    public static class ForbiddenMapper implements ExceptionMapper<ForbiddenException> {
        @Context
        UriInfo uriInfo;

        @Override
        public Response toResponse(ForbiddenException exception) {
            String path = uriInfo != null && uriInfo.getPath() != null ? uriInfo.getPath() : "";
            String message = exception.getMessage() != null ? exception.getMessage() : "Forbidden";
            return Response.status(Response.Status.FORBIDDEN)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(
                            "error", "forbidden",
                            "message", message,
                            "path", path
                    ))
                    .build();
        }
    }
}

