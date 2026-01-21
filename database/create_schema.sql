-- =====================================================
-- AI Supply Chain Control Tower - Database Schema
-- PostgreSQL Script
-- =====================================================
-- Tạo schema theo thứ tự dependency
-- Chạy script này trong pgAdmin để tạo toàn bộ database structure

-- =====================================================
-- 1. Master Data Tables
-- =====================================================

-- Table: locations
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    latitude DOUBLE PRECISION NOT NULL CHECK (latitude >= -90 AND latitude <= 90),
    longitude DOUBLE PRECISION NOT NULL CHECK (longitude >= -180 AND longitude <= 180),
    type VARCHAR(20) NOT NULL,
    address VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_location_name UNIQUE (name)
);

CREATE INDEX idx_location_type ON locations(type);

COMMENT ON TABLE locations IS 'Điểm địa lý chuẩn hóa';


-- Table: customers
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    company_name VARCHAR(200),
    default_location_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_customer_location FOREIGN KEY (default_location_id) REFERENCES locations(id)
);

CREATE INDEX idx_customer_name ON customers(name);
CREATE INDEX idx_customer_active ON customers(active);

COMMENT ON TABLE customers IS 'Khách hàng - người đặt đơn giao hàng';


-- Table: depots
CREATE TABLE depots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    location_id BIGINT NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity >= 1),
    operating_start TIME,
    operating_end TIME,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_depot_name UNIQUE (name),
    CONSTRAINT fk_depot_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE INDEX idx_depot_location ON depots(location_id);
CREATE INDEX idx_depot_active ON depots(active);

COMMENT ON TABLE depots IS 'Kho/Trung tâm phân phối';
COMMENT ON COLUMN depots.operating_start IS 'Giờ mở cửa depot (e.g., 08:00)';
COMMENT ON COLUMN depots.operating_end IS 'Giờ đóng cửa depot (e.g., 18:00)';


-- Table: drivers
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_driver_license UNIQUE (license_number)
);

COMMENT ON TABLE drivers IS 'Tài xế';


-- Table: vehicles
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    plate_number VARCHAR(20) NOT NULL UNIQUE,
    depot_id BIGINT NOT NULL,
    driver_id BIGINT,
    capacity DOUBLE PRECISION NOT NULL CHECK (capacity >= 0),
    current_latitude DOUBLE PRECISION,
    current_longitude DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_vehicle_plate UNIQUE (plate_number),
    CONSTRAINT fk_vehicle_depot FOREIGN KEY (depot_id) REFERENCES depots(id),
    CONSTRAINT fk_vehicle_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

CREATE INDEX idx_vehicle_depot ON vehicles(depot_id);
CREATE INDEX idx_vehicle_driver ON vehicles(driver_id);
CREATE INDEX idx_vehicle_status ON vehicles(status);

COMMENT ON TABLE vehicles IS 'Xe giao hàng';


-- =====================================================
-- 2. Order & Execution Tables
-- =====================================================

-- Table: delivery_orders
CREATE TABLE delivery_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    pickup_location_id BIGINT NOT NULL,
    delivery_location_id BIGINT NOT NULL,
    pickup_start_time TIMESTAMP,
    pickup_end_time TIMESTAMP,
    delivery_start_time TIMESTAMP NOT NULL,
    delivery_end_time TIMESTAMP NOT NULL,
    weight DOUBLE PRECISION NOT NULL CHECK (weight >= 0),
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_order_number UNIQUE (order_number),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_order_pickup_location FOREIGN KEY (pickup_location_id) REFERENCES locations(id),
    CONSTRAINT fk_order_delivery_location FOREIGN KEY (delivery_location_id) REFERENCES locations(id)
);

CREATE INDEX idx_order_customer ON delivery_orders(customer_id);
CREATE INDEX idx_order_pickup_location ON delivery_orders(pickup_location_id);
CREATE INDEX idx_order_delivery_location ON delivery_orders(delivery_location_id);
CREATE INDEX idx_order_status ON delivery_orders(status);
CREATE INDEX idx_order_priority ON delivery_orders(priority);

COMMENT ON TABLE delivery_orders IS 'Đơn hàng giao nhận';
COMMENT ON COLUMN delivery_orders.pickup_start_time IS 'Thời gian bắt đầu khung giờ lấy hàng';
COMMENT ON COLUMN delivery_orders.pickup_end_time IS 'Thời gian kết thúc khung giờ lấy hàng';


-- Table: delivery_tasks
CREATE TABLE delivery_tasks (
    id BIGSERIAL PRIMARY KEY,
    delivery_order_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    driver_id BIGINT,
    route_plan_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    actual_pickup_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_task_order FOREIGN KEY (delivery_order_id) REFERENCES delivery_orders(id),
    CONSTRAINT fk_task_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_task_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
    -- FK cho route_plan_id sẽ thêm sau khi route_plans được tạo
);

CREATE INDEX idx_task_order ON delivery_tasks(delivery_order_id);
CREATE INDEX idx_task_vehicle ON delivery_tasks(vehicle_id);
CREATE INDEX idx_task_driver ON delivery_tasks(driver_id);
CREATE INDEX idx_task_route ON delivery_tasks(route_plan_id);
CREATE INDEX idx_task_status ON delivery_tasks(status);

COMMENT ON TABLE delivery_tasks IS 'Thực thi giao hàng';


-- =====================================================
-- 3. Routing & Optimization Tables (AI Core)
-- =====================================================

-- Table: optimization_runs
CREATE TABLE optimization_runs (
    id BIGSERIAL PRIMARY KEY,
    trigger_type VARCHAR(20) NOT NULL,
    trigger_reason VARCHAR(500),
    input_snapshot JSONB,
    output_metrics JSONB,
    execution_time_ms BIGINT,
    completed_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_optimization_trigger ON optimization_runs(trigger_type);
CREATE INDEX idx_optimization_status ON optimization_runs(status);
CREATE INDEX idx_optimization_created ON optimization_runs(created_at);

COMMENT ON TABLE optimization_runs IS 'Lịch sử chạy tối ưu AI';


-- Table: route_plans
CREATE TABLE route_plans (
    id BIGSERIAL PRIMARY KEY,
    optimization_run_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    planned_start_time TIMESTAMP NOT NULL,
    planned_end_time TIMESTAMP NOT NULL,
    total_distance_km DOUBLE PRECISION CHECK (total_distance_km >= 0),
    total_duration_minutes INTEGER CHECK (total_duration_minutes >= 0),
    version INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_route_optimization FOREIGN KEY (optimization_run_id) REFERENCES optimization_runs(id),
    CONSTRAINT fk_route_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

CREATE INDEX idx_route_optimization ON route_plans(optimization_run_id);
CREATE INDEX idx_route_vehicle ON route_plans(vehicle_id);
CREATE INDEX idx_route_status ON route_plans(status);
CREATE INDEX idx_route_version ON route_plans(version);
CREATE INDEX idx_route_active ON route_plans(is_active);

COMMENT ON TABLE route_plans IS 'Kế hoạch tuyến đường';
COMMENT ON COLUMN route_plans.is_active IS 'Route còn active hay đã bị supersede bởi re-optimization';


-- Thêm FK constraint cho delivery_tasks.route_plan_id
ALTER TABLE delivery_tasks
ADD CONSTRAINT fk_task_route_plan FOREIGN KEY (route_plan_id) REFERENCES route_plans(id);


-- Table: route_stops
CREATE TABLE route_stops (
    id BIGSERIAL PRIMARY KEY,
    route_plan_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    delivery_order_id BIGINT,
    stop_type VARCHAR(20) NOT NULL,
    sequence INTEGER NOT NULL CHECK (sequence >= 1),
    planned_arrival TIMESTAMP NOT NULL,
    planned_departure TIMESTAMP NOT NULL,
    actual_arrival TIMESTAMP,
    actual_departure TIMESTAMP,
    distance_from_prev_km DOUBLE PRECISION,
    duration_from_prev_minutes INTEGER,
    service_time_minutes INTEGER DEFAULT 15,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_stop_route FOREIGN KEY (route_plan_id) REFERENCES route_plans(id),
    CONSTRAINT fk_stop_location FOREIGN KEY (location_id) REFERENCES locations(id),
    CONSTRAINT fk_stop_order FOREIGN KEY (delivery_order_id) REFERENCES delivery_orders(id),
    CONSTRAINT chk_stop_order_required CHECK (stop_type = 'DEPOT' OR delivery_order_id IS NOT NULL)
);

CREATE INDEX idx_stop_route_seq ON route_stops(route_plan_id, sequence);
CREATE INDEX idx_stop_location ON route_stops(location_id);
CREATE INDEX idx_stop_order ON route_stops(delivery_order_id);
CREATE INDEX idx_stop_status ON route_stops(status);
CREATE INDEX idx_stop_type ON route_stops(stop_type);

COMMENT ON TABLE route_stops IS 'Điểm dừng trên tuyến (plan vs actual)';
COMMENT ON COLUMN route_stops.stop_type IS 'Loại điểm dừng: DEPOT, PICKUP, DELIVERY';
COMMENT ON COLUMN route_stops.distance_from_prev_km IS 'Khoảng cách từ điểm dừng trước (km)';
COMMENT ON COLUMN route_stops.duration_from_prev_minutes IS 'Thời gian di chuyển từ điểm dừng trước (phút)';
COMMENT ON COLUMN route_stops.service_time_minutes IS 'Thời gian xử lý tại điểm dừng (phút)';


-- =====================================================
-- 4. Realtime & Event Tables
-- =====================================================

-- Table: vehicle_positions
CREATE TABLE vehicle_positions (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL CHECK (latitude >= -90 AND latitude <= 90),
    longitude DOUBLE PRECISION NOT NULL CHECK (longitude >= -180 AND longitude <= 180),
    speed_kmh DOUBLE PRECISION CHECK (speed_kmh >= 0),
    heading_degrees DOUBLE PRECISION CHECK (heading_degrees >= 0 AND heading_degrees <= 360),
    timestamp TIMESTAMP NOT NULL,
    source VARCHAR(20) NOT NULL DEFAULT 'GPS',
    accuracy_meters DOUBLE PRECISION,
    
    CONSTRAINT fk_position_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

CREATE INDEX idx_position_vehicle_time ON vehicle_positions(vehicle_id, timestamp DESC);
CREATE INDEX idx_position_timestamp ON vehicle_positions(timestamp DESC);

COMMENT ON TABLE vehicle_positions IS 'GPS tracking time-series data';

-- =====================================================
-- SCALABILITY: Partitioning Strategy for vehicle_positions
-- =====================================================
-- Khi scale lớn, nên partition table này theo timestamp:
--
-- Option 1: Partition by RANGE (timestamp) - Monthly partitions
-- CREATE TABLE vehicle_positions_2026_01 PARTITION OF vehicle_positions
--     FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
--
-- Option 2: Use pg_partman extension cho automatic partition management
--
-- Data Retention Policy:
-- - Keep last 90 days in main database
-- - Archive older data to cold storage
-- - Script example:
--   DELETE FROM vehicle_positions WHERE timestamp < NOW() - INTERVAL '90 days';
--
-- =====================================================



-- Table: system_events
CREATE TABLE system_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    payload JSONB,
    timestamp TIMESTAMP NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    message VARCHAR(1000)
);

CREATE INDEX idx_event_type_time ON system_events(event_type, timestamp DESC);
CREATE INDEX idx_event_entity ON system_events(entity_type, entity_id);
CREATE INDEX idx_event_severity ON system_events(severity);
CREATE INDEX idx_event_timestamp ON system_events(timestamp DESC);

COMMENT ON TABLE system_events IS 'Event sourcing cho monitoring & analytics';


-- Table: disruption_events
CREATE TABLE disruption_events (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    affected_vehicle_id BIGINT,
    affected_route_id BIGINT,
    location_id BIGINT,
    severity VARCHAR(20) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'DETECTED',
    resolution_notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_disruption_vehicle FOREIGN KEY (affected_vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_disruption_route FOREIGN KEY (affected_route_id) REFERENCES route_plans(id),
    CONSTRAINT fk_disruption_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE INDEX idx_disruption_type ON disruption_events(type);
CREATE INDEX idx_disruption_vehicle ON disruption_events(affected_vehicle_id);
CREATE INDEX idx_disruption_route ON disruption_events(affected_route_id);
CREATE INDEX idx_disruption_status_severity ON disruption_events(status, severity);
CREATE INDEX idx_disruption_detected ON disruption_events(detected_at DESC);

COMMENT ON TABLE disruption_events IS 'Sự cố vận hành - trigger re-optimization';


-- =====================================================
-- 5. Decision & Control Tables (Explainable AI)
-- =====================================================

-- Table: decision_logs
CREATE TABLE decision_logs (
    id BIGSERIAL PRIMARY KEY,
    optimization_run_id BIGINT NOT NULL,
    decision_type VARCHAR(30) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    alternatives JSONB,
    selected_option VARCHAR(500) NOT NULL,
    confidence_score DOUBLE PRECISION CHECK (confidence_score >= 0 AND confidence_score <= 1),
    timestamp TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_decision_optimization FOREIGN KEY (optimization_run_id) REFERENCES optimization_runs(id)
);

CREATE INDEX idx_decision_optimization ON decision_logs(optimization_run_id);
CREATE INDEX idx_decision_type ON decision_logs(decision_type);
CREATE INDEX idx_decision_timestamp ON decision_logs(timestamp DESC);

COMMENT ON TABLE decision_logs IS 'Explainable AI - giải thích quyết định';


-- Table: manual_overrides
CREATE TABLE manual_overrides (
    id BIGSERIAL PRIMARY KEY,
    override_type VARCHAR(30) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    original_value VARCHAR(500),
    new_value VARCHAR(500) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    approved BOOLEAN NOT NULL DEFAULT false,
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- NOTE: performed_by, approved_by KHÔNG có FK constraint (loose coupling)
);

CREATE INDEX idx_override_type ON manual_overrides(override_type);
CREATE INDEX idx_override_entity ON manual_overrides(entity_type, entity_id);
CREATE INDEX idx_override_user ON manual_overrides(performed_by);
CREATE INDEX idx_override_timestamp ON manual_overrides(timestamp DESC);

COMMENT ON TABLE manual_overrides IS 'Human-in-the-loop - audit trail can thiệp thủ công';


-- =====================================================
-- 6. Distance Matrix (Caching for Optimization)
-- =====================================================

-- Table: distance_matrix
CREATE TABLE distance_matrix (
    id BIGSERIAL PRIMARY KEY,
    from_location_id BIGINT NOT NULL,
    to_location_id BIGINT NOT NULL,
    distance_km DOUBLE PRECISION NOT NULL CHECK (distance_km >= 0),
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes >= 0),
    source VARCHAR(30) NOT NULL DEFAULT 'CALCULATED',
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_distance_from FOREIGN KEY (from_location_id) REFERENCES locations(id),
    CONSTRAINT fk_distance_to FOREIGN KEY (to_location_id) REFERENCES locations(id),
    CONSTRAINT uk_distance_pair UNIQUE (from_location_id, to_location_id)
);

CREATE INDEX idx_distance_from ON distance_matrix(from_location_id);
CREATE INDEX idx_distance_to ON distance_matrix(to_location_id);

COMMENT ON TABLE distance_matrix IS 'Cache khoảng cách và thời gian di chuyển giữa các locations';
COMMENT ON COLUMN distance_matrix.source IS 'Nguồn tính toán: CALCULATED, GOOGLE_MAPS, OSRM, etc.';


-- =====================================================
-- 0. Dispatchers (Auth)
-- =====================================================

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

-- =====================================================
-- 1. Locations
-- =====================================================

-- =====================================================
-- Summary
-- =====================================================

-- 4. drivers
-- 5. vehicles
-- 6. delivery_orders
-- 7. delivery_tasks
-- 8. optimization_runs
-- 9. route_plans
-- 10. route_stops
-- 11. vehicle_positions
-- 12. system_events
-- 13. disruption_events
-- 14. decision_logs
-- 15. manual_overrides
-- 16. distance_matrix (NEW)

-- Foreign Key Constraints: 17 FKs
-- Loose Coupling: 2 fields không có FK (performed_by, approved_by)

SELECT 'Database schema created successfully!' AS status;

