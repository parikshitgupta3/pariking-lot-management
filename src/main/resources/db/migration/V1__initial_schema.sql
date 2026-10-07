-- Initial schema for the Parking Lot Management System.
-- Column types and names must stay aligned with the JPA entities, because
-- Hibernate runs with ddl-auto=validate against this schema.

CREATE TABLE parking_lot (
    id    VARCHAR(36)   NOT NULL,
    name  VARCHAR(255)  NOT NULL,
    CONSTRAINT pk_parking_lot PRIMARY KEY (id)
);

CREATE TABLE parking_floor (
    id            VARCHAR(36)  NOT NULL,
    floor_number  INTEGER      NOT NULL,
    lot_id        VARCHAR(36)  NOT NULL,
    CONSTRAINT pk_parking_floor PRIMARY KEY (id),
    CONSTRAINT fk_parking_floor_lot
        FOREIGN KEY (lot_id) REFERENCES parking_lot (id),
    -- one floor number per lot (mirrors the domain invariant)
    CONSTRAINT uk_parking_floor_lot_floor_number
        UNIQUE (lot_id, floor_number)
);

CREATE TABLE parking_spot (
    id           VARCHAR(36)   NOT NULL,
    spot_number  VARCHAR(255)  NOT NULL,
    spot_type    VARCHAR(255)  NOT NULL,
    status       VARCHAR(255)  NOT NULL,
    floor_id     VARCHAR(36)   NOT NULL,
    CONSTRAINT pk_parking_spot PRIMARY KEY (id),
    CONSTRAINT fk_parking_spot_floor
        FOREIGN KEY (floor_id) REFERENCES parking_floor (id),
    -- one spot number per floor (mirrors the domain invariant)
    CONSTRAINT uk_parking_spot_floor_spot_number
        UNIQUE (floor_id, spot_number)
);

CREATE TABLE vehicle (
    registration_number  VARCHAR(255)  NOT NULL,
    vehicle_type         VARCHAR(255)  NOT NULL,
    CONSTRAINT pk_vehicle PRIMARY KEY (registration_number)
);

CREATE TABLE parking_ticket (
    id                          VARCHAR(36)              NOT NULL,
    vehicle_registration_number VARCHAR(255)             NOT NULL,
    spot_id                     VARCHAR(36)              NOT NULL,
    entry_time                  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    exit_time                   TIMESTAMP(6) WITH TIME ZONE,
    fee                         NUMERIC(10,2),
    status                      VARCHAR(255)             NOT NULL,
    CONSTRAINT pk_parking_ticket PRIMARY KEY (id),
    CONSTRAINT fk_parking_ticket_vehicle
        FOREIGN KEY (vehicle_registration_number) REFERENCES vehicle (registration_number),
    CONSTRAINT fk_parking_ticket_spot
        FOREIGN KEY (spot_id) REFERENCES parking_spot (id)
);

CREATE TABLE parking_gate (
    id           VARCHAR(36)   NOT NULL,
    gate_number  INTEGER       NOT NULL,
    gate_type    VARCHAR(255)  NOT NULL,
    lot_id       VARCHAR(36)   NOT NULL,
    CONSTRAINT pk_parking_gate PRIMARY KEY (id),
    CONSTRAINT fk_parking_gate_lot
        FOREIGN KEY (lot_id) REFERENCES parking_lot (id),
    -- gate numbers are unique per lot
    CONSTRAINT uk_parking_gate_lot_gate_number
        UNIQUE (lot_id, gate_number)
);

-- Indexes for the hot query paths: aggregate loads (lot -> floors -> spots),
-- ticket lookups by vehicle/spot/status, and availability scans by status.

CREATE INDEX idx_parking_floor_lot          ON parking_floor (lot_id);
CREATE INDEX idx_parking_spot_floor         ON parking_spot (floor_id);
CREATE INDEX idx_parking_spot_status        ON parking_spot (status);
CREATE INDEX idx_parking_ticket_vehicle     ON parking_ticket (vehicle_registration_number);
CREATE INDEX idx_parking_ticket_spot        ON parking_ticket (spot_id);
CREATE INDEX idx_parking_ticket_status      ON parking_ticket (status);
CREATE INDEX idx_parking_gate_lot           ON parking_gate (lot_id);
