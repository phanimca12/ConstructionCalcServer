package com.ssnc.schemaService.filter;

import com.ssnc.schemaService.service.TenantService;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTenantFilter extends OncePerRequestFilter {

    @Autowired
    JwtClaimsContext jwtClaimsContext;

    @Autowired
    TenantService tenantService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Get tenant from JWT context (call once for performance)
            String tenantName = (jwtClaimsContext != null) ? jwtClaimsContext.getTenant() : null;

            if (tenantName == null || tenantName.isEmpty()) {
                throw new IllegalStateException("Tenant name is not available in JWT context");
            }

            // Ensure tenant exists in DB, create if not
            // Delegates to TenantService for proper business logic and caching
            tenantService.ensureTenantExists(tenantName);

            TenantContext.setTenantName(tenantName);
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}
