package com.ssnc.schemaService.service;

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
    private final JwtClaimsContext jwtClaimsContext;

    public TenantService(TenantRepository tenantRepository, JwtClaimsContext jwtClaimsContext) {
        this.tenantRepository = tenantRepository;
        this.jwtClaimsContext = jwtClaimsContext;
    }

    @Transactional
    public List<Tenant> createTenantsFromContext() throws Exception {
        if (jwtClaimsContext != null && !jwtClaimsContext.isPopulated()) {
            String error = "JWT claims context is not populated. Cannot create tenants.";
            logger.error(error);
            throw new IllegalStateException(error);
        }

        List<String> clients = jwtClaimsContext.getClients();

        List<String> newClients = new ArrayList<>();
        clients.forEach(client -> {
            Optional<Tenant> existingTenant = tenantRepository.findByTenantName(client);
            if (existingTenant.isEmpty()) {
                newClients.add(client);
            }
        });

        if (newClients.isEmpty()) {
            logger.debug("All clients already onboarded: {}", clients);
            return new ArrayList<>();
        }

        List<Tenant> tenantsToCreate = new ArrayList<>();
        buildTenantEntityFromContext(tenantsToCreate, newClients);

        // Batch insert all new tenants at once
        if (!tenantsToCreate.isEmpty()) {
            tenantRepository.saveAll(tenantsToCreate);
            logger.info("Successfully created {} tenants", tenantsToCreate.size());
        }

        return tenantsToCreate;
    }

    private void buildTenantEntityFromContext(List<Tenant> tenantsToCreate, List<String> clients) {
        String userName = jwtClaimsContext.getUserId() != null ? jwtClaimsContext.getUserId() : "system";

        for (String clientName : clients) {
            logger.debug("Preparing to create new tenant for client: {}", clientName);

            Tenant newTenant = new Tenant();
            newTenant.setTenantName(clientName);
            newTenant.setCreatedBy(userName);
            newTenant.setUpdatedBy(userName);

            tenantsToCreate.add(newTenant);
        }
    }

    public TenantDto createTenant(TenantDto tenantDto) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantDto.getName());
        tenant.setCreatedBy(tenantDto.getCreatedByUser());
        tenant.setUpdatedBy(tenantDto.getModifiedByUser());

        Tenant savedTenant = tenantRepository.save(tenant);

        return mapToTenantDto(savedTenant);
    }

    public TenantDto getTenantByName(String name) {
        Tenant tenant = tenantRepository.findByTenantName(name)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
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
