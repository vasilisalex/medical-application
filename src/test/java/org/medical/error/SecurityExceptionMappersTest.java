package org.medical.error;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class SecurityExceptionMappersTest {

    @Test
    void unauthorized_without_token_has_uniform_envelope() {
        given()
        .when()
            .get("/patients")
        .then()
            .statusCode(401)
            .body("error", is("unauthorized"))
            .body("code", is("unauthorized"))
            .body("message", startsWith("Unauthorized"));
    }

    @Test
    void forbidden_mapper_has_uniform_envelope() {
        given()
        .when()
            .get("/test-errors/forbidden")
        .then()
            .statusCode(403)
            .body("error", is("forbidden"))
            .body("code", is("forbidden"))
            .body("message", is("nope"));
    }
}

