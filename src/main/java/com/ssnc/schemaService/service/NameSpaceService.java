package com.ssnc.schemaService.service;

import com.ssnc.schemaService.dto.NameSpaceDto;
import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.repo.NameSpaceRepository;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NameSpaceService {

    @Autowired
    NameSpaceRepository nameSpaceRepository;

    @Autowired
    private JwtClaimsContext jwtClaimsContext;

    public NameSpaceDto createNameSpace(NameSpaceDto nameSpaceDto) {
        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : "system";

        Nmspc nmspc = new Nmspc();
        nmspc.setNmspcName(nameSpaceDto.getName());
        nmspc.setDescription(nameSpaceDto.getDescription());
        nmspc.setCreatedBy(userName);
        nmspc.setUpdatedBy(userName);

        Nmspc savedNmspc = nameSpaceRepository.save(nmspc);

        return mapToNameSpaceDto(savedNmspc);
    }

    private NameSpaceDto mapToNameSpaceDto(Nmspc nmspc) {
        NameSpaceDto dto = new NameSpaceDto();
        dto.setId(nmspc.getNmspcName());
        dto.setName(nmspc.getNmspcName());
        dto.setDescription(nmspc.getDescription());
        dto.setCreatedByUser(nmspc.getCreatedBy());
        dto.setCreateDateTime(nmspc.getCreatedDatetime());
        dto.setModifiedByUser(nmspc.getUpdatedBy());
        dto.setModifiedDateTime(nmspc.getUpdatedDatetime());
        return dto;
    }

    public NameSpaceDto getNameSpaceByName(String name) {
        Nmspc nmspc = nameSpaceRepository.findBynmspcName(name)
                .orElseThrow(() -> new EntityNotFoundException("Namespace not found"));
        return mapToNameSpaceDto(nmspc);
    }

    public List<NameSpaceDto> getAllNameSpaces() {
        return nameSpaceRepository.findAll().stream()
                .map(this::mapToNameSpaceDto)
                .toList();
    }
}
