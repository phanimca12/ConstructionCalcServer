package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Autowired
    TenantRepository tenantRepository;
    @Autowired
    JwtClaimsContext jwtClaimsContext;

    @Transactional
    public List<Tenant> createTenantsFromContext() throws Exception {
        if (jwtClaimsContext != null && !jwtClaimsContext.isPopulated()) {
            logger.error(ErrorMessages.JWT_CONTEXT_NOT_POPULATED);
            throw new IllegalStateException(ErrorMessages.JWT_CONTEXT_NOT_POPULATED);
        }

        String tenantName = jwtClaimsContext.getTenant();

        if (tenantName == null || tenantName.isEmpty()) {
            logger.debug(ErrorMessages.NO_TENANT_IN_JWT);
            return new ArrayList<>();
        }

        // Check if tenant already exists
        Optional<Tenant> existingTenant = tenantRepository.findByTenantName(tenantName);
        if (existingTenant.isPresent()) {
            logger.debug(ErrorMessages.TENANT_ALREADY_ONBOARDED, tenantName);
            return new ArrayList<>();
        }

        // Create new tenant
        String userName = jwtClaimsContext.getUserId() != null ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        logger.debug(ErrorMessages.TENANT_ONBOARDING_PREPARING, tenantName);

        Tenant newTenant = new Tenant();
        newTenant.setTenantName(tenantName);
        newTenant.setCreatedBy(userName);
        newTenant.setUpdatedBy(userName);

        Tenant savedTenant = tenantRepository.save(newTenant);
        logger.info(ErrorMessages.TENANT_ONBOARDING_SUCCESS, tenantName);

        return List.of(savedTenant);
    }

    public TenantDto createTenant(TenantDto tenantDto) {
        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantDto.getName());
        tenant.setCreatedBy(userName);
        tenant.setUpdatedBy(userName);

        Tenant savedTenant = tenantRepository.save(tenant);

        return mapToTenantDto(savedTenant);
    }

    public TenantDto getTenantByName(String name) {
        Tenant tenant = tenantRepository.findByTenantName(name)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.TENANT_NOT_FOUND));
        return mapToTenantDto(tenant);
    }

    public List<TenantDto> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToTenantDto)
                .toList();
    }

    private TenantDto mapToTenantDto(Tenant tenant) {
        TenantDto dto = new TenantDto();
        dto.setName(tenant.getTenantName());
        dto.setCreatedByUser(tenant.getCreatedBy());
        dto.setCreateDateTime(tenant.getCreatedDatetime());
        dto.setModifiedByUser(tenant.getUpdatedBy());
        dto.setModifiedDateTime(tenant.getUpdatedDatetime());
        return dto;
    }
}
