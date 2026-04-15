package com.ssnc.schemaService.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CacheConfigPropertiesTest {

    @Test
    void testDefaultValues() {
        CacheConfigProperties config = new CacheConfigProperties();

        assertNotNull(config.getTenant());
        assertNotNull(config.getNamespace());

        assertEquals(60, config.getTenant().getExpireAfterWriteMinutes());
        assertEquals(1000, config.getTenant().getMaximumSize());

        assertEquals(60, config.getNamespace().getExpireAfterWriteMinutes());
        assertEquals(1000, config.getNamespace().getMaximumSize());
    }

    @Test
    void testSetTenantCacheConfig() {
        CacheConfigProperties config = new CacheConfigProperties();
        CacheConfigProperties.TenantCacheConfig tenantConfig = new CacheConfigProperties.TenantCacheConfig();
        tenantConfig.setExpireAfterWriteMinutes(120);
        tenantConfig.setMaximumSize(2000);

        config.setTenant(tenantConfig);

        assertEquals(120, config.getTenant().getExpireAfterWriteMinutes());
        assertEquals(2000, config.getTenant().getMaximumSize());
    }

    @Test
    void testSetNamespaceCacheConfig() {
        CacheConfigProperties config = new CacheConfigProperties();
        CacheConfigProperties.NamespaceCacheConfig namespaceConfig = new CacheConfigProperties.NamespaceCacheConfig();
        namespaceConfig.setExpireAfterWriteMinutes(90);
        namespaceConfig.setMaximumSize(1500);

        config.setNamespace(namespaceConfig);

        assertEquals(90, config.getNamespace().getExpireAfterWriteMinutes());
        assertEquals(1500, config.getNamespace().getMaximumSize());
    }

    @Test
    void testTenantCacheConfigSettersAndGetters() {
        CacheConfigProperties.TenantCacheConfig config = new CacheConfigProperties.TenantCacheConfig();

        config.setExpireAfterWriteMinutes(180);
        config.setMaximumSize(3000);

        assertEquals(180, config.getExpireAfterWriteMinutes());
        assertEquals(3000, config.getMaximumSize());
    }

    @Test
    void testNamespaceCacheConfigSettersAndGetters() {
        CacheConfigProperties.NamespaceCacheConfig config = new CacheConfigProperties.NamespaceCacheConfig();

        config.setExpireAfterWriteMinutes(45);
        config.setMaximumSize(500);

        assertEquals(45, config.getExpireAfterWriteMinutes());
        assertEquals(500, config.getMaximumSize());
    }

    @Test
    void testTenantCacheConfigEquality() {
        CacheConfigProperties.TenantCacheConfig config1 = new CacheConfigProperties.TenantCacheConfig();
        config1.setExpireAfterWriteMinutes(60);
        config1.setMaximumSize(1000);

        CacheConfigProperties.TenantCacheConfig config2 = new CacheConfigProperties.TenantCacheConfig();
        config2.setExpireAfterWriteMinutes(60);
        config2.setMaximumSize(1000);

        assertEquals(config1, config2);
    }

    @Test
    void testNamespaceCacheConfigEquality() {
        CacheConfigProperties.NamespaceCacheConfig config1 = new CacheConfigProperties.NamespaceCacheConfig();
        config1.setExpireAfterWriteMinutes(60);
        config1.setMaximumSize(1000);

        CacheConfigProperties.NamespaceCacheConfig config2 = new CacheConfigProperties.NamespaceCacheConfig();
        config2.setExpireAfterWriteMinutes(60);
        config2.setMaximumSize(1000);

        assertEquals(config1, config2);
    }

    @Test
    void testTenantCacheConfigHashCode() {
        CacheConfigProperties.TenantCacheConfig config1 = new CacheConfigProperties.TenantCacheConfig();
        config1.setExpireAfterWriteMinutes(60);
        config1.setMaximumSize(1000);

        CacheConfigProperties.TenantCacheConfig config2 = new CacheConfigProperties.TenantCacheConfig();
        config2.setExpireAfterWriteMinutes(60);
        config2.setMaximumSize(1000);

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testNamespaceCacheConfigHashCode() {
        CacheConfigProperties.NamespaceCacheConfig config1 = new CacheConfigProperties.NamespaceCacheConfig();
        config1.setExpireAfterWriteMinutes(60);
        config1.setMaximumSize(1000);

        CacheConfigProperties.NamespaceCacheConfig config2 = new CacheConfigProperties.NamespaceCacheConfig();
        config2.setExpireAfterWriteMinutes(60);
        config2.setMaximumSize(1000);

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testTenantCacheConfigToString() {
        CacheConfigProperties.TenantCacheConfig config = new CacheConfigProperties.TenantCacheConfig();
        config.setExpireAfterWriteMinutes(60);
        config.setMaximumSize(1000);

        String toString = config.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("60"));
        assertTrue(toString.contains("1000"));
    }

    @Test
    void testNamespaceCacheConfigToString() {
        CacheConfigProperties.NamespaceCacheConfig config = new CacheConfigProperties.NamespaceCacheConfig();
        config.setExpireAfterWriteMinutes(60);
        config.setMaximumSize(1000);

        String toString = config.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("60"));
        assertTrue(toString.contains("1000"));
    }
}
