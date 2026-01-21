-- =====================================================
-- Verification & Utility Queries
-- AI Control Tower Database
-- =====================================================

-- =====================================================
-- 1. Schema Verification
-- =====================================================

-- List all tables
SELECT table_name, 
       pg_size_pretty(pg_total_relation_size(quote_ident(table_name)::regclass)) AS size
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- List all foreign keys
SELECT
    tc.table_name as from_table,
    kcu.column_name as from_column,
    ccu.table_name AS to_table,
    ccu.column_name AS to_column,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY from_table, from_column;

-- List all indexes
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- =====================================================
-- 2. Data Overview Queries
-- =====================================================

-- Count records in all tables
SELECT 'dispatchers' as table_name, COUNT(*) as count FROM dispatchers
UNION ALL SELECT 'locations', COUNT(*) FROM locations
UNION ALL SELECT 'depots', COUNT(*) FROM depots
UNION ALL SELECT 'drivers', COUNT(*) FROM drivers
UNION ALL SELECT 'vehicles', COUNT(*) FROM vehicles
UNION ALL SELECT 'delivery_orders', COUNT(*) FROM delivery_orders
UNION ALL SELECT 'delivery_tasks', COUNT(*) FROM delivery_tasks
UNION ALL SELECT 'optimization_runs', COUNT(*) FROM optimization_runs
UNION ALL SELECT 'route_plans', COUNT(*) FROM route_plans
UNION ALL SELECT 'route_stops', COUNT(*) FROM route_stops
UNION ALL SELECT 'vehicle_positions', COUNT(*) FROM vehicle_positions
UNION ALL SELECT 'system_events', COUNT(*) FROM system_events
UNION ALL SELECT 'disruption_events', COUNT(*) FROM disruption_events
UNION ALL SELECT 'decision_logs', COUNT(*) FROM decision_logs
UNION ALL SELECT 'manual_overrides', COUNT(*) FROM manual_overrides
ORDER BY table_name;

-- =====================================================
-- 3. Business Queries
-- =====================================================

-- Active vehicles with current status
SELECT 
    v.id,
    v.plate_number,
    v.status,
    d.name as depot_name,
    dr.full_name as driver_name,
    v.capacity,
    v.current_latitude,
    v.current_longitude
FROM vehicles v
JOIN depots d ON v.depot_id = d.id
LEFT JOIN drivers dr ON v.driver_id = dr.id
WHERE v.status != 'DISABLED'
ORDER BY v.plate_number;

-- Orders by status
SELECT 
    status,
    COUNT(*) as count,
    AVG(weight) as avg_weight,
    SUM(weight) as total_weight
FROM delivery_orders
GROUP BY status
ORDER BY status;

-- Current active routes
SELECT 
    rp.id,
    rp.status,
    v.plate_number,
    dr.full_name as driver,
    rp.planned_start_time,
    rp.planned_end_time,
    rp.total_distance_km,
    rp.total_duration_minutes,
    COUNT(rs.id) as total_stops
FROM route_plans rp
JOIN vehicles v ON rp.vehicle_id = v.id
LEFT JOIN drivers dr ON v.driver_id = dr.id
LEFT JOIN route_stops rs ON rp.id = rs.route_plan_id
WHERE rp.status = 'ACTIVE'
GROUP BY rp.id, rp.status, v.plate_number, dr.full_name, 
         rp.planned_start_time, rp.planned_end_time, 
         rp.total_distance_km, rp.total_duration_minutes
ORDER BY rp.id;

-- Route stops with planned vs actual times
SELECT 
    rs.id,
    rp.id as route_id,
    v.plate_number,
    l.name as location_name,
    rs.sequence,
    rs.planned_arrival,
    rs.actual_arrival,
    CASE 
        WHEN rs.actual_arrival IS NOT NULL THEN
            EXTRACT(EPOCH FROM (rs.actual_arrival - rs.planned_arrival))/60
        ELSE NULL
    END as delay_minutes,
    rs.status
FROM route_stops rs
JOIN route_plans rp ON rs.route_plan_id = rp.id
JOIN vehicles v ON rp.vehicle_id = v.id
JOIN locations l ON rs.location_id = l.id
WHERE rp.status = 'ACTIVE'
ORDER BY rp.id, rs.sequence;

-- Optimization runs performance
SELECT 
    id,
    trigger_type,
    trigger_reason,
    execution_time_ms,
    status,
    created_at,
    output_metrics->>'totalDistance' as total_distance,
    output_metrics->>'vehiclesUsed' as vehicles_used
FROM optimization_runs
ORDER BY created_at DESC
LIMIT 10;

-- Recent disruptions
SELECT 
    de.id,
    de.type,
    de.severity,
    de.description,
    v.plate_number as affected_vehicle,
    l.name as location,
    de.detected_at,
    de.status
FROM disruption_events de
LEFT JOIN vehicles v ON de.affected_vehicle_id = v.id
LEFT JOIN locations l ON de.location_id = l.id
ORDER BY de.detected_at DESC
LIMIT 10;

-- Vehicle tracking history
SELECT 
    vp.id,
    v.plate_number,
    vp.latitude,
    vp.longitude,
    vp.speed_kmh,
    vp.timestamp,
    vp.source
FROM vehicle_positions vp
JOIN vehicles v ON vp.vehicle_id = v.id
WHERE v.id = 3 -- Change vehicle ID as needed
ORDER BY vp.timestamp DESC
LIMIT 20;

-- Decision logs with alternatives
SELECT 
    dl.id,
    dl.decision_type,
    dl.reason,
    dl.selected_option,
    dl.confidence_score,
    dl.alternatives,
    dl.timestamp,
    or2.trigger_type
FROM decision_logs dl
JOIN optimization_runs or2 ON dl.optimization_run_id = or2.id
ORDER BY dl.timestamp DESC
LIMIT 10;

-- Manual overrides audit
SELECT 
    mo.id,
    mo.override_type,
    mo.entity_type,
    mo.original_value,
    mo.new_value,
    mo.reason,
    mo.performed_by,
    mo.approved,
    mo.timestamp
FROM manual_overrides mo
ORDER BY mo.timestamp DESC
LIMIT 10;

-- =====================================================
-- 4. Analytics Queries
-- =====================================================

-- On-time delivery performance
SELECT 
    COUNT(*) as total_deliveries,
    SUM(CASE WHEN rs.actual_arrival <= rs.planned_arrival THEN 1 ELSE 0 END) as on_time,
    SUM(CASE WHEN rs.actual_arrival > rs.planned_arrival THEN 1 ELSE 0 END) as delayed,
    ROUND(100.0 * SUM(CASE WHEN rs.actual_arrival <= rs.planned_arrival THEN 1 ELSE 0 END) / COUNT(*), 2) as on_time_percentage
FROM route_stops rs
WHERE rs.actual_arrival IS NOT NULL
  AND rs.delivery_order_id IS NOT NULL;

-- Average delay by location
SELECT 
    l.name as location,
    l.type,
    COUNT(*) as stops_count,
    ROUND(AVG(EXTRACT(EPOCH FROM (rs.actual_arrival - rs.planned_arrival))/60)::numeric, 2) as avg_delay_minutes
FROM route_stops rs
JOIN locations l ON rs.location_id = l.id
WHERE rs.actual_arrival IS NOT NULL
GROUP BY l.id, l.name, l.type
HAVING COUNT(*) > 1
ORDER BY avg_delay_minutes DESC;

-- Vehicle utilization
SELECT 
    v.plate_number,
    v.status,
    COUNT(DISTINCT rp.id) as routes_assigned,
    SUM(rp.total_distance_km) as total_distance,
    SUM(rp.total_duration_minutes) as total_duration_minutes
FROM vehicles v
LEFT JOIN route_plans rp ON v.id = rp.vehicle_id
GROUP BY v.id, v.plate_number, v.status
ORDER BY total_distance DESC NULLS LAST;

-- Events by type and severity
SELECT 
    event_type,
    severity,
    COUNT(*) as count
FROM system_events
GROUP BY event_type, severity
ORDER BY count DESC;

-- =====================================================
-- 5. Cleanup Queries (Use with caution!)
-- =====================================================

-- Delete all data (keep schema)
-- UNCOMMMENT TO USE - BE CAREFUL!
/*
TRUNCATE TABLE manual_overrides CASCADE;
TRUNCATE TABLE decision_logs CASCADE;
TRUNCATE TABLE disruption_events CASCADE;
TRUNCATE TABLE system_events CASCADE;
TRUNCATE TABLE vehicle_positions CASCADE;
TRUNCATE TABLE route_stops CASCADE;
TRUNCATE TABLE delivery_tasks CASCADE;
TRUNCATE TABLE route_plans CASCADE;
TRUNCATE TABLE optimization_runs CASCADE;
TRUNCATE TABLE delivery_orders CASCADE;
TRUNCATE TABLE vehicles CASCADE;
TRUNCATE TABLE drivers CASCADE;
TRUNCATE TABLE depots CASCADE;
TRUNCATE TABLE locations CASCADE;
*/

-- Drop entire schema (DANGEROUS!)
-- UNCOMMENT TO USE - BE VERY CAREFUL!
/*
DROP TABLE IF EXISTS manual_overrides CASCADE;
DROP TABLE IF EXISTS decision_logs CASCADE;
DROP TABLE IF EXISTS disruption_events CASCADE;
DROP TABLE IF EXISTS system_events CASCADE;
DROP TABLE IF EXISTS vehicle_positions CASCADE;
DROP TABLE IF EXISTS route_stops CASCADE;
DROP TABLE IF EXISTS delivery_tasks CASCADE;
DROP TABLE IF EXISTS route_plans CASCADE;
DROP TABLE IF EXISTS optimization_runs CASCADE;
DROP TABLE IF EXISTS delivery_orders CASCADE;
DROP TABLE IF EXISTS vehicles CASCADE;
DROP TABLE IF EXISTS drivers CASCADE;
DROP TABLE IF EXISTS depots CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
*/
