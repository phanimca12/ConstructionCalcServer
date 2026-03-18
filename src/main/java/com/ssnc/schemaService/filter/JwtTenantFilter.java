package com.ssnc.schemaService.filter;

import com.ssnc.schemaService.constants.AppConstants;
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

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String tenantName = (jwtClaimsContext!=null && jwtClaimsContext.getTenant() != null)
                    ? jwtClaimsContext.getTenant()
                    : AppConstants.DEFAULT_TENANT_ID;
            TenantContext.setTenantName(tenantName);
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}
