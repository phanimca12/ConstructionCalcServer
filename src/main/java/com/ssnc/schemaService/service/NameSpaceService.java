package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.NameSpaceDto;
import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.repo.NameSpaceRepository;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NameSpaceService {

    @Autowired
    NameSpaceRepository nameSpaceRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JwtClaimsContext jwtClaimsContext;

    public NameSpaceDto createNameSpace(NameSpaceDto nameSpaceDto) {
        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        String tenantName = TenantContext.getTenantName();

        // Resolve tenant_id from tenant_name
        UUID tenantId = tenantRepository.findByTenantName(tenantName)
                .map(com.ssnc.schemaService.entity.Tenant::getTenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Tenant not found: %s", tenantName)));

        Nmspc nmspc = new Nmspc();
        nmspc.setTenantId(tenantId);
        nmspc.setNmspcName(nameSpaceDto.getName());
        nmspc.setDescription(nameSpaceDto.getDescription());
        nmspc.setCreatedBy(userName);
        nmspc.setUpdatedBy(userName);

        Nmspc savedNmspc = nameSpaceRepository.save(nmspc);

        return mapToNameSpaceDto(savedNmspc);
    }

    private NameSpaceDto mapToNameSpaceDto(Nmspc nmspc) {
        NameSpaceDto dto = new NameSpaceDto();
        dto.setNmspcId(nmspc.getNmspcId());
        dto.setTenantId(nmspc.getTenantId());
        dto.setName(nmspc.getNmspcName());
        dto.setDescription(nmspc.getDescription());
        dto.setCreatedByUser(nmspc.getCreatedBy());
        dto.setCreateDateTime(nmspc.getCreatedDatetime());
        dto.setModifiedByUser(nmspc.getUpdatedBy());
        dto.setModifiedDateTime(nmspc.getUpdatedDatetime());
        return dto;
    }

    public NameSpaceDto getNameSpaceByName(String name) {
        Nmspc nmspc = nameSpaceRepository.findByNmspcName(name)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.NAMESPACE_NOT_FOUND));
        return mapToNameSpaceDto(nmspc);
    }

    public List<NameSpaceDto> getAllNameSpaces() {
        return nameSpaceRepository.findAll().stream()
                .map(this::mapToNameSpaceDto)
                .toList();
    }
}
