package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SchmSpecificationsTest {

    @Mock
    private Root<Schm> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Path<Object> objectPath;

    @Mock
    private Expression<String> stringExpression;

    @Mock
    private Join<Object, Object> schmDataJoin;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(root.get(anyString())).thenReturn(objectPath);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void testWithFilters_EmptyCriteria() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void testWithFilters_WithSchmId() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        UUID schmId = UUID.randomUUID();
        criteria.setSchmId(schmId);

        when(cb.equal(any(), any())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("schmId");
        verify(cb).equal(objectPath, schmId);
    }

    @Test
    void testWithFilters_WithName() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setName("TestSchema");

        when(cb.lower(any())).thenReturn(stringExpression);
        when(cb.like(any(), anyString())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("schmName");
        verify(cb).lower(any());
        verify(cb).like(any(), eq("%testschema%"));
    }

    @Test
    void testWithFilters_WithSchemaType() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setSchemaType("JSON");

        when(cb.lower(any())).thenReturn(stringExpression);
        when(cb.equal(any(), anyString())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("schemaType");
        verify(cb).lower(any());
        verify(cb).equal(any(), eq("json"));
    }

    @Test
    void testWithFilters_WithGroup() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setGroup("TestGroup");

        when(cb.lower(any())).thenReturn(stringExpression);
        when(cb.equal(any(), anyString())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("schmGroup");
        verify(cb).lower(any());
        verify(cb).equal(any(), eq("testgroup"));
    }

    @Test
    void testWithFilters_WithModifiedByUser() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setModifiedByUser("TestUser");

        when(cb.lower(any())).thenReturn(stringExpression);
        when(cb.like(any(), anyString())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("updatedBy");
        verify(cb).lower(any());
        verify(cb).like(any(), eq("%testuser%"));
    }

    @Test
    void testWithFilters_WithVersionModifiedByUser() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setVersionModifiedByUser("VersionUser");

        when(root.join("versions")).thenReturn(schmDataJoin);
        when(schmDataJoin.get(anyString())).thenReturn(objectPath);
        when(cb.lower(any())).thenReturn(stringExpression);
        when(cb.like(any(), anyString())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).join("versions");
        verify(schmDataJoin).get("updatedBy");
        verify(cb).lower(any());
        verify(cb).like(any(), eq("%versionuser%"));
        verify(query).distinct(true);
    }

    @Test
    void testWithFilters_WithLockBy() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setLockBy("LockUser");

        when(cb.lower(any())).thenReturn(stringExpression);
        when(cb.like(any(), anyString())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("lockBy");
        verify(cb).lower(any());
        verify(cb).like(any(), eq("%lockuser%"));
    }

    @Test
    void testWithFilters_WithPublishVersion() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setPublishVersion(2);

        when(cb.equal(any(), any())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("publishVersion");
        verify(cb).equal(objectPath, 2);
    }

    @Test
    void testWithFilters_MultipleFilters() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setName("TestSchema");
        criteria.setSchemaType("JSON");
        criteria.setGroup("TestGroup");

        when(cb.lower(any())).thenReturn(stringExpression);
        when(cb.like(any(), anyString())).thenReturn(predicate);
        when(cb.equal(any(), anyString())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("schmName");
        verify(root).get("schemaType");
        verify(root).get("schmGroup");
        verify(cb, atLeast(3)).lower(any());
    }

    @Test
    void testWithFilters_AllFilters() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setSchmId(UUID.randomUUID());
        criteria.setName("TestSchema");
        criteria.setSchemaType("JSON");
        criteria.setGroup("TestGroup");
        criteria.setModifiedByUser("ModUser");
        criteria.setVersionModifiedByUser("VerUser");
        criteria.setLockBy("LockUser");
        criteria.setPublishVersion(1);

        when(root.join("versions")).thenReturn(schmDataJoin);
        when(schmDataJoin.get(anyString())).thenReturn(objectPath);
        when(cb.lower(any())).thenReturn(stringExpression);
        when(cb.like(any(), anyString())).thenReturn(predicate);
        when(cb.equal(any(), any())).thenReturn(predicate);

        Specification<Schm> spec = SchmSpecifications.withFilters(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("schmId");
        verify(root).get("schmName");
        verify(root).get("schemaType");
        verify(root).get("schmGroup");
        verify(root).get("updatedBy");
        verify(root).get("lockBy");
        verify(root).get("publishVersion");
        verify(root).join("versions");
        verify(query).distinct(true);
    }

    @Test
    void testConstructorIsPrivate() throws Exception {
        // Verify that the constructor is private
        var constructor = SchmSpecifications.class.getDeclaredConstructor();
        assertFalse(constructor.canAccess(null));
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
    }
}
