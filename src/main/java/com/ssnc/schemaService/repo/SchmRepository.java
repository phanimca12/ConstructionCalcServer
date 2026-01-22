package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SchmRepository extends JpaRepository<Schm, UUID>, JpaSpecificationExecutor<Schm> {
    @Query("""
        SELECT d
        FROM Schm s
        JOIN SchmData d
          ON d.id.schmId = s.schmId
         AND d.id.schmVersion = s.publishVersion
        WHERE s.schmId = :schmId
    """)
    List<SchmData> getPublishedSchema(@Param("schmId") UUID schmId);

    Schm getByschmId(UUID uuid);
}
