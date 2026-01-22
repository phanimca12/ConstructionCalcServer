package com.ssnc.schemaService.service;

import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    @Autowired
    TenantRepository tenantRepository;

    public Tenant createTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public Tenant getTenantByName(String name) {
        return tenantRepository.findByTenantName(name)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }
}
