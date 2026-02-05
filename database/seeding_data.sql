-- =====================================================
-- SEED DATA - Logistics Control Hub MVP
-- =====================================================

-- =====================================================
-- 1. Companies
-- =====================================================

INSERT INTO companies (name, address, phone, email, website, tax_id, description)
VALUES
('LogiTower Vietnam', '123 Đường Trần Hưng Đạo, Quận 1, TPHCM', '+84 28 1234 5678', 'contact@logitower.vn', 'www.logitower.vn', '0123456789', 'Công ty logistics hàng đầu tại Việt Nam'),
('FastShip Express', '456 Lê Lợi, Quận 1, TPHCM', '+84 28 9876 5432', 'info@fastship.vn', 'www.fastship.vn', '0987654321', 'Dịch vụ giao hàng nhanh toàn quốc'),
('VietLogistics Co', '789 Hai Bà Trưng, Quận 3, TPHCM', '+84 28 5555 1234', 'contact@vietlog.vn', 'www.vietlog.vn', '0111222333', 'Giải pháp logistics tối ưu'),
('Global Transport Ltd', '234 Nguyễn Văn Cừ, Quận 5, TPHCM', '+84 28 7777 8888', 'hello@gtransport.vn', 'www.gtransport.vn', '0444555666', 'Vận chuyển quốc tế và nội địa'),
('SmartFreight JSC', '567 Võ Văn Tần, Quận 3, TPHCM', '+84 28 3333 2222', 'support@smartfreight.vn', 'www.smartfreight.vn', '0777888999', 'Hệ thống vận tải thông minh'),
('MegaCargo Group', '890 Điện Biên Phủ, Bình Thạnh, TPHCM', '+84 28 9999 0000', 'cs@megacargo.vn', 'www.megacargo.vn', '0222333444', 'Tập đoàn vận tải hàng hóa lớn'),
('SwiftDelivery Inc', '345 Trường Sơn, Tân Bình, TPHCM', '+84 28 6666 5555', 'info@swiftdel.vn', 'www.swiftdelivery.vn', '0555666777', 'Giao hàng nhanh trong ngày'),
('EcoTrans Solutions', '678 CMT8, Quận 10, TPHCM', '+84 28 4444 3333', 'contact@ecotrans.vn', 'www.ecotrans.vn', '0888999000', 'Giải pháp vận tải xanh'),
('VNExpress Logistics', '901 Lý Thường Kiệt, Quận 11, TPHCM', '+84 28 2222 1111', 'hello@vnexlog.vn', 'www.vnexpresslog.vn', '0333444555', 'Dịch vụ logistics toàn diện'),
('ProShip Network', '123 Phan Đăng Lưu, Phú Nhuận, TPHCM', '+84 28 8888 7777', 'support@proship.vn', 'www.proshipnetwork.vn', '0666777888', 'Mạng lưới giao nhận'),
('DirectCargo Hub', '456 Cộng Hòa, Tân Bình, TPHCM', '+84 28 1111 2222', 'info@directcargo.vn', 'www.directcargo.vn', '0999000111', 'Trung tâm hàng hóa trực tiếp');

-- =====================================================
-- 2. Dispatchers
-- =====================================================

INSERT INTO dispatchers (username, password, full_name, role)
VALUES
('dispatcher01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Nguyễn Văn An', 'DISPATCHER'),
('dispatcher02', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Trần Thị Bích', 'DISPATCHER'),
('dispatcher03', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Lê Minh Châu', 'DISPATCHER'),
('dispatcher04', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Phạm Quốc Duy', 'DISPATCHER'),
('dispatcher05', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Hoàng Thu Hà', 'DISPATCHER'),
('dispatcher06', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Đỗ Văn Hùng', 'DISPATCHER'),
('dispatcher07', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Vũ Thị Lan', 'DISPATCHER'),
('dispatcher08', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Bùi Minh Nam', 'DISPATCHER'),
('dispatcher09', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Mai Thu Phương', 'DISPATCHER'),
('dispatcher10', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Ngô Văn Quang', 'DISPATCHER'),
('admin01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'Trần Thị Hương', 'ADMIN');

-- =====================================================
-- 3. Drivers
-- =====================================================

INSERT INTO drivers (name, license_number, phone_number, email)
VALUES
('Nguyễn Văn An', 'B2-001234', '0901234567', 'driver.an@logitower.vn'),
('Trần Thị Bình', 'C-005678', '0909876543', 'driver.binh@logitower.vn'),
('Lê Văn Cường', 'C-009101', '0905555555', 'driver.cuong@logitower.vn'),
('Phạm Thị Dung', 'B2-001122', '0908888888', 'driver.dung@logitower.vn'),
('Hoàng Văn Em', 'FC-003344', '0907777777', 'driver.em@logitower.vn'),
('Đỗ Thị Phương', 'C-002211', '0906666666', 'driver.phuong@logitower.vn'),
('Vũ Văn Giang', 'C-004455', '0904444444', 'driver.giang@logitower.vn'),
('Bùi Thị Hoa', 'B2-006677', '0903333333', 'driver.hoa@logitower.vn'),
('Mai Văn Ích', 'FC-008899', '0902222222', 'driver.ich@logitower.vn'),
('Ngô Thị Kiều', 'B2-009988', '0901111111', 'driver.kieu@logitower.vn'),
('Đinh Văn Lâm', 'C-007766', '0909999999', 'driver.lam@logitower.vn'),
('Lý Thị Mai', 'FC-005544', '0908877665', 'driver.mai@logitower.vn');

-- =====================================================
-- 4. Locations (Only 2 as requested)
-- =====================================================

INSERT INTO locations (street, city, country, latitude, longitude)
VALUES
('388 Trần Phú', 'Từ Sơn', 'Bắc Ninh', 21.120556, 105.973056),
('147 Nguyễn Huệ', 'Quận 1', 'Hồ Chí Minh', 10.776889, 106.700806);

-- =====================================================
-- 5. Depots
-- =====================================================

INSERT INTO depots (name, location_id, description, is_active)
VALUES
('Kho Trung Tâm TP.HCM', 2, 'Kho chính tại TP.HCM - Điểm xuất phát cho các tuyến giao hàng', TRUE),
('Kho Miền Bắc', 1, 'Kho phân phối miền Bắc - Bắc Ninh', TRUE),
('Kho Tân Bình', 2, 'Kho tại Tân Bình - TP.HCM', TRUE),
('Kho Bình Dương', 2, 'Kho công nghiệp Bình Dương', TRUE),
('Kho Đồng Nai', 2, 'Kho khu công nghiệp Đồng Nai', TRUE),
('Kho Hải Phòng', 1, 'Kho cảng Hải Phòng', FALSE),
('Kho Đà Nẵng', 2, 'Kho miền Trung - Đà Nẵng', TRUE),
('Kho Cần Thơ', 2, 'Kho miền Tây - Cần Thơ', TRUE),
('Kho Nha Trang', 2, 'Kho ven biển Nha Trang', TRUE),
('Kho Vũng Tàu', 2, 'Kho cảng Vũng Tàu', FALSE),
('Kho Long An', 2, 'Kho Long An - cửa khẩu biên giới', TRUE);

-- =====================================================
-- 6. Vehicles
-- =====================================================

INSERT INTO vehicles (code, max_weight_kg, max_volume_m3, cost_per_km, status, type, driver_id, depot_id)
VALUES
('LDT-001', 2500, 15.5, 5000, 'ACTIVE', 'Hyundai Mighty', 1, 1),     
('MDT-002', 3500, 18.0, 6500, 'ACTIVE', 'Isuzu FRR', 2, 1),        
('LDT-003', 2000, 12.0, 4500, 'MAINTENANCE', 'Kia K250', 3, 2),     
('LDT-004', 3000, 16.5, 5500, 'ACTIVE', 'Hino XZU', 4, 1),           
('MDT-005', 4000, 20.0, 7000, 'ACTIVE', 'Isuzu FVR', 5, 3),           
('LDT-006', 1800, 11.0, 4000, 'IDLE', 'Thaco Kia K200', NULL, 4),    
('LDT-007', 2800, 14.5, 5200, 'ACTIVE', 'Hyundai HD120S', 6, 5),      
('LDT-008', 3200, 17.0, 6000, 'ACTIVE', 'Hino FC', 7, 1),            
('LDT-009', 2200, 13.0, 4800, 'MAINTENANCE', 'Kia K250', 8, 7),       
('MDT-010', 3800, 19.5, 6800, 'ACTIVE', 'Isuzu FVR', 9, 8),
('LDT-011', 2600, 15.0, 5100, 'ACTIVE', 'Hyundai Porter', 10, 9),
('LDT-012', 2900, 16.0, 5400, 'IDLE', 'Kia Frontier', NULL, 11),
('MDT-013', 4200, 21.0, 7200, 'ACTIVE', 'Hino FL', 11, 1),
('LDT-014', 2400, 14.0, 4900, 'MAINTENANCE', 'Thaco Ollin', 12, 2);

-- =====================================================
-- 7. Orders
-- =====================================================

INSERT INTO orders (code, delivery_location_id, weight_kg, volume_m3, driver_id, status)
VALUES
('ORD-001', 2, 200, 1.2, 1, 'CREATED'),
('ORD-002', 1, 300, 1.5, 2, 'CREATED'),
('ORD-003', 2, 400, 2.0, NULL, 'CREATED'),
('ORD-004', 1, 150, 1.0, 3, 'IN_TRANSIT'),
('ORD-005', 2, 250, 1.3, 4, 'CREATED'),
('ORD-006', 1, 350, 1.8, NULL, 'CREATED'),
('ORD-007', 2, 180, 1.1, 5, 'DELIVERED'),
('ORD-008', 1, 220, 1.4, 6, 'CREATED'),
('ORD-009', 2, 280, 1.6, NULL, 'CREATED'),
('ORD-010', 1, 320, 1.7, 7, 'IN_TRANSIT'),
('ORD-011', 2, 190, 1.2, 8, 'CREATED'),
('ORD-012', 1, 380, 1.9, NULL, 'CREATED');

-- =====================================================
-- 8. Routing Runs (Optimization sessions)
-- =====================================================

INSERT INTO routing_runs (status, start_time, end_time, total_distance_km, total_cost, configuration)
VALUES
('COMPLETED', '2024-01-15 07:55:00', '2024-01-15 07:56:30', 18.20, 145600, 'Solver: GUIDED_LOCAL_SEARCH | Manual Seed'),
('COMPLETED', '2024-01-16 08:10:00', '2024-01-16 08:11:45', 22.50, 180000, 'Solver: GUIDED_LOCAL_SEARCH | Auto'),
('FAILED', '2024-01-17 09:00:00', '2024-01-17 09:00:15', NULL, NULL, 'Solver: GUIDED_LOCAL_SEARCH | Timeout'),
('COMPLETED', '2024-01-18 07:45:00', '2024-01-18 07:47:00', 15.80, 126400, 'Solver: PATH_CHEAPEST_ARC | Manual'),
('PENDING', NULL, NULL, NULL, NULL, 'Solver: GUIDED_LOCAL_SEARCH | Queued'),
('COMPLETED', '2024-01-19 08:20:00', '2024-01-19 08:22:15', 28.40, 227200, 'Solver: GUIDED_LOCAL_SEARCH | Batch'),
('COMPLETED', '2024-01-20 07:30:00', '2024-01-20 07:32:00', 19.60, 156800, 'Solver: PATH_CHEAPEST_ARC | Express'),
('IN_PROGRESS', '2024-01-21 08:00:00', NULL, NULL, NULL, 'Solver: GUIDED_LOCAL_SEARCH | Running'),
('COMPLETED', '2024-01-22 09:15:00', '2024-01-22 09:17:30', 24.30, 194400, 'Solver: GUIDED_LOCAL_SEARCH | Standard'),
('COMPLETED', '2024-01-23 07:50:00', '2024-01-23 07:52:45', 21.10, 168800, 'Solver: PATH_CHEAPEST_ARC | Priority'),
('FAILED', '2024-01-24 10:00:00', '2024-01-24 10:00:10', NULL, NULL, 'Solver: GUIDED_LOCAL_SEARCH | Error');

-- =====================================================
-- 9. Routes (1 vehicle = 1 optimized route)
-- =====================================================

INSERT INTO routes (
    vehicle_id,
    routing_run_id,
    total_distance_km,
    total_duration_min,
    total_cost,
    status,
    polyline
)
VALUES
(1, 1, 18.20, 42, 145600, 'CREATED', 'u~l~Fdvf~Cj@`@_@r@m@v@y@x@aAbAaBfBeCfCcDfDgEhEiFfFmGfG'),
(2, 2, 22.50, 55, 180000, 'IN_PROGRESS', 'abc123defCj@`@_@r@m@v@y@x@aAbAaBfB'),
(4, 4, 15.80, 38, 126400, 'COMPLETED', 'xyz789abcCj@`@_@r@m@v@y@x@aAbAfB'),
(5, 6, 28.40, 68, 227200, 'CREATED', 'pqr456stuvCj@`@_@r@m@v@y@x@aAbA'),
(7, 7, 19.60, 48, 156800, 'IN_PROGRESS', 'lmn012opqCj@`@_@r@m@v@y@x@aAbAaBfB'),
(8, 9, 24.30, 58, 194400, 'CREATED', 'ghi345jklCj@`@_@r@m@v@y@x@aAbAaBfBeCfC'),
(10, 10, 21.10, 52, 168800, 'COMPLETED', 'def678ghiCj@`@_@r@m@v@y@x@aAbAaBfBeCfCcD'),
(13, 1, 16.50, 40, 132000, 'CREATED', 'mno901pqrCj@`@_@r@m@v@y@x@aAbAaBfBeCfCcDfD'),
(1, 6, 25.20, 62, 201600, 'IN_PROGRESS', 'stu234vwxCj@`@_@r@m@v@y@x@aAbAaBfBeCfCcDfDgE'),
(2, 9, 20.80, 50, 166400, 'CREATED', 'yza567bcdCj@`@_@r@m@v@y@x@aAbAaBfBeCfCcDfDgEhE'),
(4, 10, 23.60, 56, 188800, 'COMPLETED', 'efg890hijCj@`@_@r@m@v@y@x@aAbAaBfBeCfCcDfDgEhEiF');

-- =====================================================
-- 10. Route Stops (Delivery Sequence)
-- =====================================================

INSERT INTO route_stops (
    route_id,
    order_id,
    location_id,
    stop_sequence,
    distance_from_prev_km,
    duration_from_prev_min,
    arrival_time,
    departure_time
)
VALUES
-- Route 1 (vehicle 1, run 1)
(1, NULL, 2, 0, 0.00, 0, '2024-01-15 08:00:00', '2024-01-15 08:15:00'),
(1, 1, 2, 1, 5.20, 12, '2024-01-15 08:27:00', '2024-01-15 08:40:00'),
(1, 2, 1, 2, 6.40, 15, '2024-01-15 08:55:00', '2024-01-15 09:10:00'),
(1, 3, 2, 3, 6.60, 15, '2024-01-15 09:25:00', '2024-01-15 09:40:00'),
(1, NULL, 2, 4, 5.50, 20, '2024-01-15 10:00:00', NULL),

-- Route 2 (vehicle 2, run 2)
(2, NULL, 2, 0, 0.00, 0, '2024-01-16 08:00:00', '2024-01-16 08:10:00'),
(2, 4, 1, 1, 7.50, 18, '2024-01-16 08:28:00', '2024-01-16 08:45:00'),
(2, 5, 2, 2, 8.00, 19, '2024-01-16 09:04:00', '2024-01-16 09:20:00'),
(2, NULL, 2, 3, 7.00, 18, '2024-01-16 09:38:00', NULL),

-- Route 3 (vehicle 4, run 4)
(3, NULL, 2, 0, 0.00, 0, '2024-01-18 07:45:00', '2024-01-18 08:00:00'),
(3, 6, 1, 1, 5.80, 14, '2024-01-18 08:14:00', '2024-01-18 08:30:00'),
(3, NULL, 2, 2, 10.00, 24, '2024-01-18 08:54:00', NULL);

-- =====================================================
-- END OF SEED DATA
-- =====================================================
