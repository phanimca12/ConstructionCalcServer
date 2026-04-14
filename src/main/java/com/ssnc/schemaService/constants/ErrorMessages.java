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
    public static final String SCHEMA_IMPORT_EXISTS = "Schema name already exist, existing schemas can not be modified using import";
    public static final String SCHEMA_NOT_FOUND = "Schema not found: %s";
    public static final String SCHEMA_NOT_FOUND_BY_ID = "Schema not found with ID: %s";
    public static final String SCHEMA_NOT_FOUND_BY_NAME = "Schema not found with name: %s";
    public static final String SCHEMA_NOT_FOUND_AFTER_IMPORT = "Schema not found immediately after import - possible data corruption";
    public static final String SCHEMA_NOT_FOUND_FOR_REFERENCE = "Schema %s not found";
    public static final String SCHEMA_VERSION_NOT_FOUND = "Version %s does not exist for schema %s";
    public static final String SCHEMA_NO_PUBLISHED_VERSION = "Schema does not have a published version: %s";
    public static final String SCHEMA_PUBLISHED_CONTENT_NOT_FOUND = "Published content not found for schema: %s";
    public static final String SCHEMA_ALREADY_PUBLISHED = "Schema %s already has published version %s";
    public static final String SCHEMA_CREATION_FAILED = "Failed to create Schema, Please contact support";
    public static final String SCHEMA_IN_USE = "Schema %s cannot be unpublished as it is in use by external references";
    public static final String SCHEMA_ALREADY_LOCKED = "Schema %s is already locked by %s";
    public static final String SCHEMA_UNLOCK_NOT_PERMITTED = "Cannot unlock - schema %s is locked by %s";
    public static final String SCHEMA_PUBLISH_FAILED_ON_IMPORT = "Failed to publish initial version during import";

    // Schema validation messages
    public static final String SCHEMA_ID_CANNOT_BE_NULL = "Schema ID cannot be null in request";
    public static final String SCHEMA_CONTENT_REQUIRED = "Content is required for schema import";
    public static final String SCHEMA_ID_OR_NAME_REQUIRED = "Either schmId or name must be provided";
    public static final String CANNOT_REFERENCE_UNPUBLISHED_SCHEMA = "Cannot create reference to unpublished schema %s. Schema must be published before creating external references.";

    // External reference messages
    public static final String EXTERNAL_REFERENCE_DUPLICATE = "External reference with name='%s', type='%s', version='%s' already exists with ID %s. Cannot create a different external reference with the same name, type, and version.";
    public static final String EXTERNAL_REFERENCE_UP_TO_DATE = "External reference is already up to date. No changes were made.";
    public static final String EXTERNAL_REFERENCE_CREATED_SUCCESS = "External reference created successfully.";
    public static final String EXTERNAL_REFERENCE_IMMUTABLE = "External reference with ID %s and version '%s' already exists and is immutable. Cannot modify name, type, or schema associations for an existing version. Create a new version if changes are needed.";
    public static final String EXTERNAL_REFERENCE_VERSION_IMMUTABLE = "External reference version '%s' for ID %s is immutable. Existing schemas: %s. Requested schemas: %s. Create a new version if different schema associations are needed.";
    public static final String EXTERNAL_REFERENCE_INVALID_TYPE = "Invalid ExtRefType: %s";

    // External reference validation messages
    public static final String EXTERNAL_REFERENCE_REQUEST_BODY_NULL = "Request body cannot be null";
    public static final String EXTERNAL_REFERENCE_ID_VERSION_REQUIRED = "Both extRefId and extRefVersion are required (NOT NULL)";

    // Field length validation messages
    public static final String VALIDATION_NAMESPACE_MAX_LENGTH = "Namespace must not exceed 32 characters";
    public static final String VALIDATION_TYPE_MAX_LENGTH = "Type must not exceed 64 characters";
    public static final String VALIDATION_EXT_REF_TYPE_MAX_LENGTH = "External reference type must not exceed 64 characters";
    public static final String VALIDATION_EXT_REF_ID_MAX_LENGTH = "External reference ID must not exceed 64 characters";
    public static final String VALIDATION_EXT_REF_NAME_MAX_LENGTH = "External reference name must not exceed 256 characters";
    public static final String VALIDATION_EXT_REF_VERSION_MAX_LENGTH = "External reference version must not exceed 64 characters";

    // Field required validation messages
    public static final String VALIDATION_SCHEMAS_FIELD_REQUIRED = "schemas field is required";
    public static final String VALIDATION_SCHM_ID_REQUIRED = "schmId is required for each schema reference";

    // HTTP status messages and error prefixes
    public static final String HTTP_BAD_REQUEST = "Bad Request";
    public static final String ERROR_PREFIX_BAD_REQUEST = "Bad Request: ";
    public static final String ERROR_PREFIX_CONFLICT = "Conflict: ";
    public static final String ERROR_PREFIX_NOT_FOUND = "Not Found: ";
    public static final String ERROR_PREFIX_ERROR = "Error: ";
    public static final String ERROR_PREFIX_INTERNAL_SERVER = "Internal Server Error: ";

    // Tenant related messages
    public static final String TENANT_NOT_FOUND = "Tenant not found";
    public static final String TENANT_ONBOARDING_SUCCESS = "Successfully created tenant";
    public static final String TENANT_ONBOARDING_PREPARING = "Preparing to create new tenant";
    public static final String TENANT_CONFIG_INVALID = "Invalid tenant configuration";
    public static final String TENANT_CONCURRENT_CREATION = "Tenant already exists (concurrent creation)";
    public static final String TENANT_CONSTRAINT_ERROR_LOG = "Database constraint violation while creating tenant";
    public static final String TENANT_NAME_UNAVAILABLE = "Tenant name is not available in JWT context";

    // Tenant filter messages
    public static final String TENANT_FILTER_DB_CONSTRAINT_ERROR = "Database constraint violation in tenant filter";
    public static final String TENANT_FILTER_DB_ACCESS_ERROR = "Database access error in tenant filter";
    public static final String TENANT_FILTER_CONSTRAINT_RESPONSE = "Tenant constraint violation";
    public static final String TENANT_FILTER_UNAVAILABLE_RESPONSE = "Service temporarily unavailable";

    // Namespace related messages
    public static final String NAMESPACE_NOT_FOUND = "Namespace not found";
    public static final String NAMESPACE_RACE_CONDITION_UNRESOLVED = "Namespace creation race condition unresolved. Please contact support.";
    public static final String NAMESPACE_CONSTRAINT_VIOLATION = "Namespace creation failed due to constraint violation. Please contact support.";
    public static final String NAMESPACE_CONCURRENT_CREATION = "Namespace already exists (concurrent creation)";
    public static final String NAMESPACE_CREATED = "Created new namespace";
    public static final String NAMESPACE_CONSTRAINT_ERROR_LOG = "Database constraint violation while creating namespace";

    // Database constraint names
    public static final String DB_CONSTRAINT_UNIQUE_EXT_REF = "uk_ext_ref_tenant_id_name_type_version";
    public static final String DB_CONSTRAINT_KEYWORD_UNIQUE = "unique";
    public static final String DB_CONSTRAINT_KEYWORD_EXT_REF = "ext_ref";

    // Generic messages
    public static final String GENERIC_ANOTHER_RECORD = "another record";

    // Pagination validation messages
    public static final String INVALID_PAGINATION_OFFSET = "Invalid pagination offset: %d. Must be between 0 and %d";
}