ALTER TABLE binding  DROP CONSTRAINT fk_binding_on_vhost;
ALTER TABLE binding  ADD  CONSTRAINT fk_binding_on_vhost
    FOREIGN KEY (vhost_id) REFERENCES virtual_host(id) ON DELETE CASCADE;

ALTER TABLE exchange DROP CONSTRAINT fk_exchange_on_vhost;
ALTER TABLE exchange ADD  CONSTRAINT fk_exchange_on_vhost
    FOREIGN KEY (vhost_id) REFERENCES virtual_host(id) ON DELETE CASCADE;

ALTER TABLE queue    DROP CONSTRAINT fk_queue_on_vhost;
ALTER TABLE queue    ADD  CONSTRAINT fk_queue_on_vhost
    FOREIGN KEY (vhost_id) REFERENCES virtual_host(id) ON DELETE CASCADE;

ALTER TABLE binding DROP CONSTRAINT fk_binding_on_source_exchange;
ALTER TABLE binding ADD  CONSTRAINT fk_binding_on_source_exchange
    FOREIGN KEY (source_exchange_id) REFERENCES exchange(id) ON DELETE CASCADE;

ALTER TABLE binding DROP CONSTRAINT fk_binding_on_destination_exchange;
ALTER TABLE binding ADD  CONSTRAINT fk_binding_on_destination_exchange
    FOREIGN KEY (destination_exchange_id) REFERENCES exchange(id) ON DELETE CASCADE;

ALTER TABLE binding DROP CONSTRAINT fk_binding_on_destination_queue;
ALTER TABLE binding ADD  CONSTRAINT fk_binding_on_destination_queue
    FOREIGN KEY (destination_queue_id) REFERENCES queue(id) ON DELETE CASCADE;