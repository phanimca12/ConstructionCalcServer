package com.ssnc.schemaService.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantId() != null ? TenantContext.getTenantId() :"client1";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
