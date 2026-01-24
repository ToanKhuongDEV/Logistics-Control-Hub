-- 1. Dispatcher 
CREATE TABLE dispatchers (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'DISPATCHER',
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Vehicles
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,

    max_weight_kg INT,
    max_volume_m3 NUMERIC(6,2),

    cost_per_km NUMERIC(10,2),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Locations (Coordinates only)
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),

    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL
);

-- 4. Orders (Each order has 1 delivery location)

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,

    delivery_location_id BIGINT NOT NULL,
    weight_kg INT,
    volume_m3 NUMERIC(6,2),

    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_location
        FOREIGN KEY (delivery_location_id)
        REFERENCES locations(id)
);

-- 5. Routes (1 vehicle = 1 optimized route)
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,

    vehicle_id BIGINT NOT NULL,

    total_distance_km NUMERIC(10,2),
    total_duration_min INT,
    total_cost NUMERIC(12,2),

    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_routes_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
);

-- 6. Route Stops (Ordered delivery sequence)
CREATE TABLE route_stops (
    id BIGSERIAL PRIMARY KEY,

    route_id BIGINT NOT NULL,
    order_id BIGINT, -- Nullable (for depot stops)
    location_id BIGINT NOT NULL, -- New field

    stop_sequence INT NOT NULL,

    distance_from_prev_km NUMERIC(10,2),
    duration_from_prev_min INT,

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

-- 7. Indexes (minimal but useful)
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_routes_vehicle ON routes(vehicle_id);
CREATE INDEX idx_route_stops_route ON route_stops(route_id);

-- END OF SCRIPT