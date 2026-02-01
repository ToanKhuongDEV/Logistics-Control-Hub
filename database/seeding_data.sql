-- =====================================================
-- SEED DATA - Logistics Control Hub MVP
-- =====================================================

-- =====================================================
-- 1. Companies
-- =====================================================

INSERT INTO companies (name, address, phone, email, website, tax_id, description)
VALUES
('LogiTower Vietnam', '123 Đường Trần Hưng Đạo, Quận 1, TPHCM', '+84 28 1234 5678', 'contact@logitower.vn', 'www.logitower.vn', '0123456789', 'Công ty logistics hàng đầu tại Việt Nam');

-- =====================================================
-- 2. Dispatchers
-- =====================================================

INSERT INTO dispatchers (username, password, full_name, role)
VALUES
('dispatcher01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Nguyen Van A', 'DISPATCHER'),
('admin01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Tran Thi B', 'ADMIN');

-- =====================================================
-- 3. Drivers
-- =====================================================

INSERT INTO drivers (name, license_number, phone_number, email)
VALUES
('Nguyen Van A', 'B2-001234', '0901234567', 'driver.a@logitower.vn'),
('Tran Thi B', 'C-005678', '0909876543', 'driver.b@logitower.vn'),
('Le Van C', 'C-009101', '0905555555', 'driver.c@logitower.vn'),
('Pham Thi D', 'B2-001122', '0908888888', 'driver.d@logitower.vn'),
('Hoang Van E', 'FC-003344', '0907777777', 'driver.e@logitower.vn'),
('Do Thi F', 'C-002211', '0906666666', 'driver.f@logitower.vn'),
('Vu Van G', 'C-004455', '0904444444', 'driver.g@logitower.vn'),
('Bui Thi H', 'B2-006677', '0903333333', 'driver.h@logitower.vn'),
('Mai Van I', 'FC-008899', '0902222222', 'driver.i@logitower.vn');

-- =====================================================
-- 3. Vehicles
-- =====================================================

INSERT INTO vehicles (code, max_weight_kg, max_volume_m3, cost_per_km, status, type, driver_id)
VALUES
('LDT-001', 2500, 15.5, 5000, 'ACTIVE', 'Hyundai Mighty', 1),     
('MDT-002', 3500, 18.0, 6500, 'ACTIVE', 'Isuzu FRR', 2),        
('LDT-003', 2000, 12.0, 4500, 'MAINTENANCE', 'Kia K250', 3),     
('LDT-004', 3000, 16.5, 5500, 'ACTIVE', 'Hino XZU', 4),           
('MDT-005', 4000, 20.0, 7000, 'ACTIVE', 'Isuzu FVR', 5),           
('LDT-006', 1800, 11.0, 4000, 'IDLE', 'Thaco Kia K200', NULL),    
('LDT-007', 2800, 14.5, 5200, 'ACTIVE', 'Hyundai HD120S', 6),      
('LDT-008', 3200, 17.0, 6000, 'ACTIVE', 'Hino FC', 7),            
('LDT-009', 2200, 13.0, 4800, 'MAINTENANCE', 'Kia K250', 8),       
('MDT-010', 3800, 19.5, 6800, 'ACTIVE', 'Isuzu FVR', 9);        
-- =====================================================
-- 4. Locations
-- =====================================================

INSERT INTO locations (name, latitude, longitude)
VALUES
('Depot - HCM', 10.776889, 106.700806),
('Customer A', 10.762622, 106.660172),
('Customer B', 10.780889, 106.699806),
('Customer C', 10.751889, 106.670806);

-- =====================================================
-- 5. Orders
-- =====================================================

INSERT INTO orders (code, delivery_location_id, weight_kg, volume_m3, driver_id, status)
VALUES
('ORD-001', 2, 200, 1.2, 1, 'CREATED'),
('ORD-002', 3, 300, 1.5, 2, 'CREATED'),
('ORD-003', 4, 400, 2.0, NULL, 'CREATED');

-- =====================================================
-- 6. Routes (SIMULATED OPTIMIZATION RESULT)
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
-- 7. Route Stops (Delivery Sequence)
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
(1, NULL, 1, 0, 0.00, 0),       -- Start at Depot
(1, 1, 2, 1, 5.20, 12),
(1, 2, 3, 2, 6.40, 15),
(1, 3, 4, 3, 6.60, 15),
(1, NULL, 1, 4, 5.50, 20);      -- Return to Depot

-- =====================================================
-- END OF SEED DATA
-- =====================================================
