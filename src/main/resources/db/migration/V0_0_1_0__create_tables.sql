CREATE SEQUENCE IF NOT EXISTS bpdm_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE import_entries
(
    id         BIGINT                      NOT NULL,
    uuid       UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    bpn        VARCHAR(255)                NOT NULL,
    import_id  varchar(255)                NOT NULL,
    PRIMARY KEY (id)
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
    started_at     TIMESTAMP WITH TIME ZONE,
    finished_at    TIMESTAMP WITH TIME ZONE,
    from_time      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT '1970-01-01 08:00:00',

    CONSTRAINT pk_sync_records PRIMARY KEY (id),
    CONSTRAINT uc_sync_records_type UNIQUE (type),
    CONSTRAINT uc_sync_records_uuid UNIQUE (uuid)
);


ALTER TABLE sync_records
    ALTER COLUMN from_time TYPE TIMESTAMP WITH TIME ZONE USING from_time AT TIME ZONE 'UTC';
