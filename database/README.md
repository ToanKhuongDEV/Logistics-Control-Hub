# Database Scripts - AI Control Tower

## 📁 Tổng Quan

Thư mục này chứa các SQL scripts để setup và quản lý PostgreSQL database cho AI Control Tower system.

---

## 📜 Các Files

### 1. [create_schema.sql](file:///d:/Intern_job/Logistics%20Control%20Hub/database/create_schema.sql)
**Mục đích**: Tạo toàn bộ database schema

**Nội dung**:
- 14 tables với đầy đủ columns
- Foreign key constraints (14 FKs)
- Indexes (optimization cho queries)
- Comments cho documentation

**Cách chạy**:
```sql
-- Trong pgAdmin hoặc psql
\i /path/to/create_schema.sql

-- Hoặc copy-paste toàn bộ nội dung vào Query Tool
```

**Thứ tự tạo tables** (theo dependency):
1. locations
2. depots, drivers
3. vehicles
4. delivery_orders
5. optimization_runs
6. route_plans
7. delivery_tasks, route_stops
8. vehicle_positions, system_events, disruption_events
9. decision_logs, manual_overrides

---

### 2. [insert_sample_data.sql](file:///d:/Intern_job/Logistics%20Control%20Hub/database/insert_sample_data.sql)
**Mục đích**: Insert dữ liệu mẫu để test

**Nội dung**:
- 8 locations (warehouses, hubs, customers)
- 2 depots (HCM, Hà Nội)
- 5 drivers
- 5 vehicles
- 5 delivery orders
- 2 optimization runs
- 3 route plans với route stops
- GPS tracking data
- Events, disruptions, decisions

**Cách chạy**:
```sql
-- Chạy SAU KHI đã chạy create_schema.sql
\i /path/to/insert_sample_data.sql
```

**Verification**:
```sql
-- Kiểm tra số lượng records
SELECT 'locations' as table_name, COUNT(*) FROM locations UNION ALL
SELECT 'depots', COUNT(*) FROM depots UNION ALL
SELECT 'vehicles', COUNT(*) FROM vehicles;
```

---

### 3. [verification_queries.sql](file:///d:/Intern_job/Logistics%20Control%20Hub/database/verification_queries.sql)
**Mục đích**: Queries hữu ích để verify và analyze data

**Nội dung**:

#### Schema Verification
- List all tables
- List all foreign keys
- List all indexes

#### Business Queries
- Active vehicles
- Orders by status
- Current active routes
- Route stops (planned vs actual)
- Optimization performance
- Recent disruptions
- Vehicle tracking history
- Decision logs
- Manual overrides audit

#### Analytics Queries
- On-time delivery performance
- Average delay by location
- Vehicle utilization
- Events by type and severity

#### Cleanup Queries
- Truncate all data (cẩn thận!)
- Drop entire schema (rất nguy hiểm!)

**Cách dùng**:
```sql
-- Copy query bạn cần vào Query Tool và chạy
-- Ví dụ: xem active routes
SELECT 
    rp.id,
    v.plate_number,
    rp.status,
    rp.total_distance_km
FROM route_plans rp
JOIN vehicles v ON rp.vehicle_id = v.id
WHERE rp.status = 'ACTIVE';
```

---

## 🚀 Quick Start Guide

### Bước 1: Tạo Database
```sql
-- Trong PostgreSQL terminal hoặc pgAdmin
CREATE DATABASE logistics_control_hub;
\c logistics_control_hub
```

### Bước 2: Chạy Schema Script
```sql
-- Trong pgAdmin Query Tool
-- Copy toàn bộ nội dung của create_schema.sql và Run
```

### Bước 3: Insert Sample Data
```sql
-- Copy toàn bộ nội dung của insert_sample_data.sql và Run
```

### Bước 4: Verify Data
```sql
-- Chạy count query từ verification_queries.sql
SELECT 
    (SELECT COUNT(*) FROM locations) as locations_count,
    (SELECT COUNT(*) FROM depots) as depots_count,
    (SELECT COUNT(*) FROM vehicles) as vehicles_count;
```

---

## 🔑 Key Features

### Loose Coupling
- `delivery_orders.customer_id` - KHÔNG có FK (customer ở service khác)
- `manual_overrides.performed_by` - KHÔNG có FK (user reference)
- Các fields khác đều có FK constraints

### JSON Support
- `optimization_runs.input_snapshot` - JSONB
- `optimization_runs.output_metrics` - JSONB
- `system_events.payload` - JSONB
- `decision_logs.alternatives` - JSONB

### Time-series Optimization
- `vehicle_positions` có index trên `(vehicle_id, timestamp DESC)`
- `system_events` có index trên `(event_type, timestamp DESC)`

---

## 📊 Database Diagram

```
┌──────────┐       ┌─────────┐
│ Locations│◄──────│ Depots  │
└────┬─────┘       └────┬────┘
     │                  │
     │            ┌─────▼─────┐      ┌─────────┐
     │            │ Vehicles  │◄─────│ Drivers │
     │            └─────┬─────┘      └─────────┘
     │                  │
     │            ┌─────▼──────────┐
     ├────────────│ RoutePlans     │
     │            └─────┬──────────┘
     │                  │
┌────▼─────────┐  ┌────▼──────┐
│DeliveryOrders│  │RouteStops │
└──────┬───────┘  └───────────┘
       │
   ┌───▼──────────┐
   │DeliveryTasks │
   └──────────────┘
```

---

## ⚠️ Important Notes

### Foreign Key Constraints
- Đảm bảo referential integrity
- Cascade delete được config cho một số relationships
- Loose coupling cho external services (customer, user)

### Indexes
- Tất cả FK columns đều có index
- Time-based queries có composite index
- Status fields có index cho filtering

### Data Types
- Timestamps: `TIMESTAMP` (không có timezone)
- JSON: `JSONB` (binary JSON, faster queries)
- Coordinates: `DOUBLE PRECISION`

---

## 🛠️ Maintenance

### Backup
```bash
pg_dump -U postgres -d logistics_control_hub > backup.sql
```

### Restore
```bash
psql -U postgres -d logistics_control_hub < backup.sql
```

### Clear All Data (Keep Schema)
```sql
-- Xem trong verification_queries.sql
-- TRUNCATE commands (commented out for safety)
```

---

## 📝 Next Steps

1. ✅ Run `create_schema.sql` trong pgAdmin
2. ✅ Run `insert_sample_data.sql` để có data test
3. ✅ Test các queries trong `verification_queries.sql`
4. ⏳ Integrate với Spring Boot JPA
5. ⏳ Setup Kafka cho vehicle position streaming
