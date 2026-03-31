    create table tenant (
        tenant_name varchar(256) not null,
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        created_by varchar(256),
        updated_by varchar(256),
        primary key (tenant_name)
    );

    create table nmspc (
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        nmspc_name varchar(32) not null,
        created_by varchar(256),
        updated_by varchar(256),
        nmspc_desc varchar(4000),
        primary key (nmspc_name)
    );
    create table schm (
        publish_version integer,
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        schm_id ${guid} not null,
        tenant_name varchar(256) not null,
        nmspc_name varchar(32) not null,
        created_by varchar(256),
        "group" varchar(256),
        lock_by varchar(256),
        schm_name varchar(256),
        updated_by varchar(256),
        schm_desc varchar(4000),
        schm_type VARCHAR(64),
        content_type VARCHAR(128),
        primary key (schm_id),
        foreign key (nmspc_name) references nmspc,
        foreign key (tenant_name) references tenant
    );

    -- Indexes for foreign key columns (improves JOIN and lookup performance)
    create index idx_schm_nmspc_name on schm(nmspc_name);
    create index idx_schm_tenant_name on schm(tenant_name);
    create table schm_data (
        schm_version integer not null,
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        SCHM_ID ${guid} not null,
        schm_version_name varchar(64),
        created_by varchar(256),
        updated_by varchar(256),
        IS_DRAFT char(1),
        schm_data ${clob},
        primary key (schm_version, SCHM_ID),
        foreign key (schm_id) references schm
    );

    -- Index for foreign key column (improves JOIN and lookup performance)
    create index idx_schm_data_schm_id on schm_data(schm_id);

    create table ext_ref (
        ext_ref_id ${guid} not null,
        tenant_name varchar(256) not null,
        ext_ref_name varchar(256),
        ext_ref_type varchar(64),
        ext_ref_version varchar(64),
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        created_by varchar(256),
        updated_by varchar(256),
        primary key (ext_ref_id),
        foreign key (tenant_name) references tenant,
        constraint uk_ext_ref_tenant_name_type_version unique (tenant_name, ext_ref_name, ext_ref_type, ext_ref_version)
    );

    -- Indexes for performance optimization
    create index idx_ext_ref_tenant_name on ext_ref(tenant_name);
    create index idx_ext_ref_type on ext_ref(ext_ref_type);
    create index idx_ext_ref_type_id_version on ext_ref(ext_ref_type, ext_ref_id, ext_ref_version);

    create table schm_ext_ref_xref (
        xref_id ${guid} not null,
        tenant_name varchar(256) not null,
        schm_id ${guid} not null,
        ext_ref_id ${guid} not null,
        created_datetime ${timestamp},
        created_by varchar(256),
        primary key (xref_id),
        foreign key (tenant_name) references tenant,
        foreign key (schm_id) references schm,
        foreign key (ext_ref_id) references ext_ref
    );

    -- Indexes for foreign key columns (improves JOIN, lookup, and cascade delete performance)
    create index idx_xref_tenant_name on schm_ext_ref_xref(tenant_name);
    create index idx_xref_schm_id on schm_ext_ref_xref(schm_id);
    create index idx_xref_ext_ref_id on schm_ext_ref_xref(ext_ref_id);

