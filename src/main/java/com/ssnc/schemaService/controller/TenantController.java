package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ApiConstants;
import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.PATH_TENANTS_BASE)
public class TenantController {

    @Autowired
    TenantService tenantService;

    @GetMapping(ApiConstants.PATH_TENANT_BY_NAME)
    public TenantDto getTenantByName(@PathVariable(ApiConstants.PARAM_TENANT_NAME) String tenantName) {
        return tenantService.getTenantByName(tenantName);
    }

    @GetMapping
    public List<TenantDto> getAllTenants() {
        return tenantService.getAllTenants();
    }
}
