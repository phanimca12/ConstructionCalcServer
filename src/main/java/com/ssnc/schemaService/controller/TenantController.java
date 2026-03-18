package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.service.TenantService;
import com.ssnc.shared.security.JwtClaimsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    @Autowired
    TenantService tenantService;
    @Autowired
    JwtClaimsContext jwtClaimsContext;

    @PostMapping
    public ResponseEntity<?> createTenant() throws Exception {
        try {
            logger.debug("Received request for tenant onboarding: [{}]", jwtClaimsContext != null ? jwtClaimsContext.getClients() : ErrorMessages.JWT_CONTEXT_NOT_FOUND);

            tenantService.createTenantsFromContext();

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            logger.error(ErrorMessages.ERROR_SYNCING_TENANTS, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorMessages.TENANT_CREATION_FAILED);
        }
    }

    @GetMapping("/{tenantName}")
    public TenantDto getTenantByName(@PathVariable String tenantName) {
        return tenantService.getTenantByName(tenantName);
    }

    @GetMapping
    public List<TenantDto> getAllTenants() {
        return tenantService.getAllTenants();
    }
}
