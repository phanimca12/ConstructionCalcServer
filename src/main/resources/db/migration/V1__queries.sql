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
        schm_id uuid not null,
        tenant_name varchar(256) not null,
        nmspc_name varchar(256) not null,
        created_by varchar(256),
        "group" varchar(256),
        lock_by varchar(256),
        schm_name varchar(256),
        updated_by varchar(256),
        schm_desc varchar(4000),
        primary key (schm_id),
        foreign key (nmspc_name) references nmspc,
        foreign key (tenant_name) references tenant
    );
    create table schm_data (
        schm_version integer not null,
        created_datetime ${timestamp},
        updated_datetime ${timestamp},
        SCHM_ID uuid not null,
        schm_version_name varchar(64),
        created_by varchar(256),
        updated_by varchar(256),
        schm_data ${clob},
        primary key (schm_version, SCHM_ID),
        foreign key (schm_id) references schm
    );


 
 