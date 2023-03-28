CREATE SEQUENCE IF NOT EXISTS bpdm_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE import_entries
(
    id         BIGINT                      NOT NULL,
    uuid       UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    bpn        VARCHAR(255)                NOT NULL,
    import_id  varchar(255)                NOT NULL
);

CREATE TABLE sync_records
(
    id             BIGINT                      NOT NULL,
    uuid           UUID                        NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type           VARCHAR(255)                NOT NULL,
    status         VARCHAR(255)                NOT NULL,
    progress       FLOAT                       NOT NULL,
    count          INTEGER                     NOT NULL,
    status_details VARCHAR(255),
    save_state     VARCHAR(255),
    started_at     TIMESTAMP with time zone,
    finished_at    TIMESTAMP with time zone,
    CONSTRAINT pk_sync_records PRIMARY KEY (id)
);

ALTER TABLE sync_records
    ADD CONSTRAINT uc_sync_records_type UNIQUE (type);

ALTER TABLE sync_records
    ADD CONSTRAINT uc_sync_records_uuid UNIQUE (uuid);

Alter table sync_records
    add column from_time timestamp without time zone not null default '1970-01-01 08:00:00';

Alter table sync_records
    alter column from_time type timestamp with time zone using from_time at time zone 'UTC';