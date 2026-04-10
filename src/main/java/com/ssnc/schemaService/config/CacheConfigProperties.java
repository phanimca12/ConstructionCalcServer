package com.ssnc.schemaService.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration properties loaded from application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "cache.caffeine")
@Data
public class CacheConfigProperties {

    private TenantCacheConfig tenant = new TenantCacheConfig();
    private NamespaceCacheConfig namespace = new NamespaceCacheConfig();

    @Data
    public static class TenantCacheConfig {
        private int expireAfterWriteMinutes = 60;
        private int maximumSize = 1000;
    }

    @Data
    public static class NamespaceCacheConfig {
        private int expireAfterWriteMinutes = 60;
        private int maximumSize = 1000;
    }
}
