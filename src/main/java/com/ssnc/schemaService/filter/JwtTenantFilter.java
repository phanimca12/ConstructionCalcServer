package com.ssnc.schemaService.filter;

import com.ssnc.schemaService.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTenantFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            //Authentication auth = SecurityContextHolder.getContext().getAuthentication();

           /* if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                String tenantId = jwt.getClaimAsString("tenant_id");
                TenantContext.setTenantId(tenantId);
            }*/
            TenantContext.setTenantName("client1Id");
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}
