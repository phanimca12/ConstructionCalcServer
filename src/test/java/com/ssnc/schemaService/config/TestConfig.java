package com.ssnc.schemaService.config;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public JwtClaimsContext jwtClaimsContext() {
        JwtClaimsContext mockContext = mock(JwtClaimsContext.class);
        when(mockContext.getUserId()).thenReturn("testUser");
        return mockContext;
    }

    @PostConstruct
    public void setupTenant() {
        TenantContext.setTenantName(AppConstants.DEFAULT_TENANT_ID);
    }
}
