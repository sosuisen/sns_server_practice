package com.example.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Payload;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ValidateUnwrappedValue;
import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.exception.JDBCConnectionException;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

class ErrorMappersTest {

    @Test
    void mapConstraintViolation_genericViolation_returns400() {
        var mappers = new ErrorMappers();

        var response = mappers.mapConstraintViolation(violationOf("USERNAME_REQUIRED"));

        assertEquals(400, response.getStatus());
        var errors = errors(response);
        assertEquals("USERNAME_REQUIRED", errors.getFirst().get("code"));
    }

    @Test
    void mapEmailInUse_returns409() {
        var mappers = new ErrorMappers();

        var response = mappers.mapEmailInUse(new EmailInUseException());

        assertEquals(409, response.getStatus());
        var errors = errors(response);
        assertEquals("EMAIL_IN_USE", errors.getFirst().get("code"));
    }

    @Test
    void mapJdbcConnection_returnsDatabaseUnavailableWith503() {
        var mappers = new ErrorMappers();
        var exception = new JDBCConnectionException("connection failed", new SQLException("io failed"));

        var response = mappers.mapJdbcConnection(exception);

        assertEquals(503, response.getStatus());
        var errors = errors(response);
        assertEquals("DATABASE_UNAVAILABLE", errors.getFirst().get("code"));
        assertEquals(Map.of(), errors.getFirst().get("params"));
    }

    @Test
    void mapPersistence_returnsDatabaseErrorWith500() {
        var mappers = new ErrorMappers();

        var response = mappers.mapPersistence(new PersistenceException("db failed"));

        assertEquals(500, response.getStatus());
        var errors = errors(response);
        assertEquals("DATABASE_ERROR", errors.getFirst().get("code"));
        assertEquals(Map.of(), errors.getFirst().get("params"));
    }

    /**
     * @Transactional メソッド内の JDBCConnectionException は Narayana TX インターセプターが
     * PersistenceException でラップして再スローする。このケースを 503 に変換できることを確認する。
     */
    @Test
    void mapPersistence_wrappedJdbcConnectionException_returnsDatabaseUnavailableWith503() {
        var mappers = new ErrorMappers();
        var cause = new JDBCConnectionException("connection lost", new SQLException("io error"));
        var wrapped = new PersistenceException(cause);

        var response = mappers.mapPersistence(wrapped);

        assertEquals(503, response.getStatus());
        var errors = errors(response);
        assertEquals("DATABASE_UNAVAILABLE", errors.getFirst().get("code"));
        assertEquals(Map.of(), errors.getFirst().get("params"));
    }

    private static ConstraintViolationException violationOf(String message) {
        return new ConstraintViolationException(Set.of(stubViolation(message)));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> errors(RestResponse<?> response) {
        return (List<Map<String, Object>>) ((Map<?, ?>) response.getEntity()).get("errors");
    }

    private static ConstraintViolation<?> stubViolation(String message) {
        return new ConstraintViolation<>() {
            @Override public String getMessage() { return message; }
            @Override public String getMessageTemplate() { return message; }
            @Override public Object getRootBean() { return null; }
            @Override public Class<Object> getRootBeanClass() { return null; }
            @Override public Object getLeafBean() { return null; }
            @Override public Object[] getExecutableParameters() { return null; }
            @Override public Object getExecutableReturnValue() { return null; }
            @Override public jakarta.validation.Path getPropertyPath() { return null; }
            @Override public Object getInvalidValue() { return null; }
            @Override public <U> U unwrap(Class<U> type) { return null; }
            @Override public ConstraintDescriptor<?> getConstraintDescriptor() {
                return new ConstraintDescriptor<>() {
                    @Override public Annotation getAnnotation() { return null; }
                    @Override public String getMessageTemplate() { return message; }
                    @Override public Set<Class<?>> getGroups() { return Set.of(); }
                    @Override public Set<Class<? extends Payload>> getPayload() { return Set.of(); }
                    @Override public ConstraintTarget getValidationAppliesTo() { return null; }
                    @Override public List<Class<? extends ConstraintValidator<Annotation, ?>>> getConstraintValidatorClasses() { return List.of(); }
                    @Override public Map<String, Object> getAttributes() { return Map.of(); }
                    @Override public Set<ConstraintDescriptor<?>> getComposingConstraints() { return Set.of(); }
                    @Override public boolean isReportAsSingleViolation() { return false; }
                    @Override public ValidateUnwrappedValue getValueUnwrapping() { return null; }
                    @Override public <U> U unwrap(Class<U> type) { return null; }
                };
            }
        };
    }
}
