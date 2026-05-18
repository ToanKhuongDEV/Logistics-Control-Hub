-- =====================================================
-- SEED DATA - Logistics Control Hub
-- Generated from database/data_backup.sql for the current schema
-- Uses INSERT statements; refresh/password-reset tokens are intentionally not seeded
-- =====================================================

BEGIN;

-- =====================================================
-- Companies
-- =====================================================
INSERT INTO companies (id, name, address, phone, email, website, tax_id, description, created_at, updated_at, deleted)
VALUES
(1, 'LogiTower Hanoi', 'Phố Hàng Trống, Quận Hoàn Kiếm, Hà Nội', '+84 24 1234 5678', 'contact@logitower.vn', 'www.logitower.vn', '0123456789', 'Công ty logistics hàng đầu tại Thủ đô', '2026-05-15 14:18:19.90197+07', NULL, FALSE);

-- =====================================================
-- Drivers
-- =====================================================
INSERT INTO drivers (id, name, license_number, phone_number, email, created_at, updated_at, deleted)
VALUES
(1, 'Nguyễn Văn An', 'B2-001234', '0901234567', 'driver.an@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(2, 'Trần Thị Bình', 'C-005678', '0909876543', 'driver.binh@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(3, 'Lê Văn Cường', 'C-009101', '0905555555', 'driver.cuong@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(4, 'Phạm Thị Dung', 'B2-001122', '0908888888', 'driver.dung@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(5, 'Hoàng Văn Em', 'FC-003344', '0907777777', 'driver.em@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(6, 'Đỗ Thị Phương', 'C-002211', '0906666666', 'driver.phuong@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(7, 'Vũ Văn Giang', 'C-004455', '0904444444', 'driver.giang@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(8, 'Bùi Thị Hoa', 'B2-006677', '0903333333', 'driver.hoa@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(9, 'Mai Văn Ích', 'FC-008899', '0902222222', 'driver.ich@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(10, 'Ngô Thị Kiều', 'B2-009988', '0901111111', 'driver.kieu@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(11, 'Đinh Văn Lâm', 'C-007766', '0909999999', 'driver.lam@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(12, 'Lý Thị Mai', 'FC-005544', '0908877665', 'driver.mai@logitower.vn', '2026-05-15 14:18:19.90197+07', NULL, FALSE);

-- =====================================================
-- Users
-- =====================================================
INSERT INTO users (id, username, password, email, full_name, role, created_at, updated_at, deleted, driver_id)
VALUES
(1, 'user01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user01@logitower.vn', 'Nguyễn Văn An', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(2, 'user02', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user02@logitower.vn', 'Trần Thị Bích', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(3, 'user03', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user03@logitower.vn', 'Lê Minh Châu', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(4, 'user04', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user04@logitower.vn', 'Phạm Quốc Duy', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(5, 'user05', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user05@logitower.vn', 'Hoàng Thu Hà', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(6, 'user06', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user06@logitower.vn', 'Đỗ Văn Hùng', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(7, 'user07', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user07@logitower.vn', 'Vũ Thị Lan', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(8, 'user08', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user08@logitower.vn', 'Bùi Minh Nam', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(9, 'user09', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user09@logitower.vn', 'Mai Thu Phương', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(10, 'user10', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'user10@logitower.vn', 'Ngô Văn Quang', 'DISPATCHER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(11, 'admin01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'admin01@logitower.vn', 'Trần Thị Hương', 'ADMIN', '2026-05-15 14:18:19.90197+07', NULL, FALSE, NULL),
(12, 'driver01', '$2a$10$EnBIa50ATiitk.ir98E6ged3Eu7bH5rwCrsn9m4k7mPLE.wH9S.P6', 'driver01@logitower.vn', 'Vũ Văn Giang', 'DRIVER', '2026-05-15 14:18:19.90197+07', NULL, FALSE, 7);

-- =====================================================
-- Locations
-- =====================================================
INSERT INTO locations (id, street, city, country, latitude, longitude, created_at, updated_at, deleted)
VALUES
(1, '121 Hàng Bè, Hoàn Kiếm', 'Hà Nội', 'Việt Nam', 21.0333, 105.8525, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(2, '48 Hàng Ngang', 'Hà Nội', 'Việt Nam', 21.0356, 105.8509, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(3, '24 Hàng Bài', 'Hà Nội', 'Việt Nam', 21.0245, 105.854, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(4, '1 Tràng Tiền', 'Hà Nội', 'Việt Nam', 21.0248, 105.8575, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(5, '36 Phố Huế', 'Hà Nội', 'Việt Nam', 21.0197, 105.8503, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(6, '50 Lý Thường Kiệt', 'Hà Nội', 'Việt Nam', 21.0258, 105.8472, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(7, '2 Lê Thái Tổ', 'Hà Nội', 'Việt Nam', 21.0288, 105.8523, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(8, '58 Quang Trung', 'Hà Nội', 'Việt Nam', 21.0192, 105.8486, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(9, '10 Nguyễn Du', 'Hà Nội', 'Việt Nam', 21.018, 105.8475, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(10, '20 Hai Bà Trưng', 'Hà Nội', 'Việt Nam', 21.024, 105.8547, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(11, '15 Phan Chu Trinh', 'Hà Nội', 'Việt Nam', 21.0246, 105.857, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(12, '90 Bà Triệu', 'Hà Nội', 'Việt Nam', 21.0198, 105.852, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(13, '200 Nguyễn Trãi', 'Hà Nội', 'Việt Nam', 20.9985, 105.8095, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(14, '72 Tây Sơn', 'Hà Nội', 'Việt Nam', 21.0089, 105.8265, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(15, '165 Xuân Thủy', 'Hà Nội', 'Việt Nam', 21.0362, 105.7835, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(16, '334 Cầu Giấy', 'Hà Nội', 'Việt Nam', 21.035, 105.79, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(17, '35 Trần Duy Hưng', 'Hà Nội', 'Việt Nam', 21.01, 105.798, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(18, '250 Minh Khai', 'Hà Nội', 'Việt Nam', 21.0005, 105.8665, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(19, '1 Phạm Văn Đồng', 'Hà Nội', 'Việt Nam', 21.0465, 105.782, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(20, '68 Nguyễn Chí Thanh', 'Hà Nội', 'Việt Nam', 21.0225, 105.8105, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(21, '1 Nguyễn Huệ, Q1', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7745, 106.703, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(22, '135 Nguyễn Thị Minh Khai', 'TP. Hồ Chí Minh', 'Việt Nam', 10.779, 106.695, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(23, '10 Lê Duẩn', 'TP. Hồ Chí Minh', 'Việt Nam', 10.787, 106.7045, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(24, '45 Lý Tự Trọng', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7765, 106.7005, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(25, '50 Nguyễn Du', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7795, 106.7, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(26, '200 Lê Thánh Tôn', 'TP. Hồ Chí Minh', 'Việt Nam', 10.772, 106.6995, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(27, '25 Tôn Đức Thắng', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7748, 106.7065, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(28, '300 Điện Biên Phủ', 'TP. Hồ Chí Minh', 'Việt Nam', 10.784, 106.703, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(29, '15 Võ Văn Tần', 'TP. Hồ Chí Minh', 'Việt Nam', 10.773, 106.6895, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(30, '100 Nguyễn Văn Cừ', 'TP. Hồ Chí Minh', 'Việt Nam', 10.759, 106.682, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(31, '500 Cách Mạng Tháng 8', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7705, 106.67, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(32, '120 Trần Hưng Đạo', 'TP. Hồ Chí Minh', 'Việt Nam', 10.7655, 106.696, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(33, '86 Nguyễn Thái Học', 'TP. Hồ Chí Minh', 'Việt Nam', 10.765, 106.6935, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(34, '12 Phạm Ngọc Thạch', 'TP. Hồ Chí Minh', 'Việt Nam', 10.782, 106.6925, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(35, '400 Lê Văn Sỹ', 'TP. Hồ Chí Minh', 'Việt Nam', 10.791, 106.674, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(36, '90 Hoàng Văn Thụ', 'TP. Hồ Chí Minh', 'Việt Nam', 10.8, 106.6665, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(37, '50 Trường Chinh', 'TP. Hồ Chí Minh', 'Việt Nam', 10.81, 106.63, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(38, '100 Phan Xích Long', 'TP. Hồ Chí Minh', 'Việt Nam', 10.8015, 106.6855, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(39, '25 Nguyễn Oanh', 'TP. Hồ Chí Minh', 'Việt Nam', 10.838, 106.6705, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(40, '300 Quang Trung', 'TP. Hồ Chí Minh', 'Việt Nam', 10.83, 106.65, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(41, '1 Lê Lợi, Huế', 'Huế', 'Việt Nam', 16.4635, 107.59, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(42, '10 Bạch Đằng, Đà Nẵng', 'Đà Nẵng', 'Việt Nam', 16.0678, 108.224, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(43, '100 Trần Phú, Nha Trang', 'Nha Trang', 'Việt Nam', 12.238, 109.196, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(44, '50 Nguyễn Tất Thành, Quy Nhơn', 'Quy Nhơn', 'Việt Nam', 13.78, 109.219, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(45, '1 Trần Hưng Đạo, Đà Lạt', 'Đà Lạt', 'Việt Nam', 11.94, 108.458, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(46, '20 Hùng Vương, Buôn Ma Thuột', 'Buôn Ma Thuột', 'Việt Nam', 12.667, 108.05, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(47, '15 Nguyễn Huệ, Cần Thơ', 'Cần Thơ', 'Việt Nam', 10.0455, 105.7465, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(48, '10 Trần Phú, Hải Phòng', 'Hải Phòng', 'Việt Nam', 20.8655, 106.682, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(49, '50 Hùng Vương, Hạ Long', 'Hạ Long', 'Việt Nam', 20.95, 107.08, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(50, '30 Lý Thường Kiệt, Nam Định', 'Nam Định', 'Việt Nam', 20.42, 106.17, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(51, '100 Nguyễn Trãi, Thanh Hóa', 'Thanh Hóa', 'Việt Nam', 19.806, 105.785, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(52, '200 Phan Bội Châu, Vinh', 'Vinh', 'Việt Nam', 18.679, 105.68, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(53, '50 Nguyễn Du, Đồng Hới', 'Đồng Hới', 'Việt Nam', 17.4685, 106.622, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(54, '10 Trần Phú, Quảng Ngãi', 'Quảng Ngãi', 'Việt Nam', 15.12, 108.8, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(55, '1 Nguyễn Huệ, Phan Thiết', 'Phan Thiết', 'Việt Nam', 10.98, 108.26, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(56, '20 Trần Hưng Đạo, Rạch Giá', 'Rạch Giá', 'Việt Nam', 10.012, 105.08, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(57, '15 Nguyễn Trung Trực, Cà Mau', 'Cà Mau', 'Việt Nam', 9.176, 105.15, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(58, '100 30/4, Phú Quốc', 'Phú Quốc', 'Việt Nam', 10.289, 103.984, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(59, '36 Trần Đăng Ninh', 'Hà Nội', 'Việt Nam', 20.964676, 105.769153, '2026-05-18 09:06:51.958277+07', NULL, FALSE),
(60, '311 Hai Bà Trưng', 'Hà Nội', 'Việt Nam', 21.014946, 105.852063, '2026-05-18 09:07:24.237942+07', NULL, FALSE),
(61, '35  Phan Chu Trinh', 'Hà Nội', 'Việt Nam', 21.02084, 105.855857, '2026-05-18 09:07:38.588072+07', NULL, FALSE),
(62, '31 Giảng Võ', 'Hà Nội', 'Việt Nam', 21.032247, 105.828264, '2026-05-18 09:08:05.792071+07', NULL, FALSE),
(63, '35 Nguyễn Chí Thanh', 'Hà Nội', 'Việt Nam', 21.027454, 105.812384, '2026-05-18 09:08:34.975161+07', NULL, FALSE),
(64, '112 Nguyễn Chí Thanh', 'Hà Nội', 'Việt Nam', 21.028462, 105.812447, '2026-05-18 09:09:01.40269+07', NULL, FALSE);

-- =====================================================
-- Depots
-- =====================================================
INSERT INTO depots (id, name, location_id, dispatcher_id, description, is_active, created_at, updated_at, deleted)
VALUES
(1, 'Kho Trung Tâm Hoàn Kiếm', 1, NULL, 'Kho chính tại khu vực Hoàn Kiếm - điểm xuất phát trung tâm cho các tuyến giao hàng nội thành', TRUE, '2026-05-15 14:18:19.90197+07', '2026-05-15 14:37:22.723418+07', FALSE),
(2, 'Kho Ba Đình', 2, 1, 'Kho phân phối khu vực Ba Đình - phục vụ khu hành chính trung tâm', TRUE, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(3, 'Kho Cầu Giấy', 3, 2, 'Kho phân phối khu vực Cầu Giấy - phục vụ phía Tây Bắc nội thành', TRUE, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(4, 'Kho Mỹ Đình', 4, 3, 'Kho logistics khu vực Mỹ Đình - Nam Từ Liêm', TRUE, '2026-05-15 14:18:19.90197+07', NULL, FALSE);

-- =====================================================
-- Vehicles
-- =====================================================
INSERT INTO vehicles (id, code, max_weight_kg, max_volume_m3, cost_per_km, status, type, driver_id, depot_id, created_at, updated_at, deleted)
VALUES
(1, 'HDT-001', 6500, 35.50, 15000.00, 'ACTIVE', 'Hyundai Mighty', 1, 1, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(2, 'MDT-002', 3500, 18.00, 6500.00, 'ACTIVE', 'Isuzu FRR', 2, 1, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(3, 'LDT-003', 2000, 12.00, 4500.00, 'MAINTENANCE', 'Kia K250', 3, 2, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(4, 'LDT-004', 3000, 16.50, 5500.00, 'ACTIVE', 'Hino XZU', 4, 1, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(5, 'MDT-005', 4000, 20.00, 7000.00, 'ACTIVE', 'Isuzu FVR', 5, 1, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(6, 'LDT-006', 1800, 11.00, 4000.00, 'IDLE', 'Thaco Kia K200', NULL, 2, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(7, 'LDT-007', 2800, 14.50, 5200.00, 'ACTIVE', 'Hyundai HD120S', 6, 1, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(8, 'LDT-008', 3200, 17.00, 6000.00, 'ACTIVE', 'Hino FC', 7, 4, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(9, 'LDT-009', 2200, 13.00, 4800.00, 'MAINTENANCE', 'Kia K250', 8, 1, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(10, 'MDT-010', 3800, 19.50, 6800.00, 'ACTIVE', 'Isuzu FVR', 9, 4, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(11, 'LDT-011', 2600, 15.00, 5100.00, 'ACTIVE', 'Hyundai Porter', 10, 2, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(12, 'LDT-012', 2900, 16.00, 5400.00, 'IDLE', 'Kia Frontier', NULL, 1, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(13, 'MDT-013', 4200, 21.00, 7200.00, 'ACTIVE', 'Hino FL', 11, 1, '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(14, 'LDT-014', 2400, 14.00, 4900.00, 'MAINTENANCE', 'Thaco Ollin', NULL, 2, '2026-05-15 14:18:19.90197+07', NULL, FALSE);

-- =====================================================
-- Orders
-- =====================================================
INSERT INTO orders (id, code, delivery_location_id, weight_kg, volume_m3, driver_id, depot_id, status, created_at, updated_at, deleted)
VALUES
(1, 'ORD-001', 59, 200, 1.20, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(2, 'ORD-002', 5, 300, 1.50, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(3, 'ORD-003', 5, 400, 2.00, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(4, 'ORD-004', 5, 150, 1.00, NULL, 1, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(5, 'ORD-005', 5, 250, 1.30, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(6, 'ORD-006', 5, 350, 1.80, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(7, 'ORD-007', 5, 180, 1.10, NULL, 1, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(8, 'ORD-008', 6, 220, 1.40, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(9, 'ORD-009', 7, 280, 1.60, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(10, 'ORD-010', 8, 320, 1.70, NULL, 1, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(11, 'ORD-011', 9, 190, 1.20, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(12, 'ORD-012', 10, 380, 1.90, 7, 4, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(13, 'ORD-013', 11, 420, 2.10, 7, 4, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(14, 'ORD-014', 6, 260, 1.40, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(15, 'ORD-015', 7, 310, 1.80, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(16, 'ORD-016', 8, 145, 0.90, NULL, 2, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(17, 'ORD-017', 9, 275, 1.50, NULL, 2, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(18, 'ORD-018', 60, 360, 2.00, NULL, 4, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(19, 'ORD-019', 61, 410, 2.20, 7, 4, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(20, 'ORD-020', 5, 230, 1.10, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(21, 'ORD-021', 6, 340, 1.90, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(22, 'ORD-022', 7, 510, 2.70, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(23, 'ORD-023', 8, 295, 1.60, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(24, 'ORD-024', 9, 165, 1.00, NULL, 1, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(25, 'ORD-025', 10, 205, 1.20, NULL, 4, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(26, 'ORD-026', 64, 470, 2.50, 7, 4, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(27, 'ORD-027', 5, 385, 2.00, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(28, 'ORD-028', 6, 215, 1.20, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(29, 'ORD-029', 7, 330, 1.80, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(30, 'ORD-030', 8, 290, 1.50, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(31, 'ORD-031', 9, 355, 1.90, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(32, 'ORD-032', 10, 440, 2.30, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(33, 'ORD-033', 11, 520, 2.90, NULL, 3, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(34, 'ORD-034', 5, 245, 1.40, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(35, 'ORD-035', 6, 315, 1.70, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(36, 'ORD-036', 7, 365, 2.10, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(37, 'ORD-037', 8, 185, 1.10, NULL, 2, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(38, 'ORD-038', 9, 205, 1.30, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(39, 'ORD-039', 10, 395, 2.20, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(40, 'ORD-040', 11, 455, 2.40, 7, 4, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(41, 'ORD-041', 5, 275, 1.50, NULL, 1, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(42, 'ORD-042', 6, 325, 1.80, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(43, 'ORD-043', 7, 485, 2.60, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(44, 'ORD-044', 8, 155, 0.80, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(45, 'ORD-045', 9, 265, 1.40, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(46, 'ORD-046', 10, 375, 2.00, NULL, 4, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(47, 'ORD-047', 62, 565, 3.00, NULL, 4, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(48, 'ORD-048', 5, 225, 1.20, NULL, 1, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(49, 'ORD-049', 6, 305, 1.60, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(50, 'ORD-050', 7, 345, 1.90, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(51, 'ORD-051', 8, 435, 2.30, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(52, 'ORD-052', 9, 195, 1.10, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(53, 'ORD-053', 10, 285, 1.50, NULL, 4, 'IN_TRANSIT', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(54, 'ORD-054', 11, 495, 2.80, NULL, 4, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(55, 'ORD-055', 5, 255, 1.30, NULL, 1, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(56, 'ORD-056', 6, 365, 2.00, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(57, 'ORD-057', 7, 415, 2.20, NULL, 3, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(58, 'ORD-058', 8, 175, 1.00, NULL, 2, 'CREATED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(59, 'ORD-059', 9, 285, 1.60, NULL, 3, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE),
(60, 'ORD-060', 63, 405, 2.10, NULL, 4, 'DELIVERED', '2026-05-15 14:18:19.90197+07', NULL, FALSE);

-- =====================================================
-- Routing Runs
-- =====================================================
INSERT INTO routing_runs (id, status, start_time, end_time, total_distance_km, total_cost, configuration, depot_id, created_at, updated_at, deleted)
VALUES
(1, 'COMPLETED', '2026-05-18 02:10:21.755799', '2026-05-18 02:10:21.766433', 12.65, 75876.00, 'Solver: GUIDED_LOCAL_SEARCH | Strategy: PATH_CHEAPEST_ARC | TimeLimit: 5s | SolveTime: 5014ms | PhysicalVehicles: 2 | VirtualVehicles: 6 | RoutesUsed: 1 | FixedCost: 50000', 4, '2026-05-18 09:10:21.772591+07', NULL, FALSE);

-- =====================================================
-- Routes
-- =====================================================
INSERT INTO routes (id, vehicle_id, routing_run_id, total_distance_km, total_duration_min, total_cost, status, polyline, created_at, updated_at, deleted)
VALUES
(1, 8, 1, 12.65, 21, 75876.00, 'CREATED', 'gli_C_hbeSd@`@VZ@F@HDFQJ]RyA\q@Nk@La@HQDEPc@dCG\PDpBb@NDRDr@PXF\FfAVz@P\JRDZHIb@{@nE[zAG\]GyDk@]GMd@k@pCYtAKh@If@m@`DCPUhAEREX_@lBi@tCMv@SbACPI`@GVc@~Bq@rDEZGVaAhFObAEXSXKVKf@CJCLERENIRKJ{@|@MFy@v@QPy@v@KNa@`@c@`@ENk@h@k@j@YVi@h@Ip@E`@Ir@y@pHEVCRCV_@bDSnB[rCETCVK|@Kz@AFGr@Il@AHUnBEj@Gh@E`@QvAALGb@APERc@~DMnACTE\CXg@zEJp@H|@Ff@BF@RJdABP@JHl@Hv@TvARxAD^BXFf@@H@Ld@nDD^TpB@NZlFZtEOl@BTNdB@JCh@?VBTRjCFt@BRPxBFt@Fp@ZdEBR@JPbCBVB\RnC@`@BNBT@FWfAOz@OnACPa@vBEVY~AIXAFALMl@\HLDb@Hv@Pb@NPHNRFJP@HBDCDEN?\BTBzDp@vANf@?zBZ`@Fj@FbB\F@LUGAAAYGsASc@IWEwAWq@Mq@Aq@A}AU{AWwCo@kB_@wGyA{@Qy@c@q@MMEy@Si@OUGETEPTDtBd@t@Lv@Vp@NdATf@Jh@JXFRD\HJo@BK@GlAeHT_CDsAl@u@?K?MU_DASYcEAKg@wGAQSwCM_BEi@Ca@C]CWEc@Iu@Gi@Gq@IgAM}AG}@CYGu@ImAKqAM_BGo@G_@i@eEE_@o@wFOgAEc@AUA[Ds@@C^uBF[Jk@DUHa@VsAHg@P{@f@oCXyAF_@F]t@yDJe@Jk@~@}EHc@Jk@H_@DSBMDOd@gCF]DSbAmFHg@Je@Z_B`@uBF]P}@^kBf@kCDYl@@jA?jB@P?n@?fA@pABX?fB?B@FULq@ZaB\kBLs@FUDU\gBJi@Ns@Jg@XyABUDW@Eb@wBHe@BKBSF]^aB@ETiAP{@BMJi@f@}BJg@TcADc@D]nA}FHg@b@H~@TtEbARFDS|@sEBK`@qBH_@DYl@yC?AVqAF]H]P_AhAuFHc@_@IICkBc@QEKCy@Sm@MSEa@IQE_Cg@uBe@_@K]KeAWk@OGC]K_@KBE@G?G?GAEEIIEICG?IBGBCBAFABAFW[e@a@', '2026-05-18 09:10:21.779248+07', NULL, FALSE);

-- =====================================================
-- Route Stops
-- =====================================================
INSERT INTO route_stops (id, route_id, order_id, location_id, stop_sequence, distance_from_prev_km, duration_from_prev_min, arrival_time, departure_time, created_at, updated_at, deleted)
VALUES
(1, 1, NULL, 4, 0, 0.00, 0, NULL, NULL, '2026-05-18 09:10:16.09475+07', NULL, FALSE),
(2, 1, 13, 11, 1, 0.07, 1, NULL, NULL, '2026-05-18 09:10:16.09475+07', NULL, FALSE),
(3, 1, 40, 11, 2, 0.07, 1, NULL, NULL, '2026-05-18 09:10:16.09475+07', NULL, FALSE),
(4, 1, 12, 10, 3, 0.51, 1, NULL, NULL, '2026-05-18 09:10:16.09475+07', NULL, FALSE),
(5, 1, 26, 64, 4, 5.53, 8, NULL, NULL, '2026-05-18 09:10:16.09475+07', NULL, FALSE),
(6, 1, 19, 61, 5, 6.02, 9, NULL, NULL, '2026-05-18 09:10:16.09475+07', NULL, FALSE),
(7, 1, NULL, 4, 6, 0.51, 2, NULL, NULL, '2026-05-18 09:10:16.09475+07', NULL, FALSE);

-- =====================================================
-- Audit Logs
-- =====================================================
INSERT INTO audit_logs (id, actor_user_id, actor_username, actor_role, action, resource_type, resource_id, resource_name, scope_depot_id, status, message, before_data, after_data, metadata, ip_address, user_agent, request_id, created_at, updated_at, deleted)
VALUES
(1, 11, 'admin01', 'ADMIN', 'LOGIN', 'AUTH', '11', 'admin01', NULL, 'SUCCESS', 'Dang nhap thanh cong', NULL, NULL, '{"username": "admin01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'ec8f512f-2866-44b6-a1e0-1b4829f2cd82', '2026-05-15 14:27:30.484651+07', '2026-05-15 14:27:30.484651+07', FALSE),
(2, 11, 'admin01', 'ADMIN', 'LOGIN', 'AUTH', '11', 'admin01', NULL, 'SUCCESS', 'Dang nhap thanh cong', NULL, NULL, '{"username": "admin01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '5f72a996-17ea-4619-a114-0de2dd168e17', '2026-05-15 14:35:07.469762+07', '2026-05-15 14:35:07.469762+07', FALSE),
(3, 11, 'admin01', 'ADMIN', 'UPDATE', 'USER', '1', 'user01', 1, 'SUCCESS', 'Updated employee account', '{"id": 1, "role": "DISPATCHER", "email": "user01@logitower.vn", "fullName": "Nguyễn Văn An", "username": "user01", "assignedDepotIds": [1, 2]}'::jsonb, '{"id": 1, "role": "DISPATCHER", "email": "user01@logitower.vn", "fullName": "Nguyễn Văn An", "username": "user01", "assignedDepotIds": [1, 2]}'::jsonb, '{"role": "DISPATCHER", "updatedFields": ["assignedDepotIds"]}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '32096794-4a4f-446b-ad01-413a78224b64', '2026-05-15 14:37:22.735391+07', '2026-05-15 14:37:22.739916+07', FALSE),
(4, 11, 'admin01', 'ADMIN', 'LOGIN', 'AUTH', '11', 'admin01', NULL, 'SUCCESS', 'Dang nhap thanh cong', NULL, NULL, '{"username": "admin01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'f9c3cdbe-72d3-40a0-a832-d779d87eacb5', '2026-05-18 08:42:01.076984+07', NULL, FALSE),
(5, 11, 'admin01', 'ADMIN', 'LOGOUT', 'AUTH', '11', 'admin01', NULL, 'SUCCESS', 'Dang xuat thanh cong', NULL, NULL, '{"username": "admin01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '788a9ee9-1854-49c7-b9e1-288a72225705', '2026-05-18 08:44:20.876059+07', NULL, FALSE),
(6, 1, 'user01', 'DISPATCHER', 'LOGIN', 'AUTH', '1', 'user01', 2, 'SUCCESS', 'Dang nhap thanh cong', NULL, NULL, '{"username": "user01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'ea531e4f-894a-4b46-9a0c-fa3e4d78f682', '2026-05-18 08:44:23.043051+07', NULL, FALSE),
(7, 1, 'user01', 'DISPATCHER', 'LOGOUT', 'AUTH', '1', 'user01', 2, 'SUCCESS', 'Dang xuat thanh cong', NULL, NULL, '{"username": "user01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '7f3143c9-7e9a-4a4d-90d5-b01fc053ba50', '2026-05-18 08:45:05.393736+07', NULL, FALSE),
(8, 11, 'admin01', 'ADMIN', 'LOGIN', 'AUTH', '11', 'admin01', NULL, 'SUCCESS', 'Dang nhap thanh cong', NULL, NULL, '{"username": "admin01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'ee6c0728-32c3-4429-aac4-e21a427b02cc', '2026-05-18 08:45:07.767727+07', NULL, FALSE),
(9, 11, 'admin01', 'ADMIN', 'LOGIN', 'AUTH', '11', 'admin01', NULL, 'SUCCESS', 'Dang nhap thanh cong', NULL, NULL, '{"username": "admin01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'ccc4fe07-341f-4c06-bf34-e8b00d988b77', '2026-05-18 08:53:36.148469+07', NULL, FALSE),
(10, 11, 'admin01', 'ADMIN', 'UPDATE', 'ORDER', '1', 'ORD-001', 1, 'SUCCESS', 'Updated order', '{"id": 1, "code": "ORD-001", "status": "CREATED", "depotId": 1, "driverId": null, "volumeM3": 1.2, "weightKg": 200, "depotName": "Kho Trung Tâm Hoàn Kiếm", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "36 Phố Huế", "country": "Việt Nam"}, "deliveryLocationId": 5}'::jsonb, '{"id": 1, "code": "ORD-001", "status": "CREATED", "depotId": 1, "driverId": null, "volumeM3": 1.2, "weightKg": 200, "depotName": "Kho Trung Tâm Hoàn Kiếm", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "36 Trần Đăng Ninh", "country": "Việt Nam"}, "deliveryLocationId": 59}'::jsonb, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'ce80ee87-931c-4c8d-9f6e-6f5e02d3ce04', '2026-05-18 09:06:53.876374+07', NULL, FALSE),
(11, 11, 'admin01', 'ADMIN', 'UPDATE', 'ORDER', '18', 'ORD-018', 4, 'SUCCESS', 'Updated order', '{"id": 18, "code": "ORD-018", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2, "weightKg": 360, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "20 Hai Bà Trưng", "country": "Việt Nam"}, "deliveryLocationId": 10}'::jsonb, '{"id": 18, "code": "ORD-018", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2, "weightKg": 360, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "311 Hai Bà Trưng", "country": "Việt Nam"}, "deliveryLocationId": 60}'::jsonb, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '5b8eb362-d44f-4f29-bc70-277e613f5e23', '2026-05-18 09:07:25.856575+07', NULL, FALSE),
(12, 11, 'admin01', 'ADMIN', 'UPDATE', 'ORDER', '19', 'ORD-019', 4, 'SUCCESS', 'Updated order', '{"id": 19, "code": "ORD-019", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2.2, "weightKg": 410, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "15 Phan Chu Trinh", "country": "Việt Nam"}, "deliveryLocationId": 11}'::jsonb, '{"id": 19, "code": "ORD-019", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2.2, "weightKg": 410, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "35  Phan Chu Trinh", "country": "Việt Nam"}, "deliveryLocationId": 61}'::jsonb, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'b737680c-a962-4420-8a5d-94e8dceaae72', '2026-05-18 09:07:40.646613+07', NULL, FALSE),
(13, 11, 'admin01', 'ADMIN', 'UPDATE', 'ORDER', '47', 'ORD-047', 4, 'SUCCESS', 'Updated order', '{"id": 47, "code": "ORD-047", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 3, "weightKg": 565, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "15 Phan Chu Trinh", "country": "Việt Nam"}, "deliveryLocationId": 11}'::jsonb, '{"id": 47, "code": "ORD-047", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 3, "weightKg": 565, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "31 Giảng Võ", "country": "Việt Nam"}, "deliveryLocationId": 62}'::jsonb, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '810225a9-b760-4e61-ad3c-5e47a70ae370', '2026-05-18 09:08:07.691383+07', NULL, FALSE),
(14, 11, 'admin01', 'ADMIN', 'UPDATE', 'ORDER', '60', 'ORD-060', 4, 'SUCCESS', 'Updated order', '{"id": 60, "code": "ORD-060", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2.1, "weightKg": 405, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "20 Hai Bà Trưng", "country": "Việt Nam"}, "deliveryLocationId": 10}'::jsonb, '{"id": 60, "code": "ORD-060", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2.1, "weightKg": 405, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "35 Nguyễn Chí Thanh", "country": "Việt Nam"}, "deliveryLocationId": 63}'::jsonb, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '99be9ac3-4ab1-4fbc-ac14-5b924d211345', '2026-05-18 09:08:37.073476+07', NULL, FALSE),
(15, 11, 'admin01', 'ADMIN', 'UPDATE', 'ORDER', '26', 'ORD-026', 4, 'SUCCESS', 'Updated order', '{"id": 26, "code": "ORD-026", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2.5, "weightKg": 470, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "15 Phan Chu Trinh", "country": "Việt Nam"}, "deliveryLocationId": 11}'::jsonb, '{"id": 26, "code": "ORD-026", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2.5, "weightKg": 470, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "112 Nguyễn Chí Thanh", "country": "Việt Nam"}, "deliveryLocationId": 64}'::jsonb, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '565215c7-4ba9-45fe-94d4-c5d12196b539', '2026-05-18 09:09:03.397461+07', NULL, FALSE),
(16, 11, 'admin01', 'ADMIN', 'BULK_UPDATE', 'ORDER', '5', 'Bulk order status update', 4, 'SUCCESS', 'Bulk updated order status', '[{"id": 46, "code": "ORD-046", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2, "weightKg": 375, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "20 Hai Bà Trưng", "country": "Việt Nam"}, "deliveryLocationId": 10}, {"id": 54, "code": "ORD-054", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2.8, "weightKg": 495, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "15 Phan Chu Trinh", "country": "Việt Nam"}, "deliveryLocationId": 11}, {"id": 18, "code": "ORD-018", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2, "weightKg": 360, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "311 Hai Bà Trưng", "country": "Việt Nam"}, "deliveryLocationId": 60}, {"id": 47, "code": "ORD-047", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 3, "weightKg": 565, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "31 Giảng Võ", "country": "Việt Nam"}, "deliveryLocationId": 62}, {"id": 60, "code": "ORD-060", "status": "CREATED", "depotId": 4, "driverId": null, "volumeM3": 2.1, "weightKg": 405, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "35 Nguyễn Chí Thanh", "country": "Việt Nam"}, "deliveryLocationId": 63}]'::jsonb, '[{"id": 46, "code": "ORD-046", "status": "DELIVERED", "depotId": 4, "driverId": null, "volumeM3": 2, "weightKg": 375, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "20 Hai Bà Trưng", "country": "Việt Nam"}, "deliveryLocationId": 10}, {"id": 54, "code": "ORD-054", "status": "DELIVERED", "depotId": 4, "driverId": null, "volumeM3": 2.8, "weightKg": 495, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "15 Phan Chu Trinh", "country": "Việt Nam"}, "deliveryLocationId": 11}, {"id": 18, "code": "ORD-018", "status": "DELIVERED", "depotId": 4, "driverId": null, "volumeM3": 2, "weightKg": 360, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "311 Hai Bà Trưng", "country": "Việt Nam"}, "deliveryLocationId": 60}, {"id": 47, "code": "ORD-047", "status": "DELIVERED", "depotId": 4, "driverId": null, "volumeM3": 3, "weightKg": 565, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "31 Giảng Võ", "country": "Việt Nam"}, "deliveryLocationId": 62}, {"id": 60, "code": "ORD-060", "status": "DELIVERED", "depotId": 4, "driverId": null, "volumeM3": 2.1, "weightKg": 405, "depotName": "Kho Mỹ Đình", "driverName": null, "deliveryLocation": {"city": "Hà Nội", "street": "35 Nguyễn Chí Thanh", "country": "Việt Nam"}, "deliveryLocationId": 63}]'::jsonb, '{"status": "DELIVERED", "orderIds": [60, 54, 47, 46, 18]}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '786c4436-39f1-4088-bb5e-d17397767906', '2026-05-18 09:09:27.138767+07', NULL, FALSE),
(17, 11, 'admin01', 'ADMIN', 'EXECUTE', 'ROUTING_RUN', NULL, 'Routing execution', 4, 'FAILED', 'Không tìm thấy phương tiện nào phù hợp (đang hoạt động và có tài xế) để tối ưu.', NULL, NULL, '{"depotId": 4, "exceptionType": "ValidationException"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'a2aa67aa-0f2f-4009-8504-6fb11dd8eaf9', '2026-05-18 09:09:35.460223+07', NULL, FALSE),
(18, 11, 'admin01', 'ADMIN', 'BULK_UPDATE', 'VEHICLE', '2', 'Vehicle depot reassignment', 4, 'SUCCESS', 'Bulk updated vehicle depot', '[{"id": 8, "code": "LDT-008", "type": "Hino FC", "status": "ACTIVE", "depotId": 1, "driverId": 7, "costPerKm": 6000, "depotName": "Kho Trung Tâm Hoàn Kiếm", "driverName": "Vũ Văn Giang", "maxVolumeM3": 17, "maxWeightKg": 3200}, {"id": 10, "code": "MDT-010", "type": "Isuzu FVR", "status": "ACTIVE", "depotId": 1, "driverId": 9, "costPerKm": 6800, "depotName": "Kho Trung Tâm Hoàn Kiếm", "driverName": "Mai Văn Ích", "maxVolumeM3": 19.5, "maxWeightKg": 3800}]'::jsonb, '[{"id": 8, "code": "LDT-008", "type": "Hino FC", "status": "ACTIVE", "depotId": 4, "driverId": 7, "costPerKm": 6000, "depotName": "Kho Mỹ Đình", "driverName": "Vũ Văn Giang", "maxVolumeM3": 17, "maxWeightKg": 3200}, {"id": 10, "code": "MDT-010", "type": "Isuzu FVR", "status": "ACTIVE", "depotId": 4, "driverId": 9, "costPerKm": 6800, "depotName": "Kho Mỹ Đình", "driverName": "Mai Văn Ích", "maxVolumeM3": 19.5, "maxWeightKg": 3800}]'::jsonb, '{"depotId": 4, "vehicleIds": [10, 8]}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '886022c7-fe10-4431-8914-3039c4552327', '2026-05-18 09:10:08.319102+07', NULL, FALSE),
(19, 11, 'admin01', 'ADMIN', 'EXECUTE', 'ROUTING_RUN', '1', 'Kho Mỹ Đình', 4, 'SUCCESS', 'Executed routing optimization', NULL, '{"id": 1, "status": "COMPLETED", "depotId": 4, "endTime": "2026-05-18T09:10:21.7664333", "depotName": "Kho Mỹ Đình", "startTime": "2026-05-18T09:10:21.7557991", "totalCost": 75876, "routeCount": 1, "configuration": "Solver: GUIDED_LOCAL_SEARCH | Strategy: PATH_CHEAPEST_ARC | TimeLimit: 5s | SolveTime: 5014ms | PhysicalVehicles: 2 | VirtualVehicles: 6 | RoutesUsed: 1 | FixedCost: 50000", "totalDistanceKm": 12.646}'::jsonb, '{"orderIds": [12, 13, 40, 19, 26], "orderCount": 5, "vehicleIds": [8, 10], "vehicleCount": 2}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', '1dd67b17-d958-4052-907d-d2c7b5ae0270', '2026-05-18 09:10:21.793722+07', NULL, FALSE),
(20, 11, 'admin01', 'ADMIN', 'LOGIN', 'AUTH', '11', 'admin01', NULL, 'SUCCESS', 'Dang nhap thanh cong', NULL, NULL, '{"username": "admin01"}'::jsonb, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', 'd889c92b-c926-44f5-b223-3f4f9d07d2fa', '2026-05-18 14:55:44.748232+07', '2026-05-18 14:55:44.748232+07', FALSE);

-- =====================================================
-- Sequence values
-- =====================================================
SELECT pg_catalog.setval('public.companies_id_seq', 1, true);
SELECT pg_catalog.setval('public.drivers_id_seq', 12, true);
SELECT pg_catalog.setval('public.users_id_seq', 12, true);
SELECT pg_catalog.setval('public.locations_id_seq', 64, true);
SELECT pg_catalog.setval('public.depots_id_seq', 4, true);
SELECT pg_catalog.setval('public.vehicles_id_seq', 14, true);
SELECT pg_catalog.setval('public.orders_id_seq', 60, true);
SELECT pg_catalog.setval('public.routing_runs_id_seq', 1, true);
SELECT pg_catalog.setval('public.routes_id_seq', 1, true);
SELECT pg_catalog.setval('public.route_stops_id_seq', 7, true);
SELECT pg_catalog.setval('public.audit_logs_id_seq', 20, true);
SELECT pg_catalog.setval('public.refresh_tokens_id_seq', 1, false);
SELECT pg_catalog.setval('public.password_reset_tokens_id_seq', 1, false);

COMMIT;

-- END OF SCRIPT
