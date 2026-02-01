# 📋 Kế Hoạch Triển Khai - Logistics Control Hub

> **AI Supply Chain Control Tower System**  
> **Thời gian:** 12-13 tuần (3 tháng)

---

## 🎯 Mục Tiêu Dự Án

Xây dựng hệ thống quản lý logistics với các tính năng:

- ✅ CRUD cơ bản cho **Location**, Depot, Vehicle, Order
- ✅ Authentication cho dispatcher
- ✅ Tối ưu hóa tuyến đường, chi phí (Google OR-Tools)
- ✅ Analytics: Tổng km, tổng thời gian, số xe, số đơn
- ✅ Real-time tracking (WebSocket)
- ✅ Disruption handling bằng cách can thiệp của admin

---

## 🛠️ Tech Stack

### Backend

| Component    | Technology         | Version     | Purpose                   |
| ------------ | ------------------ | ----------- | ------------------------- |
| Framework    | Spring Boot        | 3.4.4       | REST API, Business Logic  |
| Language     | Java               | 17          | Main programming language |
| Database     | PostgreSQL         | 15          | Data persistence          |
| ORM          | JPA/Hibernate      | -           | Object-relational mapping |
| Event Bus    | Apache Kafka       | 4.x (KRaft) | Async messaging           |
| Workflow     | Temporal           | 1.24.0      | Durable workflows         |
| Optimization | Google OR-Tools    | 9.8.3296    | Route optimization        |
| Validation   | Jakarta Validation | -           | Input validation          |
| Mapping      | MapStruct          | 1.5.5       | DTO mapping               |
| API Docs     | SpringDoc OpenAPI  | 2.3.0       | Swagger UI                |

### Frontend

| Component  | Technology              | Purpose                      |
| ---------- | ----------------------- | ---------------------------- |
| Framework  | Next.js 16              | React Framework (App Router) |
| Styling    | Tailwind CSS v4         | Utility-first CSS            |
| UI Library | Shadcn UI               | Accessible implementation    |
| State      | React Context / Zustand | State management             |
| Validation | Zod + React Hook Form   | Form validation              |
| Map        | Leaflet                 | Map visualization            |

### Infrastructure

| Service       | Technology          | Purpose         |
| ------------- | ------------------- | --------------- |
| Database      | PostgreSQL (local)  | Data storage    |
| Message Queue | Kafka (local KRaft) | Event streaming |

---

## 📅 Timeline & Phases

### **Giai đoạn 1: Nền tảng & Tính năng cốt lõi** (4 tuần) ✅ Tuần 1-3 HOÀN THÀNH

> **Scope:** Chạy được bài toán logistics cơ bản, có kết quả rõ ràng

**Tuần 1: Thiết lập dự án** ✅

- Backend: Spring Boot + PostgreSQL + Kafka setup ✅
- Frontend: React + Vite + Ant Design setup ✅
- Exception handling, Value objects ✅

**Tuần 2 : Tạo lược đồ cơ sở dữ liệu** ✅

- Database tables for Core Entities ✅

**Tuần 3: CRUD + Định tuyến cơ bản**

- **Xác thực (Authentication)** ✅
- **Location**, Depot, Vehicle, Driver, Order entities ✅
- Full CRUD endpoints + UI pages
  - **Vehicles/Fleet**: ✅
  - **Drivers**: ✅
  - **Orders**: ✅
- **Order**: Manual create + Auto-generate button
- **Routing**: OR-Tools integration (simplified - no time windows) 🚧 (Entities ready, Service pending)

**Tuần 4: Tích hợp UI & KPI**

- Hiển thị route trên map (Leaflet) ✅
- Bảng xe - đơn ✅
- KPI cơ bản (km, thời gian, số xe, số đơn)
- Lưu lịch sử routing runs vào DB

---

### **Giai đoạn 2: Tối ưu hóa & Mở rộng** (2-3 tuần)

**Week 5-6:** Tích ích hợp Google OR-Tools:

- Single depot
- Capacity constraint (volume / weight)
- No time window
- Batch optimize (bấm nút “Optimize”)
- Lưu kết quả:
  - routing_run
  - vehicle_routes
- **Kafka Integration:**
  - Publish events: `RoutingOptimizationRequested`, `RoutingOptimizationCompleted`
  - Consumer xử lý optimization task async
- refactor and optimize database schema
  **Week 7:** Visualization & KPI
- Hiển thị tuyến giao trên map (Leaflet)
- Bảng:
  - Xe → danh sách đơn được gán
- KPI:
  - Tổng km
  - Tổng thời gian
  - Số xe dùng
  - Số đơn giao
- Xem lại lịch sử routing runs

### **Giai đoạn 3: Can thiệp Admin & Điều phối quy trình** (2-3 tuần)

**Week 8:** Admin intervention

- Admin can add or remove disruption and request re-optimization
- Admin có thể thay đổi lộ trình thủ công (Human-in-the-loop):
  - Người điều phối muốn đổi tuyến -> AI đề xuất các phương án khả dụng khác.
  - Người điều phối chọn phương án tốt nhất.
- **Kafka Events:**
  - `DisruptionReported`, `RouteModified`, `ReoptimizationRequested`

**Week 9:** Temporal Workflow Integration

- **Workflow:** `RouteOptimizationWorkflow`
  - Orchestrate: fetch orders → optimize → save results → notify
  - Handle retry logic, timeouts
- **Workflow:** `DisruptionHandlingWorkflow`
  - Detect disruption → re-optimize affected routes → update DB
- Activity implementation cho các bước trong workflow

---

## 📂 Project Structure

```
Logistics-Control-Hub/
├── backend/                     ✅
│   └── src/main/java/com/logistics/hub/
│       ├── feature/             ✅
│       │   ├── auth/            ✅
│       │   ├── vehicle/         ✅
│       │   ├── driver/          ✅
│       │   ├── order/           ✅ (Backend)
│       │   └── routing/         🚧 (Entities only)
│       ├── common/              ✅
│       └── config/              ✅
│
├── frontend/                    ✅
│   └── src/
│       ├── app/                 ✅ (Next.js App Router)
│       │   ├── dashboard/       ✅
│       │   ├── fleet/           ✅
│       │   ├── drivers/         ✅
│       │   └── ...
│       ├── components/          ✅ (Shadcn UI)
│       ├── lib/                 ✅
│       └── styles/              ✅
│
└── PLAN.md                      ✅ (this file)
```

---

## 🚀 Getting Started

### Backend

```bash
cd backend
mvn spring-boot:run
```

→ http://localhost:8080

### Frontend

```bash
cd frontend
npm run dev
```

→ http://localhost:3000

---

**Trạng thái:** Giai đoạn 1 - Tuần 3 ✅ HOÀN THÀNH
**Tiếp theo:** Hoàn thiện Dịch vụ Định tuyến (OR-Tools) & UI Đơn hàng
