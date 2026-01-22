package com.ssnc.schemaService.tenant;

public class TenantContext {

    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    public static void setTenantName(String tenantId) {
        TENANT.set(tenantId);
    }

    public static String getTenantName() {
        return TENANT.get();
    }

    public static void clear() {
        TENANT.remove();
    }
}
