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
    public static final String SCHEMA_NOT_FOUND_FOR_REFERENCE = "Schema %s not found";
    public static final String SCHEMA_VERSION_NOT_FOUND = "Version %s does not exist for schema %s";
    public static final String SCHEMA_ALREADY_PUBLISHED = "Schema %s already has published version %s";
    public static final String SCHEMA_CREATION_FAILED = "Failed to create Schema, Please contact support";
    public static final String SCHEMA_IN_USE = "Schema %s cannot be unpublished as it is in use by external references";
    public static final String SCHEMA_ALREADY_LOCKED = "Schema %s is already locked by %s";
    public static final String SCHEMA_UNLOCK_NOT_PERMITTED = "Cannot unlock - schema %s is locked by %s";

    // Schema validation messages
    public static final String SCHEMA_ID_CANNOT_BE_NULL = "Schema ID cannot be null in request";
    public static final String CANNOT_REFERENCE_UNPUBLISHED_SCHEMA = "Cannot create reference to unpublished schema %s. Schema must be published before creating external references.";

    // External reference messages
    public static final String EXTERNAL_REFERENCE_DUPLICATE = "External reference with name='%s', type='%s', version='%s' already exists with ID %s. Cannot create a different external reference with the same name, type, and version.";
    public static final String EXTERNAL_REFERENCE_UP_TO_DATE = "External reference is already up to date. No changes were made.";
    public static final String EXTERNAL_REFERENCE_CREATED_SUCCESS = "External reference created successfully.";
    public static final String EXTERNAL_REFERENCE_IMMUTABLE = "External reference with ID %s and version '%s' already exists and is immutable. Cannot modify name, type, or schema associations for an existing version. Create a new version if changes are needed.";
    public static final String EXTERNAL_REFERENCE_VERSION_IMMUTABLE = "External reference version '%s' for ID %s is immutable. Existing schemas: %s. Requested schemas: %s. Create a new version if different schema associations are needed.";

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