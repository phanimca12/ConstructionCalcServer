package com.ssnc.schemaService.util;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseExceptionUtilsTest {

    @Test
    void testIsUniqueConstraintViolation_PostgreSQL_UniqueViolation() {
        // PostgreSQL unique constraint violation (SQLState 23505)
        SQLException sqlEx = new SQLException("duplicate key value violates unique constraint \"schm_name_unique\"", "23505");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "schm_name"));
        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e));
    }

    @Test
    void testIsUniqueConstraintViolation_MySQL_UniqueViolation() {
        // MySQL unique constraint violation (SQLState 23000)
        SQLException sqlEx = new SQLException("Duplicate entry 'test' for key 'schm_name'", "23000");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "schm_name"));
        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e));
    }

    @Test
    void testIsUniqueConstraintViolation_WrongColumn() {
        // Unique constraint on different column
        SQLException sqlEx = new SQLException("duplicate key value violates unique constraint \"tenant_name_unique\"", "23505");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertFalse(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "schm_name"));
        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "tenant"));
        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e)); // No column check
    }

    @Test
    void testIsUniqueConstraintViolation_CaseInsensitive() {
        // Test case-insensitive matching
        SQLException sqlEx = new SQLException("duplicate key value violates unique constraint \"SCHM_NAME_UNIQUE\"", "23505");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "schm_name"));
        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "SCHM_NAME"));
        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "ScHm_NaMe"));
    }

    @Test
    void testIsUniqueConstraintViolation_ForeignKeyViolation() {
        // Foreign key constraint violation (SQLState 23503)
        SQLException sqlEx = new SQLException("foreign key constraint violation", "23503");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertFalse(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "schm_name"));
        assertFalse(DatabaseExceptionUtils.isUniqueConstraintViolation(e));
    }

    @Test
    void testIsUniqueConstraintViolation_NotNullViolation() {
        // Not null constraint violation (SQLState 23502)
        SQLException sqlEx = new SQLException("null value in column violates not-null constraint", "23502");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertFalse(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "schm_name"));
        assertFalse(DatabaseExceptionUtils.isUniqueConstraintViolation(e));
    }

    @Test
    void testIsUniqueConstraintViolation_NoSQLException() {
        // Exception without SQLException as root cause
        DataIntegrityViolationException e = new DataIntegrityViolationException("Generic constraint violation");

        assertFalse(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "schm_name"));
        assertFalse(DatabaseExceptionUtils.isUniqueConstraintViolation(e));
    }

    @Test
    void testIsUniqueConstraintViolation_NullColumnName() {
        // Null column name should match any unique constraint
        SQLException sqlEx = new SQLException("duplicate key value violates unique constraint", "23505");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, null));
    }

    @Test
    void testIsUniqueConstraintViolation_EmptyColumnName() {
        // Empty column name should match any unique constraint
        SQLException sqlEx = new SQLException("duplicate key value violates unique constraint", "23505");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, ""));
    }

    @Test
    void testIsUniqueConstraintViolation_PartialMatch() {
        // Partial column name should match (e.g., "tenant" matches "tenant_name")
        SQLException sqlEx = new SQLException("duplicate key value violates unique constraint \"uk_tenant_name\"", "23505");
        DataIntegrityViolationException e = new DataIntegrityViolationException("Constraint violation", sqlEx);

        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "tenant"));
        assertTrue(DatabaseExceptionUtils.isUniqueConstraintViolation(e, "tenant_name"));
    }

    @Test
    void testConstructor_ThrowsException() {
        // Test that utility class cannot be instantiated
        // Reflection wraps the IllegalStateException in InvocationTargetException
        java.lang.reflect.InvocationTargetException exception = assertThrows(
            java.lang.reflect.InvocationTargetException.class, () -> {
                java.lang.reflect.Constructor<DatabaseExceptionUtils> constructor =
                    DatabaseExceptionUtils.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            });

        // Verify the cause is IllegalStateException with correct message
        assertNotNull(exception.getCause());
        assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertEquals("Utility class - cannot be instantiated", exception.getCause().getMessage());
    }
}
