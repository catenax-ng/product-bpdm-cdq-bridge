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
