package com.ssnc.schemaService.config;

import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import static org.junit.jupiter.api.Assertions.*;

class HibernateConfigTest {

    @Test
    void testJpaVendorAdapter() {
        HibernateConfig config = new HibernateConfig();
        JpaVendorAdapter adapter = config.jpaVendorAdapter();

        assertNotNull(adapter);
        assertTrue(adapter instanceof HibernateJpaVendorAdapter);
    }

    @Test
    void testJpaVendorAdapterReturnsNewInstance() {
        HibernateConfig config = new HibernateConfig();
        JpaVendorAdapter adapter1 = config.jpaVendorAdapter();
        JpaVendorAdapter adapter2 = config.jpaVendorAdapter();

        assertNotNull(adapter1);
        assertNotNull(adapter2);
        assertNotSame(adapter1, adapter2);
    }
}
