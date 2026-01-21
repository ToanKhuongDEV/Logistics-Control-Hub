-- =====================================================
-- AI Supply Chain Control Tower - Database Schema Update v2
-- PostgreSQL Script
-- =====================================================
-- Script cập nhật schema với các cải thiện:
-- 1. Thêm bảng customers
-- 2. Fix kiểu dữ liệu operating time cho depots
-- 3. Thêm pickup time window cho delivery_orders
-- 4. Thêm bảng distance_matrix cho caching
-- 5. Thêm constraint cho route_stops
-- =====================================================

-- =====================================================
-- 1. THÊM BẢNG CUSTOMERS
-- =====================================================

CREATE TABLE IF NOT EXISTS customers (
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

CREATE INDEX IF NOT EXISTS idx_customer_name ON customers(name);
CREATE INDEX IF NOT EXISTS idx_customer_active ON customers(active);

COMMENT ON TABLE customers IS 'Khách hàng - người đặt đơn giao hàng';


-- =====================================================
-- 2. THÊM FK CHO delivery_orders.customer_id
-- =====================================================

-- Thêm FK constraint (chỉ chạy nếu chưa có)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_order_customer' 
        AND table_name = 'delivery_orders'
    ) THEN
        ALTER TABLE delivery_orders
        ADD CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id);
    END IF;
END $$;


-- =====================================================
-- 3. FIX KIỂU DỮ LIỆU CHO DEPOTS OPERATING TIME
-- =====================================================

-- Thêm cột mới với kiểu TIME
ALTER TABLE depots ADD COLUMN IF NOT EXISTS operating_start TIME;
ALTER TABLE depots ADD COLUMN IF NOT EXISTS operating_end TIME;

-- Migrate data (nếu có data cũ)
UPDATE depots 
SET operating_start = operating_start_time::TIME,
    operating_end = operating_end_time::TIME
WHERE operating_start_time IS NOT NULL 
  AND operating_start IS NULL;

-- Note: Sau khi verify, có thể drop các cột cũ:
-- ALTER TABLE depots DROP COLUMN operating_start_time;
-- ALTER TABLE depots DROP COLUMN operating_end_time;

COMMENT ON COLUMN depots.operating_start IS 'Giờ mở cửa depot (e.g., 08:00)';
COMMENT ON COLUMN depots.operating_end IS 'Giờ đóng cửa depot (e.g., 18:00)';


-- =====================================================
-- 4. THÊM PICKUP TIME WINDOW CHO DELIVERY_ORDERS
-- =====================================================

ALTER TABLE delivery_orders ADD COLUMN IF NOT EXISTS pickup_start_time TIMESTAMP;
ALTER TABLE delivery_orders ADD COLUMN IF NOT EXISTS pickup_end_time TIMESTAMP;

COMMENT ON COLUMN delivery_orders.pickup_start_time IS 'Thời gian bắt đầu khung giờ lấy hàng';
COMMENT ON COLUMN delivery_orders.pickup_end_time IS 'Thời gian kết thúc khung giờ lấy hàng';


-- =====================================================
-- 5. THÊM BẢNG DISTANCE_MATRIX (CACHING)
-- =====================================================

CREATE TABLE IF NOT EXISTS distance_matrix (
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

CREATE INDEX IF NOT EXISTS idx_distance_from ON distance_matrix(from_location_id);
CREATE INDEX IF NOT EXISTS idx_distance_to ON distance_matrix(to_location_id);

COMMENT ON TABLE distance_matrix IS 'Cache khoảng cách và thời gian di chuyển giữa các locations';
COMMENT ON COLUMN distance_matrix.source IS 'Nguồn tính toán: CALCULATED, GOOGLE_MAPS, OSRM, etc.';


-- =====================================================
-- 6. THÊM CONSTRAINT CHO ROUTE_STOPS
-- =====================================================

-- Đảm bảo các stop không phải DEPOT phải có delivery_order_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'chk_stop_order_required'
    ) THEN
        ALTER TABLE route_stops ADD CONSTRAINT chk_stop_order_required 
        CHECK (stop_type = 'DEPOT' OR delivery_order_id IS NOT NULL);
    END IF;
END $$;


-- =====================================================
-- 7. THÊM ESTIMATED DISTANCE/DURATION CHO ROUTE_STOPS
-- =====================================================

ALTER TABLE route_stops ADD COLUMN IF NOT EXISTS distance_from_prev_km DOUBLE PRECISION;
ALTER TABLE route_stops ADD COLUMN IF NOT EXISTS duration_from_prev_minutes INTEGER;

COMMENT ON COLUMN route_stops.distance_from_prev_km IS 'Khoảng cách từ điểm dừng trước (km)';
COMMENT ON COLUMN route_stops.duration_from_prev_minutes IS 'Thời gian di chuyển từ điểm dừng trước (phút)';


-- =====================================================
-- 8. THÊM SERVICE TIME CHO ROUTE_STOPS
-- =====================================================

ALTER TABLE route_stops ADD COLUMN IF NOT EXISTS service_time_minutes INTEGER DEFAULT 15;

COMMENT ON COLUMN route_stops.service_time_minutes IS 'Thời gian xử lý tại điểm dừng (phút)';


-- =====================================================
-- Summary of Changes
-- =====================================================

-- New Tables: 2
-- 1. customers
-- 2. distance_matrix

-- Modified Tables: 3
-- 1. depots (added operating_start, operating_end as TIME)
-- 2. delivery_orders (added pickup_start_time, pickup_end_time)
-- 3. route_stops (added distance_from_prev_km, duration_from_prev_minutes, service_time_minutes, constraint)

-- Total Tables After Update: 16

SELECT 'Database schema updated successfully to v2!' AS status;
