-- =====================================================
-- SEED DATA - Logistics Control Hub MVP
-- =====================================================

-- =====================================================
-- 1. Dispatchers
-- =====================================================

INSERT INTO dispatchers (username, password, full_name, role)
VALUES
('dispatcher01', '$2a$10$dummyhashedpassword', 'Nguyen Van A', 'DISPATCHER'),
('admin01', '$2a$10$dummyhashedpassword', 'Tran Thi B', 'ADMIN');

-- =====================================================
-- 2. Vehicles
-- =====================================================

INSERT INTO vehicles (code, max_weight_kg, max_volume_m3, cost_per_km)
VALUES
('VEH-01', 1000, 5.0, 8000),
('VEH-02', 500, 3.0, 6000);

-- =====================================================
-- 3. Locations
-- =====================================================

INSERT INTO locations (name, latitude, longitude)
VALUES
('Depot - HCM', 10.776889, 106.700806),
('Customer A', 10.762622, 106.660172),
('Customer B', 10.780889, 106.699806),
('Customer C', 10.751889, 106.670806);

-- =====================================================
-- 4. Orders
-- =====================================================

INSERT INTO orders (code, delivery_location_id, weight_kg, volume_m3, status)
VALUES
('ORD-001', 2, 200, 1.2, 'CREATED'),
('ORD-002', 3, 300, 1.5, 'CREATED'),
('ORD-003', 4, 400, 2.0, 'CREATED');

-- =====================================================
-- 5. Routes (SIMULATED OPTIMIZATION RESULT)
-- =====================================================

INSERT INTO routes (
    vehicle_id,
    total_distance_km,
    total_duration_min,
    total_cost,
    status
)
VALUES
(1, 18.20, 42, 145600, 'CREATED');

-- =====================================================
-- 6. Route Stops (Delivery Sequence)
-- =====================================================

INSERT INTO route_stops (
    route_id,
    order_id,
    location_id,
    stop_sequence,
    distance_from_prev_km,
    duration_from_prev_min
)
VALUES
(1, 1, 2, 1, 5.20, 12),
(1, 2, 3, 2, 6.40, 15),
(1, 3, 4, 3, 6.60, 15);

-- =====================================================
-- END OF SEED DATA
-- =====================================================
