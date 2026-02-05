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
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- 2. Dispatcher 
CREATE TABLE dispatchers (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'DISPATCHER'
);

-- 3. Drivers
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- 4. Locations (With separated address fields)
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'Việt Nam',

    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    
    CONSTRAINT uq_locations_address_coords
        UNIQUE (street, city, country, latitude, longitude)
);

-- 5. Depots (Start locations for vehicles)
CREATE TABLE depots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,

    location_id BIGINT NOT NULL,

    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_depots_location
        FOREIGN KEY (location_id)
        REFERENCES locations(id)
);

-- 6. Vehicles
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,

    max_weight_kg INT,
    max_volume_m3 NUMERIC(6,2),

    cost_per_km NUMERIC(10,2),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    type VARCHAR(100),
    driver_id BIGINT UNIQUE, -- QUAN TRỌNG
    depot_id BIGINT,

    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehicles_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id),

    CONSTRAINT fk_vehicles_depot
        FOREIGN KEY (depot_id)
        REFERENCES depots(id)
);

-- 7. Orders (Each order has 1 delivery location)

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,

    delivery_location_id BIGINT NOT NULL,
    weight_kg INT,
    volume_m3 NUMERIC(6,2),
    driver_id BIGINT,

    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_location
        FOREIGN KEY (delivery_location_id)
        REFERENCES locations(id),
    
    CONSTRAINT fk_orders_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id)
);

-- 8. Routing Runs (Optimization sessions)
CREATE TABLE routing_runs (
    id BIGSERIAL PRIMARY KEY,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    start_time TIMESTAMP WITHOUT TIME ZONE,
    end_time TIMESTAMP WITHOUT TIME ZONE,

    total_distance_km NUMERIC(12,2),
    total_cost NUMERIC(12,2),

    configuration TEXT,

    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Routes (1 vehicle = 1 optimized route)
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,

    vehicle_id BIGINT NOT NULL,
    routing_run_id BIGINT,

    total_distance_km NUMERIC(10,2),
    total_duration_min INT,
    total_cost NUMERIC(12,2),

    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    polyline TEXT, --(for map display)

    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_routes_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id),

    CONSTRAINT fk_routes_routing_run
        FOREIGN KEY (routing_run_id)
        REFERENCES routing_runs(id)
);

-- 10. Route Stops (Ordered delivery sequence)
CREATE TABLE route_stops (
    id BIGSERIAL PRIMARY KEY,

    route_id BIGINT NOT NULL,
    order_id BIGINT, -- Nullable (for depot stops)
    location_id BIGINT NOT NULL, -- New field

    stop_sequence INT NOT NULL,

    distance_from_prev_km NUMERIC(10,2),
    duration_from_prev_min INT,

    arrival_time TIMESTAMP WITHOUT TIME ZONE,
    departure_time TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT fk_route_stops_route
        FOREIGN KEY (route_id)
        REFERENCES routes(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_route_stops_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CONSTRAINT fk_route_stops_location
        FOREIGN KEY (location_id)
        REFERENCES locations(id),

    CONSTRAINT uq_route_stop_sequence
        UNIQUE (route_id, stop_sequence)
);

-- 11. Indexes (minimal but useful)
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_driver ON orders(driver_id);
CREATE INDEX idx_routes_vehicle ON routes(vehicle_id);
CREATE INDEX idx_route_stops_route ON route_stops(route_id);
CREATE INDEX idx_vehicles_driver ON vehicles(driver_id);

-- END OF SCRIPT