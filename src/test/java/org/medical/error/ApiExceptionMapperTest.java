package org.medical.error;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ApiExceptionMapperTest {

    @Test
    void notFound_is_mapped_with_uniform_envelope() {
        given()
        .when().get("/test-errors/notfound")
        .then()
            .statusCode(404)
            .body("error", is("not_found"))
            .body("code", is("not_found"))
            .body("message", is("nf message"))
            .body("path", endsWith("test-errors/notfound"))
            .body("errors", anyOf(nullValue(), anEmptyMap()));
    }

    @Test
    void badRequest_is_mapped_with_uniform_envelope() {
        given()
        .when().get("/test-errors/badrequest")
        .then()
            .statusCode(400)
            .body("error", is("bad_request"))
            .body("code", is("bad_request"))
            .body("message", is("br message"))
            .body("path", endsWith("test-errors/badrequest"))
            .body("errors", anyOf(nullValue(), anEmptyMap()));
    }

    @Test
    void conflict_is_mapped_with_uniform_envelope() {
        given()
        .when().get("/test-errors/conflict")
        .then()
            .statusCode(409)
            .body("error", is("conflict"))
            .body("code", is("conflict"))
            .body("message", is("cf message"))
            .body("path", endsWith("test-errors/conflict"))
            .body("errors", anyOf(nullValue(), anEmptyMap()));
    }
}

