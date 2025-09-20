package org.medical.error;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ValidationExceptionMapperTest {

    // Test-only resource co-located for simplicity
    @Path("/test-errors/validation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class TestValidationResource {
        public static class V {
            @NotBlank
            public String name;
            @Email
            public String email;
        }

        @POST
        public String validate(@jakarta.validation.Valid V v) {
            return "ok";
        }
    }

    @Test
    void validation_errors_include_envelope_and_errors_array() {
        String body = "{\n  \"name\": \"\",\n  \"email\": \"not-an-email\"\n}";
        given()
            .contentType("application/json")
            .body(body)
        .when()
            .post("/test-errors/validation")
        .then()
            .statusCode(400)
            .body("error", is("validation_error"))
            .body("code", is("validation_error"))
            .body("message", is("Validation failed"))
            .body("path", endsWith("test-errors/validation"))
            .body("errors", notNullValue())
            .body("errors.size()", greaterThanOrEqualTo(1))
            .body("errors.message.flatten()", hasItem(anyOf(containsString("must not be blank"), containsString("must be a well-formed email address"))));
    }
}

