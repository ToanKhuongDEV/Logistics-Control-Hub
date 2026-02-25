-- =====================================================
-- SEED DATA - Logistics Control Hub MVP
-- =====================================================

-- =====================================================
-- 1. Companies
-- =====================================================

INSERT INTO companies (name, address, phone, email, website, tax_id, description)
VALUES
('LogiTower Hanoi', 'Phố Hàng Trống, Quận Hoàn Kiếm, Hà Nội', '+84 24 1234 5678', 'contact@logitower.vn', 'www.logitower.vn', '0123456789', 'Công ty logistics hàng đầu tại Thủ đô');
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
-- 4. Locations (11 unique locations in Hanoi)
-- =====================================================
INSERT INTO locations (street, city, country, latitude, longitude)
VALUES
('18 Đường lê Quang đạo', 'Hà Nội', 'Việt Nam', 21.013194, 105.770154),
('29 Liễu Giai, Phường Ngọc Khánh, Quận Ba Đình', 'Hà Nội', 'Việt Nam', 21.013194, 105.770154),
('Phố Duy Tân, Phường Dịch Vọng Hậu, Quận Cầu Giấy', 'Hà Nội', 'Việt Nam', 21.032221, 105.781664),
('Phố Lê Đức Thọ, Phường Mỹ Đình 2, Quận Nam Từ Liêm', 'Hà Nội', 'Việt Nam', 21.028511, 105.768314),
('Phố Nguyễn Văn Cừ, Phường Bồ Đề, Quận Long Biên', 'Hà Nội', 'Việt Nam', 21.047361, 105.886138),
('Phố Lạc Long Quân, Phường Xuân La, Quận Tây Hồ', 'Hà Nội', 'Việt Nam', 21.070228, 105.807059),
('Phố Trần Khát Chân, Phường Thanh Nhàn, Quận Hai Bà Trưng', 'Hà Nội', 'Việt Nam', 21.012679, 105.854041),
('Phố Chùa Bộc, Phường Quang Trung, Quận Đống Đa', 'Hà Nội', 'Việt Nam', 21.007206, 105.826187),
('Phố Nguyễn Trãi, Phường Thượng Đình, Quận Thanh Xuân', 'Hà Nội', 'Việt Nam', 20.998146, 105.815815),
('Phố Quang Trung, Phường Quang Trung, Quận Hà Đông', 'Hà Nội', 'Việt Nam', 20.969308, 105.777152),
('Số 1 Giải Phóng, Phường Giáp Bát, Quận Hoàng Mai', 'Hà Nội', 'Việt Nam', 20.979364, 105.841171);

-- =====================================================
-- 5. Depots (Updated to Hanoi locations)
-- =====================================================
INSERT INTO depots (name, location_id, description, is_active)
VALUES
('Kho Trung Tâm Hoàn Kiếm', 1, 'Kho chính tại khu vực Hoàn Kiếm - điểm xuất phát trung tâm cho các tuyến giao hàng nội thành', TRUE),
('Kho Ba Đình', 2, 'Kho phân phối khu vực Ba Đình - phục vụ khu hành chính trung tâm', TRUE),
('Kho Cầu Giấy', 3, 'Kho phân phối khu vực Cầu Giấy - phục vụ phía Tây Bắc nội thành', TRUE),
('Kho Mỹ Đình', 4, 'Kho logistics khu vực Mỹ Đình - Nam Từ Liêm', TRUE);

-- =====================================================
-- 6. Vehicles (All vehicles at Depot 1 - Kho Trung Tâm Hoàn Kiếm)
-- =====================================================

INSERT INTO vehicles (code, max_weight_kg, max_volume_m3, cost_per_km, status, type, driver_id, depot_id)
VALUES
('HDT-001', 6500, 35.5, 15000, 'ACTIVE', 'Hyundai Mighty', 1, 1),     
('MDT-002', 3500, 18.0, 6500, 'ACTIVE', 'Isuzu FRR', 2, 1),        
('LDT-003', 2000, 12.0, 4500, 'MAINTENANCE', 'Kia K250', 3, 2),     
('LDT-004', 3000, 16.5, 5500, 'ACTIVE', 'Hino XZU', 4, 1),           
('MDT-005', 4000, 20.0, 7000, 'ACTIVE', 'Isuzu FVR', 5, 1),           
('LDT-006', 1800, 11.0, 4000, 'IDLE', 'Thaco Kia K200', NULL, 2),    
('LDT-007', 2800, 14.5, 5200, 'ACTIVE', 'Hyundai HD120S', 6, 1),      
('LDT-008', 3200, 17.0, 6000, 'ACTIVE', 'Hino FC', 7, 1),            
('LDT-009', 2200, 13.0, 4800, 'MAINTENANCE', 'Kia K250', 8, 1),       
('MDT-010', 3800, 19.5, 6800, 'ACTIVE', 'Isuzu FVR', 9, 1),
('LDT-011', 2600, 15.0, 5100, 'ACTIVE', 'Hyundai Porter', 10, 2),
('LDT-012', 2900, 16.0, 5400, 'IDLE', 'Kia Frontier', NULL, 1),
('MDT-013', 4200, 21.0, 7200, 'ACTIVE', 'Hino FL', 11, 1),
('LDT-014', 2400, 14.0, 4900, 'MAINTENANCE', 'Thaco Ollin', NULL, 2);

-- =====================================================
-- 7. Orders (Delivery to Hanoi locations)
-- =====================================================

INSERT INTO orders (code, delivery_location_id, weight_kg, volume_m3, driver_id, depot_id, status)
VALUES
('ORD-001', 5, 200, 1.2, NULL, 1, 'CREATED'),
('ORD-002', 5, 300, 1.5, NULL, 1, 'CREATED'),
('ORD-003', 5, 400, 2.0, NULL, 1, 'CREATED'),
('ORD-004', 5, 150, 1.0, NULL, 1, 'IN_TRANSIT'),
('ORD-005', 5, 250, 1.3, NULL, 1, 'CREATED'),
('ORD-006', 5, 350, 1.8, NULL, 1, 'CREATED'),
('ORD-007', 5, 180, 1.1, NULL, 1, 'DELIVERED'),
('ORD-008', 6, 220, 1.4, NULL, 1, 'CREATED'),
('ORD-009', 7, 280, 1.6, NULL, 1, 'CREATED'),
('ORD-010', 8, 320, 1.7, NULL, 1, 'IN_TRANSIT'),
('ORD-011', 9, 190, 1.2, NULL, 1, 'CREATED'),
('ORD-012', 10, 380, 1.9, NULL, 4, 'CREATED');


-- =====================================================
-- END OF SEED DATA
-- =====================================================
