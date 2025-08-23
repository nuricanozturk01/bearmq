CREATE TABLE binding
(
    id                      VARCHAR(26)                 NOT NULL,
    source_exchange_id      VARCHAR(26)                 NOT NULL,
    destination_queue_id    VARCHAR(26),
    destination_exchange_id VARCHAR(26),
    destination_type        VARCHAR(16)                 NOT NULL,
    routing_key             VARCHAR(255),
    arguments               JSON,
    status                  VARCHAR(16)                 NOT NULL,
    version                 BIGINT                      NOT NULL,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted                 BOOLEAN                     NOT NULL,
    vhost_id                VARCHAR(26),
    CONSTRAINT "pk_bındıng" PRIMARY KEY (id)
);

CREATE TABLE exchange
(
    id          VARCHAR(26)                   NOT NULL,
    vhost_id    VARCHAR(26)                   NOT NULL,
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
    deleted     BOOLEAN                       NOT NULL,
    CONSTRAINT pk_exchange PRIMARY KEY (id)
);

CREATE TABLE queue
(
    id                   VARCHAR(26)                  NOT NULL,
    name                 VARCHAR(26)                  NOT NULL,
    actual_name          VARCHAR(255)                 NOT NULL,
    vhost_id             VARCHAR(26)                  NOT NULL,
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
    deleted              BOOLEAN                      NOT NULL,
    CONSTRAINT pk_queue PRIMARY KEY (id)
);

CREATE TABLE subscription_plan
(
    name         VARCHAR(255) NOT NULL,
    max_vhosts   INTEGER      NOT NULL,
    max_queues   INTEGER      NOT NULL,
    max_exchange INTEGER      NOT NULL,
    deleted      BOOLEAN      NOT NULL,
    CONSTRAINT "pk_subscrıptıon_plan" PRIMARY KEY (name)
);

CREATE TABLE tenant
(
    id                   VARCHAR(26)                   NOT NULL,
    full_name            VARCHAR(255),
    password             VARCHAR(255)                  NOT NULL,
    username             VARCHAR(150)                  NOT NULL,
    salt                 VARCHAR(16)                   NOT NULL,
    email                VARCHAR(150)                  NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
    subscription_plan_id VARCHAR(255)                  NOT NULL,
    status               VARCHAR(255) DEFAULT 'ACTIVE' NOT NULL,
    deleted              BOOLEAN                       NOT NULL,
    api_key              VARCHAR(52)                   NOT NULL,
    CONSTRAINT pk_tenant PRIMARY KEY (id)
);

CREATE TABLE virtual_host
(
    id          VARCHAR(26)                   NOT NULL,
    name        VARCHAR(255),
    description VARCHAR(255),
    username    VARCHAR(150)                  NOT NULL,
    password    VARCHAR(150)                  NOT NULL,
    domain      VARCHAR(255)                  NOT NULL,
    url         VARCHAR(150)                  NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
    tenant_id   VARCHAR(26)                   NOT NULL,
    status      VARCHAR(255) DEFAULT 'ACTIVE' NOT NULL,
    deleted     BOOLEAN                       NOT NULL,
    CONSTRAINT "pk_vırtual_host" PRIMARY KEY (id)
);

ALTER TABLE queue
    ADD CONSTRAINT uc_queue_actual_name UNIQUE (actual_name);

ALTER TABLE tenant
    ADD CONSTRAINT "uc_tenant_emaıl" UNIQUE (email);

ALTER TABLE tenant
    ADD CONSTRAINT "uc_tenant_subscrıptıon_plan" UNIQUE (subscription_plan_id);

ALTER TABLE tenant
    ADD CONSTRAINT uc_tenant_username UNIQUE (username);

ALTER TABLE virtual_host
    ADD CONSTRAINT "uc_vırtual_host_url" UNIQUE (url);

ALTER TABLE virtual_host
    ADD CONSTRAINT "uc_vırtual_host_username" UNIQUE (username);

ALTER TABLE binding
    ADD CONSTRAINT FK_BINDING_ON_DESTINATION_EXCHANGE FOREIGN KEY (destination_exchange_id) REFERENCES exchange (id)
        ON DELETE CASCADE;

ALTER TABLE binding
    ADD CONSTRAINT FK_BINDING_ON_DESTINATION_QUEUE FOREIGN KEY (destination_queue_id) REFERENCES queue (id)
        ON DELETE CASCADE;

ALTER TABLE binding
    ADD CONSTRAINT FK_BINDING_ON_SOURCE_EXCHANGE FOREIGN KEY (source_exchange_id) REFERENCES exchange (id)
        ON DELETE CASCADE;

ALTER TABLE binding
    ADD CONSTRAINT FK_BINDING_ON_VHOST FOREIGN KEY (vhost_id) REFERENCES virtual_host (id) ON DELETE CASCADE;

ALTER TABLE exchange
    ADD CONSTRAINT FK_EXCHANGE_ON_VHOST FOREIGN KEY (vhost_id) REFERENCES virtual_host (id) ON DELETE CASCADE;

ALTER TABLE queue
    ADD CONSTRAINT FK_QUEUE_ON_VHOST FOREIGN KEY (vhost_id) REFERENCES virtual_host (id) ON DELETE CASCADE;

ALTER TABLE tenant
    ADD CONSTRAINT FK_TENANT_ON_SUBSCRIPTION_PLAN FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plan (name);

ALTER TABLE virtual_host
    ADD CONSTRAINT FK_VIRTUAL_HOST_ON_TENANT FOREIGN KEY (tenant_id) REFERENCES tenant (id);