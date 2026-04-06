package com.ssnc.schemaService.constants;

/**
 * API endpoint and path variable constants
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    /**
     * Base path mappings - External References
     */
    public static final String PATH_EXT_REF_BASE = "/extRef/{nameSpace}";
    public static final String PATH_EXT_REF_BY_TYPE_ID_VERSION = "/type/{extRefType}/id/{extRefId}/version/{extRefVersion}/schemas";
    public static final String PATH_EXT_REF_CREATE = "/type/{extRefType}/name/{extRefName}/id/{extRefId}/version/{extRefVersion}/schemas";

    /**
     * Base path mappings - Schemas
     */
    public static final String PATH_SCHEMAS_BASE = "/schemas/{nameSpace}";
    public static final String PATH_SCHEMA_BY_ID = "/{id}";
    public static final String PATH_SCHEMA_VERSION = "/{id}/version/{versionNumber}";
    public static final String PATH_SCHEMA_VERSION_PUBLISH = "/{id}/version/{versionNumber}/publish";
    public static final String PATH_SCHEMA_VERSION_UNPUBLISH = "/{id}/version/unpublish";
    public static final String PATH_SCHEMA_VERSION_PUBLISHED = "/{id}/version/published";
    public static final String PATH_SCHEMA_VERSION_DRAFT = "/{id}/version/draft";
    public static final String PATH_SCHEMA_VERSION_PUBLISHED_CONTENT = "/{id}/version/published/content";
    public static final String PATH_SCHEMA_VERSION_CONTENT = "/{id}/version/{versionNumber}/content";
    public static final String PATH_SCHEMA_VERSION_DRAFT_CONTENT = "/{id}/version/draft/content";
    public static final String PATH_SCHEMA_LOCK = "/{id}/lock";
    public static final String PATH_SCHEMA_UNLOCK = "/{id}/unLock";
    public static final String PATH_SCHEMA_EXT_REFS = "/{id}/extRefs";

    /**
     * Base path mappings - Tenants
     */
    public static final String PATH_TENANTS_BASE = "/tenants";
    public static final String PATH_TENANT_BY_NAME = "/{tenantName}";

    /**
     * Base path mappings - Namespaces
     */
    public static final String PATH_NAMESPACES_BASE = "/namespaces";
    public static final String PATH_NAMESPACE_BY_NAME = "/{namespace}";

    /**
     * Path variable names
     */
    public static final String PARAM_NAME_SPACE = "nameSpace";
    public static final String PARAM_EXT_REF_TYPE = "extRefType";
    public static final String PARAM_EXT_REF_ID = "extRefId";
    public static final String PARAM_EXT_REF_NAME = "extRefName";
    public static final String PARAM_EXT_REF_VERSION = "extRefVersion";
    public static final String PARAM_ID = "id";
    public static final String PARAM_VERSION_NUMBER = "versionNumber";
    public static final String PARAM_TENANT_NAME = "tenantName";
    public static final String PARAM_NAMESPACE = "namespace";

    /**
     * Query parameter names
     */
    public static final String QUERY_PARAM_TYPE = "type";
    public static final String QUERY_PARAM_SORT = "sort";
    public static final String QUERY_PARAM_VERSION_NAME = "versionName";
    public static final String QUERY_PARAM_SCHM_GROUP = "schmGroup";
    public static final String QUERY_PARAM_NAME = "name";
    public static final String QUERY_PARAM_GROUP = "group";
    public static final String QUERY_PARAM_MODIFIED_BY_USER = "modifiedByUser";
    public static final String QUERY_PARAM_VERSION_MODIFIED_BY_USER = "versionModifiedByUser";
    public static final String QUERY_PARAM_WITH_VERSION = "withVersion";
    public static final String QUERY_PARAM_VERSION_NUMBER = "versionNumber";

    /**
     * Request part names (multipart form data)
     */
    public static final String REQUEST_PART_SCHEMA = "schema";
    public static final String REQUEST_PART_CONTENT = "content";

    /**
     * Default values
     */
    public static final String DEFAULT_WITH_VERSION = "none";
}
