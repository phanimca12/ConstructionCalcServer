package com.ssnc.schemaService.tenant;

import com.ssnc.shared.security.JwtClaimsContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String> {
    @Autowired
    JwtClaimsContext jwtClaimsContext;

        private static final String DEFAULT_TENANT = "public";


    @Override
    public String resolveCurrentTenantIdentifier() {
        return jwtClaimsContext.getTenant() != null ? jwtClaimsContext.getTenant() :"client1Id";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
