    create table tenant (
        tenant_id ${guid} not null,
        tenant_name varchar(256) not null,
        created_by varchar(256),
        updated_by varchar(256),
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        primary key (tenant_id)
    );

    create table nmspc (
        nmspc_id ${guid} not null,
        tenant_id ${guid} not null,
        nmspc_name varchar(32) not null,
        nmspc_desc varchar(4000),
        created_by varchar(256),
        updated_by varchar(256),
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        primary key (nmspc_id),
        foreign key (tenant_id) references tenant
    );

    -- Index for foreign key column
    create index idx_nmspc_tenant_id on nmspc(tenant_id);

    create table schm (
        schm_id ${guid} not null,
        tenant_id ${guid} not null,
        nmspc_id ${guid} not null,
        schm_name varchar(256),
        schm_desc varchar(4000),
        schm_type VARCHAR(64),
        content_type VARCHAR(128),
        schm_group varchar(256),
        publish_version integer,
        lock_by varchar(256),
        created_by varchar(256),
        updated_by varchar(256),
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        primary key (schm_id),
        foreign key (tenant_id) references tenant,
        foreign key (nmspc_id) references nmspc
    );

    -- Indexes for foreign key columns (improves JOIN and lookup performance)
    create index idx_schm_tenant_id on schm(tenant_id);
    create index idx_schm_nmspc_id on schm(nmspc_id);

    create table schm_data (
        schm_version integer not null,
        schm_id ${guid} not null,
        schm_version_name varchar(64),
        schm_data ${clob},
        is_draft char(1),
        created_by varchar(256),
        updated_by varchar(256),
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        primary key (schm_version, schm_id),
        foreign key (schm_id) references schm
    );

    -- Index for foreign key column (improves JOIN and lookup performance)
    create index idx_schm_data_schm_id on schm_data(schm_id);

    create table ext_ref (
        ext_ref_id varchar(64) not null,
        tenant_id ${guid} not null,
        ext_ref_name varchar(256),
        ext_ref_type integer not null,
        ext_ref_version varchar(64) not null,
        created_by varchar(256),
        updated_by varchar(256),
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        primary key (ext_ref_id, ext_ref_version),
        foreign key (tenant_id) references tenant
    );

    -- Indexes for performance optimization
    create index idx_ext_ref_tenant_id on ext_ref(tenant_id);
    create index idx_ext_ref_type on ext_ref(ext_ref_type);
    create index idx_ext_ref_type_id_version on ext_ref(ext_ref_type, ext_ref_id, ext_ref_version);

    -- UNIQUE CONSTRAINT: Prevent duplicate external references within a tenant
    -- Ensures (tenant_id, ext_ref_name, ext_ref_type, ext_ref_version) is unique
    -- Enforces business rule: same name + type + version cannot exist twice in a tenant
    -- Constraint name matches ErrorMessages.DB_CONSTRAINT_UNIQUE_EXT_REF constant
    create unique index uk_ext_ref_tenant_id_name_type_version on ext_ref(tenant_id, ext_ref_name, ext_ref_type, ext_ref_version);

    create table schm_ext_ref_xref (
        xref_id ${guid} not null,
        tenant_id ${guid} not null,
        schm_id ${guid} not null,
        ext_ref_id varchar(64) not null,
        ext_ref_version varchar(64) not null,
        created_by varchar(256),
        created_datetime ${timestamp},
        primary key (xref_id),
        foreign key (tenant_id) references tenant,
        foreign key (schm_id) references schm,
        foreign key (ext_ref_id, ext_ref_version) references ext_ref(ext_ref_id, ext_ref_version)
    );

    -- Indexes for foreign key columns (improves JOIN, lookup, and cascade delete performance)
    create index idx_xref_tenant_id on schm_ext_ref_xref(tenant_id);
    create index idx_xref_schm_id on schm_ext_ref_xref(schm_id);
    create index idx_xref_ext_ref_id_version on schm_ext_ref_xref(ext_ref_id, ext_ref_version);

