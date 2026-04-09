package com.ssnc.schemaService.filter;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.service.TenantService;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTenantFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTenantFilter.class);

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
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, ErrorMessages.TENANT_NAME_UNAVAILABLE);
                return;
            }

            // Ensure tenant exists in DB, create if not
            // Delegates to TenantService for proper business logic and caching
            tenantService.ensureTenantExists(tenantName);

            TenantContext.setTenantName(tenantName);
            filterChain.doFilter(request, response);

        } catch (DataIntegrityViolationException e) {
            logger.error(ErrorMessages.TENANT_FILTER_DB_CONSTRAINT_ERROR, e);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.TENANT_FILTER_CONSTRAINT_RESPONSE);
        } catch (DataAccessException e) {
            logger.error(ErrorMessages.TENANT_FILTER_DB_ACCESS_ERROR, e);
            sendErrorResponse(response, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessages.TENANT_FILTER_UNAVAILABLE_RESPONSE);
        } finally {
            TenantContext.clear();
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(status.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String jsonError = String.format("{\"error\":\"%s\",\"status\":%d}",
                    escapeJson(message), status.value());
            response.getWriter().write(jsonError);
        }
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
