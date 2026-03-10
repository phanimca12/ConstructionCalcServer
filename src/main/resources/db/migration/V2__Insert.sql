-- Insert statements
INSERT INTO tenant (tenant_name, created_datetime, updated_datetime, created_by, updated_by)
VALUES ('client1Id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

INSERT INTO nmspc (nmspc_name, nmspc_desc, created_datetime, updated_datetime, created_by, updated_by)
VALUES ('data_object', 'Data Object Namespace', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

INSERT INTO nmspc (nmspc_name, nmspc_desc, created_datetime, updated_datetime, created_by, updated_by)
VALUES ('gateway', 'Gateway Namespace', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

