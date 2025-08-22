CREATE TABLE bear_binding
(
    id                      VARCHAR(26)                 NOT NULL,
    vhost                   VARCHAR(128)                NOT NULL,
    source_exchange_id      VARCHAR(26)                 NOT NULL,
    destination_type        VARCHAR(16)                 NOT NULL,
    destination_queue_id    VARCHAR(26),
    destination_exchange_id VARCHAR(26),
    routing_key             VARCHAR(255),
    arguments               JSON,
    status                  VARCHAR(16)                 NOT NULL,
    version                 BIGINT                      NOT NULL,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT "pk_bear_bındıng" PRIMARY KEY (id)
);

CREATE TABLE bear_exchange
(
    id          VARCHAR(26)                   NOT NULL,
    vhost       VARCHAR(128)                  NOT NULL,
    name        VARCHAR(255)                  NOT NULL,
    actual_name VARCHAR(255)                  NOT NULL,
    type        VARCHAR(16)                   NOT NULL,
    durable     BOOLEAN      DEFAULT TRUE     NOT NULL,
    auto_delete BOOLEAN      DEFAULT FALSE    NOT NULL,
    internal    BOOLEAN      DEFAULT FALSE    NOT NULL,
    delayed     BOOLEAN      DEFAULT FALSE    NOT NULL,
    arguments   JSON,
    status      VARCHAR(255) DEFAULT 'ACTIVE' NOT NULL,
    version     BIGINT       DEFAULT 0        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
    CONSTRAINT pk_bear_exchange PRIMARY KEY (id)
);

CREATE TABLE bear_queue
(
    id                   VARCHAR(26)                  NOT NULL,
    name                 VARCHAR(26)                  NOT NULL,
    actual_name          VARCHAR(255)                 NOT NULL,
    vhost                VARCHAR(255)                 NOT NULL,
    durable              BOOLEAN      DEFAULT TRUE    NOT NULL,
    exclusive            BOOLEAN      DEFAULT FALSE   NOT NULL,
    auto_delete          BOOLEAN      DEFAULT FALSE   NOT NULL,
    arguments            JSON,
    status               VARCHAR(255),
    overflow_policy      VARCHAR(255) DEFAULT 'BLOCK' NOT NULL,
    max_bytes            BIGINT       DEFAULT 4096    NOT NULL,
    max_message_count    BIGINT                       NOT NULL,
    message_ttl_ms       BIGINT,
    message_retention_ms BIGINT,
    created_at           TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    CONSTRAINT pk_bear_queue PRIMARY KEY (id)
);

CREATE TABLE tenant
(
    id         VARCHAR(26)                 NOT NULL,
    full_name  VARCHAR(255),
    username   VARCHAR(150)                NOT NULL,
    email      VARCHAR(150)                NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_tenant PRIMARY KEY (id)
);

ALTER TABLE bear_queue
    ADD CONSTRAINT uc_bear_queue_actual_name UNIQUE (actual_name);

ALTER TABLE bear_queue
    ADD CONSTRAINT uc_bear_queue_vhost UNIQUE (vhost);

ALTER TABLE tenant
    ADD CONSTRAINT "uc_tenant_emaıl" UNIQUE (email);

ALTER TABLE tenant
    ADD CONSTRAINT uc_tenant_username UNIQUE (username);

CREATE INDEX ix_bind_dst_ex ON bear_binding (destination_exchange_id);

CREATE INDEX ix_bind_dst_q ON bear_binding (destination_queue_id);

CREATE INDEX ix_bind_vhost_src ON bear_binding (vhost, source_exchange_id);