package com.ssnc.schemaService.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantName() != null ? TenantContext.getTenantName() :"CLIENT1ID";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
