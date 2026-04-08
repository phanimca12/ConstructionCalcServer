package com.ssnc.schemaService.tenant;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.schemaService.service.NameSpaceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class NamespaceFilterManager {

    @PersistenceContext
    private EntityManager entityManager;

    private final TenantRepository tenantRepository;
    private final NameSpaceService nameSpaceService;

    // Cache namespace existence to avoid DB queries on every request
    // Key format: "tenantId:namespaceName" for tenant-aware caching
    private final Cache<String, UUID> namespaceIdCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

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

            // Ensure namespace exists and get its ID
            UUID nmspcId = ensureNamespaceExists(tenantId, ns);

            // Enable filter with UUID directly
            entityManager.unwrap(Session.class)
                         .enableFilter("namespaceFilter")
                         .setParameter("namespaceId", nmspcId);
        }
    }

    private UUID resolveTenantId(String tenantName) {
        return tenantRepository.findByTenantName(tenantName)
                .map(com.ssnc.schemaService.entity.Tenant::getTenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Tenant not found: %s", tenantName)));
    }

    private UUID ensureNamespaceExists(UUID tenantId, String namespace) {
        String cacheKey = tenantId + ":" + namespace;

        // Check cache first to avoid DB query on every request
        UUID cachedId = namespaceIdCache.getIfPresent(cacheKey);
        if (cachedId != null) {
            return cachedId;
        }

        // Cache miss - delegate to service for DB operations and creation
        UUID nmspcId = nameSpaceService.ensureNamespaceExists(tenantId, namespace);

        // Cache the namespace ID after successful operation
        namespaceIdCache.put(cacheKey, nmspcId);
        return nmspcId;
    }
}
