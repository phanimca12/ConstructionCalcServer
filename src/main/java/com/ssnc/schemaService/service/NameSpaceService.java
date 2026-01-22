package com.ssnc.schemaService.service;

import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.repo.NameSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NameSpaceService {

    @Autowired
    NameSpaceRepository nameSpaceRepository;

    public Nmspc createNameSpace(Nmspc nmspc) {
        return nameSpaceRepository.save(nmspc);
    }

    public Nmspc getNameSpaceByName(String name) {
        return nameSpaceRepository.findBynmspcName(name)
                .orElseThrow(() -> new EntityNotFoundException("Namespace not found"));
    }

    public List<Nmspc> getAllNameSpaces() {
        return nameSpaceRepository.findAll();
    }
}
