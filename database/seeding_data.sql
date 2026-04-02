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

INSERT INTO dispatchers (username, password, email, full_name, role)
VALUES
('dispatcher01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher01@logitower.vn', 'Nguyễn Văn An', 'DISPATCHER'),
('dispatcher02', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher02@logitower.vn', 'Trần Thị Bích', 'DISPATCHER'),
('dispatcher03', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher03@logitower.vn', 'Lê Minh Châu', 'DISPATCHER'),
('dispatcher04', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher04@logitower.vn', 'Phạm Quốc Duy', 'DISPATCHER'),
('dispatcher05', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher05@logitower.vn', 'Hoàng Thu Hà', 'DISPATCHER'),
('dispatcher06', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher06@logitower.vn', 'Đỗ Văn Hùng', 'DISPATCHER'),
('dispatcher07', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher07@logitower.vn', 'Vũ Thị Lan', 'DISPATCHER'),
('dispatcher08', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher08@logitower.vn', 'Bùi Minh Nam', 'DISPATCHER'),
('dispatcher09', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher09@logitower.vn', 'Mai Thu Phương', 'DISPATCHER'),
('dispatcher10', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'dispatcher10@logitower.vn', 'Ngô Văn Quang', 'DISPATCHER'),
('admin01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'admin01@logitower.vn', 'Trần Thị Hương', 'ADMIN');

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
-- 4. Locations (58 real-world street-level locations)
-- =====================================================
INSERT INTO locations (street, city, country, latitude, longitude)
VALUES
('121 Hàng Bè, Hoàn Kiếm', 'Hà Nội', 'Việt Nam', 21.0333, 105.8525),
('48 Hàng Ngang', 'Hà Nội', 'Việt Nam', 21.0356, 105.8509),
('24 Hàng Bài', 'Hà Nội', 'Việt Nam', 21.0245, 105.8540),
('1 Tràng Tiền', 'Hà Nội', 'Việt Nam', 21.0248, 105.8575),
('36 Phố Huế', 'Hà Nội', 'Việt Nam', 21.0197, 105.8503),
('50 Lý Thường Kiệt', 'Hà Nội', 'Việt Nam', 21.0258, 105.8472),
('2 Lê Thái Tổ', 'Hà Nội', 'Việt Nam', 21.0288, 105.8523),
('58 Quang Trung', 'Hà Nội', 'Việt Nam', 21.0192, 105.8486),
('10 Nguyễn Du', 'Hà Nội', 'Việt Nam', 21.0180, 105.8475),
('20 Hai Bà Trưng', 'Hà Nội', 'Việt Nam', 21.0240, 105.8547),
('15 Phan Chu Trinh', 'Hà Nội', 'Việt Nam', 21.0246, 105.8570),
('90 Bà Triệu', 'Hà Nội', 'Việt Nam', 21.0198, 105.8520),
('200 Nguyễn Trãi', 'Hà Nội', 'Việt Nam', 20.9985, 105.8095),
('72 Tây Sơn', 'Hà Nội', 'Việt Nam', 21.0089, 105.8265),
('165 Xuân Thủy', 'Hà Nội', 'Việt Nam', 21.0362, 105.7835),
('334 Cầu Giấy', 'Hà Nội', 'Việt Nam', 21.0350, 105.7900),
('35 Trần Duy Hưng', 'Hà Nội', 'Việt Nam', 21.0100, 105.7980),
('250 Minh Khai', 'Hà Nội', 'Việt Nam', 21.0005, 105.8665),
('1 Phạm Văn Đồng', 'Hà Nội', 'Việt Nam', 21.0465, 105.7820),
('68 Nguyễn Chí Thanh', 'Hà Nội', 'Việt Nam', 21.0225, 105.8105),
('1 Nguyễn Huệ, Q1', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7745, 106.7030),
('135 Nguyễn Thị Minh Khai', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7790, 106.6950),
('10 Lê Duẩn', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7870, 106.7045),
('45 Lý Tự Trọng', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7765, 106.7005),
('50 Nguyễn Du', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7795, 106.7000),
('200 Lê Thánh Tôn', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7720, 106.6995),
('25 Tôn Đức Thắng', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7748, 106.7065),
('300 Điện Biên Phủ', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7840, 106.7030),
('15 Võ Văn Tần', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7730, 106.6895),
('100 Nguyễn Văn Cừ', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7590, 106.6820),
('500 Cách Mạng Tháng 8', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7705, 106.6700),
('120 Trần Hưng Đạo', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7655, 106.6960),
('86 Nguyễn Thái Học', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7650, 106.6935),
('12 Phạm Ngọc Thạch', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7820, 106.6925),
('400 Lê Văn Sỹ', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7910, 106.6740),
('90 Hoàng Văn Thụ', 'TP. Hồ Chí Minh', 'Việt Nam', 10.8000, 106.6665),
('50 Trường Chinh', 'TP. Hồ Chí Minh', 'Việt Nam', 10.8100, 106.6300),
('100 Phan Xích Long', 'TP. Hồ Chí Minh', 'Việt Nam', 10.8015, 106.6855),
('25 Nguyễn Oanh', 'TP. Hồ Chí Minh', 'Việt Nam', 10.8380, 106.6705),
('300 Quang Trung', 'TP. Hồ Chí Minh', 'Việt Nam', 10.8300, 106.6500),
('1 Lê Lợi, Huế', 'Huế', 'Việt Nam', 16.4635, 107.5900),
('10 Bạch Đằng, Đà Nẵng', 'Đà Nẵng', 'Việt Nam', 16.0678, 108.2240),
('100 Trần Phú, Nha Trang', 'Nha Trang', 'Việt Nam', 12.2380, 109.1960),
('50 Nguyễn Tất Thành, Quy Nhơn', 'Quy Nhơn', 'Việt Nam', 13.7800, 109.2190),
('1 Trần Hưng Đạo, Đà Lạt', 'Đà Lạt', 'Việt Nam', 11.9400, 108.4580),
('20 Hùng Vương, Buôn Ma Thuột', 'Buôn Ma Thuột', 'Việt Nam', 12.6670, 108.0500),
('15 Nguyễn Huệ, Cần Thơ', 'Cần Thơ', 'Việt Nam', 10.0455, 105.7465),
('10 Trần Phú, Hải Phòng', 'Hải Phòng', 'Việt Nam', 20.8655, 106.6820),
('50 Hùng Vương, Hạ Long', 'Hạ Long', 'Việt Nam', 20.9500, 107.0800),
('30 Lý Thường Kiệt, Nam Định', 'Nam Định', 'Việt Nam', 20.4200, 106.1700),
('100 Nguyễn Trãi, Thanh Hóa', 'Thanh Hóa', 'Việt Nam', 19.8060, 105.7850),
('200 Phan Bội Châu, Vinh', 'Vinh', 'Việt Nam', 18.6790, 105.6800),
('50 Nguyễn Du, Đồng Hới', 'Đồng Hới', 'Việt Nam', 17.4685, 106.6220),
('10 Trần Phú, Quảng Ngãi', 'Quảng Ngãi', 'Việt Nam', 15.1200, 108.8000),
('1 Nguyễn Huệ, Phan Thiết', 'Phan Thiết', 'Việt Nam', 10.9800, 108.2600),
('20 Trần Hưng Đạo, Rạch Giá', 'Rạch Giá', 'Việt Nam', 10.0120, 105.0800),
('15 Nguyễn Trung Trực, Cà Mau', 'Cà Mau', 'Việt Nam', 9.1760, 105.1500),
('100 30/4, Phú Quốc', 'Phú Quốc', 'Việt Nam', 10.2890, 103.9840);

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
('ORD-012', 10, 380, 1.9, NULL, 4, 'CREATED'),
('ORD-013', 11, 420, 2.1, NULL, 4, 'CREATED'),
('ORD-014', 6, 260, 1.4, NULL, 2, 'CREATED'),
('ORD-015', 7, 310, 1.8, NULL, 2, 'CREATED'),
('ORD-016', 8, 145, 0.9, NULL, 2, 'DELIVERED'),
('ORD-017', 9, 275, 1.5, NULL, 2, 'IN_TRANSIT'),
('ORD-018', 10, 360, 2.0, NULL, 4, 'CREATED'),
('ORD-019', 11, 410, 2.2, NULL, 4, 'CREATED'),
('ORD-020', 5, 230, 1.1, NULL, 1, 'CREATED'),
('ORD-021', 6, 340, 1.9, NULL, 1, 'CREATED'),
('ORD-022', 7, 510, 2.7, NULL, 1, 'CREATED'),
('ORD-023', 8, 295, 1.6, NULL, 1, 'CREATED'),
('ORD-024', 9, 165, 1.0, NULL, 1, 'DELIVERED'),
('ORD-025', 10, 205, 1.2, NULL, 4, 'IN_TRANSIT'),
('ORD-026', 11, 470, 2.5, NULL, 4, 'CREATED'),
('ORD-027', 5, 385, 2.0, NULL, 1, 'CREATED'),
('ORD-028', 6, 215, 1.2, NULL, 2, 'CREATED'),
('ORD-029', 7, 330, 1.8, NULL, 2, 'CREATED'),
('ORD-030', 8, 290, 1.5, NULL, 2, 'CREATED'),
('ORD-031', 9, 355, 1.9, NULL, 3, 'CREATED'),
('ORD-032', 10, 440, 2.3, NULL, 3, 'CREATED'),
('ORD-033', 11, 520, 2.9, NULL, 3, 'IN_TRANSIT'),
('ORD-034', 5, 245, 1.4, NULL, 1, 'CREATED'),
('ORD-035', 6, 315, 1.7, NULL, 1, 'CREATED'),
('ORD-036', 7, 365, 2.1, NULL, 2, 'CREATED'),
('ORD-037', 8, 185, 1.1, NULL, 2, 'DELIVERED'),
('ORD-038', 9, 205, 1.3, NULL, 3, 'CREATED'),
('ORD-039', 10, 395, 2.2, NULL, 3, 'CREATED'),
('ORD-040', 11, 455, 2.4, NULL, 4, 'CREATED'),
('ORD-041', 5, 275, 1.5, NULL, 1, 'IN_TRANSIT'),
('ORD-042', 6, 325, 1.8, NULL, 2, 'CREATED'),
('ORD-043', 7, 485, 2.6, NULL, 3, 'CREATED'),
('ORD-044', 8, 155, 0.8, NULL, 2, 'CREATED'),
('ORD-045', 9, 265, 1.4, NULL, 3, 'CREATED'),
('ORD-046', 10, 375, 2.0, NULL, 4, 'CREATED'),
('ORD-047', 11, 565, 3.0, NULL, 4, 'CREATED'),
('ORD-048', 5, 225, 1.2, NULL, 1, 'DELIVERED'),
('ORD-049', 6, 305, 1.6, NULL, 2, 'CREATED'),
('ORD-050', 7, 345, 1.9, NULL, 3, 'CREATED'),
('ORD-051', 8, 435, 2.3, NULL, 3, 'CREATED'),
('ORD-052', 9, 195, 1.1, NULL, 3, 'CREATED'),
('ORD-053', 10, 285, 1.5, NULL, 4, 'IN_TRANSIT'),
('ORD-054', 11, 495, 2.8, NULL, 4, 'CREATED'),
('ORD-055', 5, 255, 1.3, NULL, 1, 'CREATED'),
('ORD-056', 6, 365, 2.0, NULL, 2, 'CREATED'),
('ORD-057', 7, 415, 2.2, NULL, 3, 'CREATED'),
('ORD-058', 8, 175, 1.0, NULL, 2, 'CREATED'),
('ORD-059', 9, 285, 1.6, NULL, 3, 'DELIVERED'),
('ORD-060', 10, 405, 2.1, NULL, 4, 'CREATED');


-- =====================================================
-- END OF SEED DATA
-- =====================================================
