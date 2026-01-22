package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.SchmData;
import com.ssnc.schemaService.entity.SchmDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface SchmDataRepository extends JpaRepository<SchmData, SchmDataId> {
    Optional<SchmData> findTopByIdSchmIdOrderByIdSchmVersionDesc(UUID schemaId);
}
