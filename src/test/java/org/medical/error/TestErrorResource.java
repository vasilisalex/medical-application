package org.medical.error;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test-errors")
@Produces(MediaType.APPLICATION_JSON)
public class TestErrorResource {

    @GET
    @Path("/notfound")
    public String notFound() {
        throw ApiException.notFound("nf message");
    }

    @GET
    @Path("/badrequest")
    public String badRequest() {
        throw ApiException.badRequest("br message");
    }

    @GET
    @Path("/conflict")
    public String conflict() {
        throw ApiException.conflict("cf message");
    }

    @GET
    @Path("/forbidden")
    public String forbidden() {
        throw new io.quarkus.security.ForbiddenException("nope");
    }
}
