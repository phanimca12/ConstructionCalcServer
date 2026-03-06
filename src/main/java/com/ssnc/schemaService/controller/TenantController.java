package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    @Autowired
    TenantService tenantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tenant createTenant(@RequestBody Tenant request) {
        return tenantService.createTenant(request);
    }

    @GetMapping("/{tenantName}")
    public Tenant getTenantByName(@PathVariable String tenantName) {
        return tenantService.getTenantByName(tenantName);
    }

    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }
}
