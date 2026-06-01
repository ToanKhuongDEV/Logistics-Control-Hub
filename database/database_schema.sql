-- =====================================================
-- DATABASE SCHEMA - Logistics Control Hub
-- PostgreSQL
-- =====================================================

-- 1. Companies
CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    website VARCHAR(255),
    tax_id VARCHAR(50),
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 2. Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'DISPATCHER',
    driver_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_users_role
        CHECK (role IN ('ADMIN', 'DISPATCHER', 'DRIVER')),
    CONSTRAINT ck_users_driver_role_link
        CHECK (
            (role = 'DRIVER' AND driver_id IS NOT NULL)
            OR (role <> 'DRIVER' AND driver_id IS NULL)
        )
);

-- 3. Drivers
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id);

-- 4. Locations
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'Viet Nam',
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5. Depots
CREATE TABLE depots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location_id BIGINT NOT NULL,
    dispatcher_id BIGINT,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_depots_location
        FOREIGN KEY (location_id)
        REFERENCES locations(id),
    CONSTRAINT fk_depots_dispatcher
        FOREIGN KEY (dispatcher_id)
        REFERENCES users(id)
);

-- 6. Vehicles
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    max_weight_kg INT,
    max_volume_m3 NUMERIC(6,2),
    cost_per_km NUMERIC(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    type VARCHAR(100),
    driver_id BIGINT,
    depot_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_vehicles_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id),
    CONSTRAINT fk_vehicles_depot
        FOREIGN KEY (depot_id)
        REFERENCES depots(id),
    CONSTRAINT ck_vehicles_status
        CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'IDLE'))
);

-- 7. Orders
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    delivery_location_id BIGINT NOT NULL,
    weight_kg INT,
    volume_m3 NUMERIC(6,2),
    driver_id BIGINT,
    depot_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_orders_location
        FOREIGN KEY (delivery_location_id)
        REFERENCES locations(id),
    CONSTRAINT fk_orders_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id),
    CONSTRAINT fk_orders_depot
        FOREIGN KEY (depot_id)
        REFERENCES depots(id),
    CONSTRAINT ck_orders_status
        CHECK (status IN ('CREATED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'))
);

-- 8. Routing Runs
CREATE TABLE routing_runs (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP WITHOUT TIME ZONE,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    total_distance_km NUMERIC(12,2),
    total_cost NUMERIC(12,2),
    configuration TEXT,
    depot_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_routing_runs_depot
        FOREIGN KEY (depot_id)
        REFERENCES depots(id),
    CONSTRAINT ck_routing_runs_status
        CHECK (status IN ('COMPLETED', 'FAILED'))
);

-- 9. Routes
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    routing_run_id BIGINT,
    total_distance_km NUMERIC(10,2),
    total_duration_min INT,
    total_cost NUMERIC(12,2),
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    polyline TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_routes_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_routes_routing_run
        FOREIGN KEY (routing_run_id)
        REFERENCES routing_runs(id),
    CONSTRAINT ck_routes_status
        CHECK (status IN ('CREATED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- 10. Route Stops
CREATE TABLE route_stops (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    order_id BIGINT,
    location_id BIGINT NOT NULL,
    stop_sequence INT NOT NULL,
    distance_from_prev_km NUMERIC(10,2),
    duration_from_prev_min INT,
    arrival_time TIMESTAMP WITHOUT TIME ZONE,
    departure_time TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_route_stops_route
        FOREIGN KEY (route_id)
        REFERENCES routes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_route_stops_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_route_stops_location
        FOREIGN KEY (location_id)
        REFERENCES locations(id)
);

-- 11. Refresh Tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    jti VARCHAR(36) NOT NULL,
    token TEXT NOT NULL,
    username VARCHAR(50) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 12. Password Reset Tokens
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 13. Audit Logs
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT,
    actor_username VARCHAR(50),
    actor_role VARCHAR(20),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    resource_name VARCHAR(255),
    scope_depot_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    message TEXT,
    before_data JSONB,
    after_data JSONB,
    metadata JSONB,
    ip_address VARCHAR(64),
    user_agent VARCHAR(500),
    request_id VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_user_id)
        REFERENCES users(id),
    CONSTRAINT ck_audit_logs_status
        CHECK (status IN ('SUCCESS', 'FAILED'))
);

-- =====================================================
-- Normal indexes
-- =====================================================

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_users_driver ON users(driver_id);
CREATE INDEX idx_orders_driver ON orders(driver_id);
CREATE INDEX idx_routes_vehicle ON routes(vehicle_id);
CREATE INDEX idx_route_stops_route ON route_stops(route_id);
CREATE INDEX idx_vehicles_driver ON vehicles(driver_id);
CREATE INDEX idx_refresh_tokens_username ON refresh_tokens(username);
CREATE INDEX idx_refresh_tokens_jti ON refresh_tokens(jti);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_email ON password_reset_tokens(email);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_scope_depot_id ON audit_logs(scope_depot_id);

-- =====================================================
-- Soft-delete-aware unique indexes
-- =====================================================

CREATE UNIQUE INDEX ux_users_username_active
    ON users (username)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_users_email_active
    ON users (email)
    WHERE deleted = FALSE AND email IS NOT NULL;

CREATE UNIQUE INDEX ux_users_driver_active
    ON users (driver_id)
    WHERE deleted = FALSE AND driver_id IS NOT NULL;

CREATE UNIQUE INDEX ux_drivers_license_number_active
    ON drivers (license_number)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_drivers_phone_number_active
    ON drivers (phone_number)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_locations_address_coords_active
    ON locations (street, city, country, latitude, longitude)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_depots_location_active
    ON depots (location_id)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_vehicles_code_active
    ON vehicles (code)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_vehicles_driver_active
    ON vehicles (driver_id)
    WHERE deleted = FALSE AND driver_id IS NOT NULL;

CREATE UNIQUE INDEX ux_orders_code_active
    ON orders (code)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_route_stops_route_sequence_active
    ON route_stops (route_id, stop_sequence)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_refresh_tokens_jti_active
    ON refresh_tokens (jti)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX ux_password_reset_tokens_token_active
    ON password_reset_tokens (token)
    WHERE deleted = FALSE;

-- END OF SCRIPT
