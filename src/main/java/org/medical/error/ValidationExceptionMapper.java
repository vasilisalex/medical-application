package org.medical.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<Map<String, String>> errors = exception.getConstraintViolations()
                .stream()
                .map(this::toEntry)
                .collect(Collectors.toList());

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("errors", errors))
                .build();
    }

    private Map<String, String> toEntry(ConstraintViolation<?> v) {
        String field = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
        return Map.of(
                "field", field,
                "message", v.getMessage()
        );
    }
}

