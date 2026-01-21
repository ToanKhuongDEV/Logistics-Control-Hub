-- =====================================================
-- Sample Data - AI Control Tower Database (Schema V2)
-- =====================================================
-- Script này chứa dữ liệu mẫu để test hệ thống
-- Chạy sau khi đã chạy create_schema.sql (hoặc update_schema_v2.sql)

-- =====================================================
-- 1. Locations
-- =====================================================

INSERT INTO locations (name, latitude, longitude, type, address) VALUES
('Kho Trung Tâm HCM', 10.7769, 106.7009, 'WAREHOUSE', '123 Nguyễn Huệ, Q1, TP.HCM'),
('Kho Miền Bắc', 21.0285, 105.8542, 'WAREHOUSE', '456 Hai Bà Trưng, Hoàn Kiếm, Hà Nội'),
('Hub Bình Thạnh', 10.8142, 106.7053, 'HUB', '789 Xô Viết Nghệ Tĩnh, Bình Thạnh, TP.HCM'),
('Khách hàng A', 10.8231, 106.6297, 'CUSTOMER', '100 Lê Lợi, Q1, TP.HCM'),
('Khách hàng B', 10.7545, 106.6701, 'CUSTOMER', '200 Nguyễn Trãi, Q5, TP.HCM'),
('Khách hàng C', 10.7931, 106.7208, 'CUSTOMER', '300 Phan Văn Trị, Gò Vấp, TP.HCM'),
('Khách hàng D', 21.0378, 105.8342, 'CUSTOMER', '400 Láng Hạ, Đống Đa, Hà Nội'),
('Hub Thủ Đức', 10.8505, 106.7717, 'HUB', '500 Quốc Lộ 1A, Thủ Đức, TP.HCM');

-- =====================================================
-- 2. Customers (NEW V2)
-- =====================================================

INSERT INTO customers (name, phone, email, company_name, default_location_id, active) VALUES
('Nguyễn Văn Khách A', '0901111111', 'khacha@gmail.com', 'Công ty A', 4, true),
('Trần Thị Khách B', '0902222222', 'khachb@gmail.com', 'Công ty B', 5, true),
('Lê Văn Khách C', '0903333333', 'khachc@gmail.com', 'Công ty C', 6, true),
('Phạm Thị Khách D', '0904444444', 'khachd@gmail.com', 'Công ty D', 7, true),
('Hoàng Văn Khách E', '0905555555', 'khache@gmail.com', 'Công ty E', 4, true);

-- =====================================================
-- 3. Depots (Updated TIME type)
-- =====================================================

INSERT INTO depots (name, location_id, capacity, operating_start, operating_end, active) VALUES
('Depot HCM', 1, 50, '06:00:00', '22:00:00', true),
('Depot Hà Nội', 2, 30, '06:00:00', '22:00:00', true);

-- =====================================================
-- 4. Drivers
-- =====================================================

INSERT INTO drivers (full_name, license_number, phone_number, status) VALUES
('Nguyễn Văn A', 'B123456789', '0901234567', 'AVAILABLE'),
('Trần Thị B', 'B234567890', '0902345678', 'AVAILABLE'),
('Lê Văn C', 'B345678901', '0903456789', 'DRIVING'),
('Phạm Thị D', 'B456789012', '0904567890', 'AVAILABLE'),
('Hoàng Văn E', 'B567890123', '0905678901', 'OFF_DUTY');

-- =====================================================
-- 5. Vehicles
-- =====================================================

INSERT INTO vehicles (plate_number, depot_id, driver_id, capacity, current_latitude, current_longitude, status) VALUES
('51A-12345', 1, 1, 1000.0, 10.7769, 106.7009, 'AVAILABLE'),
('51B-23456', 1, 2, 1500.0, 10.7769, 106.7009, 'AVAILABLE'),
('51C-34567', 1, 3, 2000.0, 10.8142, 106.7053, 'IN_USE'),
('30A-45678', 2, 4, 1200.0, 21.0285, 105.8542, 'AVAILABLE'),
('30B-56789', 2, NULL, 1800.0, 21.0285, 105.8542, 'MAINTENANCE');

-- =====================================================
-- 6. Delivery Orders (Updated FK customer_id + pickup time)
-- =====================================================

INSERT INTO delivery_orders (order_number, customer_id, pickup_location_id, delivery_location_id, 
                             pickup_start_time, pickup_end_time,
                             delivery_start_time, delivery_end_time, weight, priority, status) VALUES
('ORD-2026-00001', 1, 1, 4, '2026-01-21 08:00:00', '2026-01-21 12:00:00', '2026-01-21 14:00:00', '2026-01-21 16:00:00', 50.0, 'NORMAL', 'PENDING'),
('ORD-2026-00002', 2, 1, 5, '2026-01-21 07:00:00', '2026-01-21 09:00:00', '2026-01-21 09:00:00', '2026-01-21 12:00:00', 75.5, 'HIGH', 'PLANNED'),
('ORD-2026-00003', 3, 1, 6, '2026-01-21 10:00:00', '2026-01-21 14:00:00', '2026-01-21 15:00:00', '2026-01-21 18:00:00', 120.0, 'URGENT', 'ASSIGNED'),
('ORD-2026-00004', 4, 2, 7, '2026-01-22 06:00:00', '2026-01-22 08:00:00', '2026-01-22 08:00:00', '2026-01-22 10:00:00', 60.0, 'NORMAL', 'PENDING'),
('ORD-2026-00005', 5, 1, 4, '2026-01-21 08:00:00', '2026-01-21 10:00:00', '2026-01-21 10:00:00', '2026-01-21 13:00:00', 85.0, 'HIGH', 'IN_TRANSIT');

-- =====================================================
-- 7. Distance Matrix (NEW)
-- =====================================================

INSERT INTO distance_matrix (from_location_id, to_location_id, distance_km, duration_minutes, source) VALUES
(1, 4, 10.5, 30, 'CALCULATED'), -- HCM -> Cust A
(1, 5, 5.2, 15, 'CALCULATED'),  -- HCM -> Cust B
(1, 6, 8.0, 20, 'CALCULATED'),  -- HCM -> Cust C
(4, 5, 4.0, 12, 'CALCULATED'),  -- Cust A -> Cust B
(5, 6, 6.0, 18, 'CALCULATED');  -- Cust B -> Cust C

-- =====================================================
-- 8. Optimization Runs
-- =====================================================

INSERT INTO optimization_runs (trigger_type, trigger_reason, input_snapshot, output_metrics, 
                               execution_time_ms, completed_at, status) VALUES
('SCHEDULED', 'Daily morning optimization', 
 '{"orders": 5, "vehicles": 3, "constraints": {"maxDistance": 100}}'::jsonb,
 '{"totalDistance": 45.5, "totalTime": 180, "vehiclesUsed": 3}'::jsonb,
 2500, '2026-01-21 07:00:00', 'COMPLETED'),
('DISRUPTION', 'Traffic jam detected on Route 1', 
 '{"affectedOrders": 2, "alternativeRoutes": 3}'::jsonb,
 '{"rerouted": true, "additionalDistance": 5.2}'::jsonb,
 1800, '2026-01-21 10:30:00', 'COMPLETED');

-- =====================================================
-- 9. Route Plans
-- =====================================================

INSERT INTO route_plans (optimization_run_id, vehicle_id, planned_start_time, planned_end_time,
                        total_distance_km, total_duration_minutes, version, status) VALUES
(1, 1, '2026-01-21 08:00:00', '2026-01-21 12:00:00', 25.5, 120, 1, 'ACTIVE'),
(1, 2, '2026-01-21 09:00:00', '2026-01-21 14:00:00', 35.2, 180, 1, 'ACTIVE'),
(2, 3, '2026-01-21 10:00:00', '2026-01-21 16:00:00', 42.8, 240, 2, 'ACTIVE');

-- =====================================================
-- 10. Delivery Tasks
-- =====================================================

INSERT INTO delivery_tasks (delivery_order_id, vehicle_id, driver_id, route_plan_id, status,
                            actual_pickup_time, actual_delivery_time) VALUES
(3, 3, 3, 3, 'IN_TRANSIT', '2026-01-21 10:30:00', NULL),
(5, 3, 3, 3, 'PICKING_UP', NULL, NULL);

-- =====================================================
-- 11. Route Stops (Updated with distance/duration fields)
-- =====================================================

INSERT INTO route_stops (route_plan_id, location_id, delivery_order_id, stop_type, sequence,
                        planned_arrival, planned_departure, actual_arrival, status,
                        distance_from_prev_km, duration_from_prev_minutes, service_time_minutes) VALUES
-- Route 1
(1, 1, NULL, 'DEPOT', 1, '2026-01-21 08:00:00', '2026-01-21 08:15:00', '2026-01-21 08:00:00', 'COMPLETED', 0, 0, 15),
(1, 4, 1, 'DELIVERY', 2, '2026-01-21 09:00:00', '2026-01-21 09:30:00', '2026-01-21 09:05:00', 'COMPLETED', 10.5, 30, 20),
(1, 1, NULL, 'DEPOT', 3, '2026-01-21 11:00:00', '2026-01-21 11:15:00', NULL, 'PENDING', 10.5, 30, 15),

-- Route 2
(2, 1, NULL, 'DEPOT', 1, '2026-01-21 09:00:00', '2026-01-21 09:15:00', '2026-01-21 09:00:00', 'COMPLETED', 0, 0, 15),
(2, 5, 2, 'DELIVERY', 2, '2026-01-21 10:30:00', '2026-01-21 11:00:00', '2026-01-21 10:35:00', 'COMPLETED', 5.2, 15, 20),
(2, 1, NULL, 'DEPOT', 3, '2026-01-21 13:00:00', '2026-01-21 13:15:00', NULL, 'EN_ROUTE', 5.2, 15, 15),

-- Route 3 (current active)
(3, 1, NULL, 'DEPOT', 1, '2026-01-21 10:00:00', '2026-01-21 10:15:00', '2026-01-21 10:00:00', 'COMPLETED', 0, 0, 15),
(3, 6, 3, 'DELIVERY', 2, '2026-01-21 12:00:00', '2026-01-21 12:30:00', NULL, 'EN_ROUTE', 8.0, 20, 20),
(3, 4, 5, 'DELIVERY', 3, '2026-01-21 14:00:00', '2026-01-21 14:30:00', NULL, 'PENDING', 5.0, 15, 20),
(3, 1, NULL, 'DEPOT', 4, '2026-01-21 16:00:00', '2026-01-21 16:15:00', NULL, 'PENDING', 8.0, 20, 15);

-- =====================================================
-- 12. Vehicle Positions (GPS Tracking)
-- =====================================================

INSERT INTO vehicle_positions (vehicle_id, latitude, longitude, speed_kmh, heading_degrees, 
                               timestamp, source) VALUES
-- Vehicle 1 positions
(1, 10.7769, 106.7009, 0, 0, '2026-01-21 08:00:00', 'GPS'),
(1, 10.7850, 106.7050, 45, 90, '2026-01-21 08:15:00', 'GPS'),
(1, 10.8000, 106.7100, 50, 85, '2026-01-21 08:30:00', 'GPS'),

-- Vehicle 3 positions (currently in transit)
(3, 10.7769, 106.7009, 0, 0, '2026-01-21 10:00:00', 'GPS'),
(3, 10.7850, 106.7050, 55, 75, '2026-01-21 10:30:00', 'GPS'),
(3, 10.7931, 106.7208, 48, 120, '2026-01-21 11:00:00', 'GPS'),
(3, 10.8050, 106.7150, 52, 110, '2026-01-21 11:30:00', 'GPS');

-- =====================================================
-- 13. System Events
-- =====================================================

INSERT INTO system_events (event_type, entity_type, entity_id, payload, timestamp, severity, message) VALUES
('ORDER_CREATED', 'DeliveryOrder', 1, '{"priority": "NORMAL"}'::jsonb, '2026-01-21 07:00:00', 'INFO', 'New order created'),
('OPTIMIZATION_STARTED', 'OptimizationRun', 1, NULL, '2026-01-21 07:00:00', 'INFO', 'Daily optimization started'),
('OPTIMIZATION_COMPLETED', 'OptimizationRun', 1, '{"duration": 2500}'::jsonb, '2026-01-21 07:00:00', 'INFO', 'Optimization completed successfully'),
('ROUTE_ASSIGNED', 'RoutePlan', 1, NULL, '2026-01-21 07:05:00', 'INFO', 'Route assigned to vehicle 51A-12345'),
('VEHICLE_DEPARTED', 'Vehicle', 1, NULL, '2026-01-21 08:00:00', 'INFO', 'Vehicle departed from depot'),
('DISRUPTION_DETECTED', 'DisruptionEvent', 1, '{"type": "TRAFFIC_JAM"}'::jsonb, '2026-01-21 10:00:00', 'HIGH', 'Traffic jam detected'),
('VEHICLE_ARRIVED', 'Vehicle', 1, '{"location": "Customer A"}'::jsonb, '2026-01-21 09:05:00', 'INFO', 'Vehicle arrived at destination');

-- =====================================================
-- 14. Disruption Events
-- =====================================================

INSERT INTO disruption_events (type, affected_vehicle_id, affected_route_id, location_id, severity,
                               description, detected_at, status) VALUES
('TRAFFIC_JAM', 3, 3, 8, 'MEDIUM', 'Heavy traffic on Highway 1', '2026-01-21 10:00:00', 'IN_PROGRESS'),
('VEHICLE_BREAKDOWN', 5, NULL, 2, 'HIGH', 'Engine failure - vehicle sent to maintenance', '2026-01-21 06:00:00', 'RESOLVED');

-- =====================================================
-- 15. Decision Logs (Explainable AI)
-- =====================================================

INSERT INTO decision_logs (optimization_run_id, decision_type, reason, alternatives, 
                          selected_option, confidence_score, timestamp) VALUES
(1, 'ROUTE_CALCULATION', 'Minimizing total distance while respecting time windows',
 '{"option1": {"vehicle": "51A-12345", "distance": 25.5}, "option2": {"vehicle": "51B-23456", "distance": 28.3}}'::jsonb,
 'Assign to Vehicle 51A-12345', 0.92, '2026-01-21 07:00:00'),
(2, 'ROUTE_RECALCULATION', 'Traffic jam detected, rerouting to avoid congestion',
 '{"option1": {"route": "via Highway 1", "eta": "16:30"}, "option2": {"route": "via Local Road", "eta": "16:15"}}'::jsonb,
 'Take local road detour', 0.87, '2026-01-21 10:30:00');

-- =====================================================
-- 16. Manual Overrides
-- =====================================================

INSERT INTO manual_overrides (override_type, entity_type, entity_id, original_value, new_value,
                              reason, performed_by, approved, timestamp) VALUES
('PRIORITY_CHANGE', 'DeliveryOrder', 3, 'NORMAL', 'URGENT', 'Customer requested expedited delivery',
 'dispatcher@company.com', true, '2026-01-21 08:00:00'),
('VEHICLE_DISABLE', 'Vehicle', 5, 'AVAILABLE', 'MAINTENANCE', 'Scheduled maintenance required',
 'admin@company.com', true, '2026-01-21 06:00:00');

-- =====================================================
-- Summary & Verification
-- =====================================================

SELECT 'Sample data inserted successfully!' AS status;
