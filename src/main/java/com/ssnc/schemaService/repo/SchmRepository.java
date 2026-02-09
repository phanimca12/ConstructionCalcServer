package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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



    @Query("""
        SELECT d
        FROM Schm s
        JOIN SchmData d 
          ON d.id.schmId = s.schmId
         AND d.id.schmVersion = :schmVersion
        WHERE s.schmId = :schmId
    """)
    List<SchmData> getSchemaByVersion(@Param("schmId") UUID schmId, @Param("schmVersion") int schmVersion);


    @Query("""
        SELECT d
        FROM Schm s
        JOIN SchmData d 
          ON d.id.schmId = s.schmId
              WHERE s.schmId = :schmId
               AND d.id.schmVersion = (
                      SELECT MAX(d2.id.schmVersion)
                      FROM SchmData d2
                      WHERE d2.id.schmId = :schmId
                  )
    """)
    List<SchmData> getSchemaByLatestVersion(@Param("schmId") UUID schmId);


    @Transactional
    @Modifying
    @Query("""
    DELETE FROM SchmData d
    WHERE d.id.schmId = :schmId
      AND d.id.schmVersion = :schmVersion
""")
    void deleteBySchmIdAndSchmVersion(@Param("schmId") UUID schmId,
                                      @Param("schmVersion") int schmVersion);

    Schm getByschmId(UUID uuid);
}
