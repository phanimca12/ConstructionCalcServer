package com.ssnc.schemaService.constants;

/**
 * Centralized error and informational messages
 */
public final class ErrorMessages {

    private ErrorMessages() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    // Schema related messages
    public static final String SCHEMA_ALREADY_EXISTS = "Schema with name %s already exists";
    public static final String SCHEMA_NOT_FOUND = "Schema not found: %s";
    public static final String SCHEMA_VERSION_NOT_FOUND = "Version %s does not exist for schema %s";
    public static final String SCHEMA_ALREADY_PUBLISHED = "Schema %s already has published version %s";
    public static final String SCHEMA_CREATION_FAILED = "Failed to create Schema, Please contact support";

    // Tenant related messages
    public static final String TENANT_NOT_FOUND = "Tenant not found";
    public static final String TENANT_ALREADY_ONBOARDED = "Tenant already onboarded: %s";
    public static final String JWT_CONTEXT_NOT_POPULATED = "JWT claims context is not populated. Cannot create tenants.";
    public static final String NO_TENANT_IN_JWT = "No tenant found in JWT context";
    public static final String TENANT_ONBOARDING_SUCCESS = "Successfully created tenant: %s";
    public static final String TENANT_ONBOARDING_PREPARING = "Preparing to create new tenant: %s";
    public static final String TENANT_CREATION_FAILED = "Failed to create Tenant, Please contact support";

    // Namespace related messages
    public static final String NAMESPACE_NOT_FOUND = "Namespace not found";

    // JWT context messages
    public static final String JWT_CONTEXT_NOT_FOUND = "Context not Found";

    // Sync messages
    public static final String ERROR_SYNCING_TENANTS = "Error syncing tenants from JWT claims context: {}";
}