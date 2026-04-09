package com.ssnc.schemaService.tenant;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.schemaService.service.NameSpaceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NamespaceFilterManager {

    @PersistenceContext
    private EntityManager entityManager;

    private final TenantRepository tenantRepository;
    private final NameSpaceService nameSpaceService;

    @Autowired
    public NamespaceFilterManager(
            @Lazy TenantRepository tenantRepository,
            @Lazy NameSpaceService nameSpaceService) {
        this.tenantRepository = tenantRepository;
        this.nameSpaceService = nameSpaceService;
    }

    public void enableIfPresent(String ns) {
        if (ns != null) {
            // Get tenant ID from context
            String tenantName = TenantContext.getTenantName();
            UUID tenantId = resolveTenantId(tenantName);

            // Ensure namespace exists and get its ID (caching handled by service)
            UUID nmspcId = nameSpaceService.ensureNamespaceExists(tenantId, ns);

            // Enable filter with UUID directly
            entityManager.unwrap(Session.class)
                         .enableFilter(AppConstants.FILTER_NAMESPACE)
                         .setParameter(AppConstants.FILTER_PARAM_NAMESPACE_ID, nmspcId);
        }
    }

    private UUID resolveTenantId(String tenantName) {
        return tenantRepository.findByTenantName(tenantName)
                .map(com.ssnc.schemaService.entity.Tenant::getTenantId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.TENANT_NOT_FOUND));
    }
}
