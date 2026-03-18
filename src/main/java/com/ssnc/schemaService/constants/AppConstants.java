package com.ssnc.schemaService.constants;

/**
 * Application-wide constants
 */
public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    /**
     * Default user constants
     */
    public static final String SYSTEM_USER = "system";

    /**
     * Default tenant constants
     */
    public static final String DEFAULT_TENANT_ID = "client1Id";

    /**
     * Boolean to character conversion constants
     */
    public static final String BOOLEAN_YES = "Y";
    public static final String BOOLEAN_NO = "N";

    /**
     * Version name constants
     */
    public static final String VERSION_NAME_PUBLISHED = "published";
    public static final String VERSION_NAME_LATEST = "latest";
    public static final String VERSION_NAME_DRAFT = "draft";

    /**
     * Schema type constants
     */
    public static final String SCHEMA_TYPE_FORM_DATA = "FormData";
}