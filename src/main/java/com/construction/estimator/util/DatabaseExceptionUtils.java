package com.construction.estimator.util;

import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

/**
 * Utility class for handling database exceptions in a database-agnostic way.
 * Uses standard JDBC SQLState codes to identify constraint violations.
 */
public final class DatabaseExceptionUtils {

    private DatabaseExceptionUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    /**
     * Checks if a DataIntegrityViolationException is a unique constraint violation
     * using JDBC SQLState codes (database-agnostic).
     *
     * Standard SQLState codes for unique constraint violations:
     * - 23505: PostgreSQL unique_violation
     * - 23000: MySQL/MariaDB integrity_constraint_violation
     * - 23505: H2 unique_violation
     *
     * @param e - The exception to check
     * @param columnName - The column name to check in the error message (optional, can be null)
     * @return true if it's a unique constraint violation on the specified column
     */
    public static boolean isUniqueConstraintViolation(DataIntegrityViolationException e, String columnName) {
        Throwable rootCause = e.getRootCause();

        if (rootCause instanceof SQLException) {
            SQLException sqlEx = (SQLException) rootCause;
            String sqlState = sqlEx.getSQLState();

            // Check for unique constraint violation SQLState codes
            if ("23505".equals(sqlState) || "23000".equals(sqlState)) {
                // If no specific column name provided, return true for any unique constraint
                if (columnName == null || columnName.isEmpty()) {
                    return true;
                }

                // Verify it's specifically the target constraint
                String message = sqlEx.getMessage();
                return message != null && message.toLowerCase().contains(columnName.toLowerCase());
            }
        }

        return false;
    }

    /**
     * Checks if a DataIntegrityViolationException is a unique constraint violation
     * without checking for a specific column name.
     *
     * @param e - The exception to check
     * @return true if it's any unique constraint violation
     */
    public static boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        return isUniqueConstraintViolation(e, null);
    }
}
