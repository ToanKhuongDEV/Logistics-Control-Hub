# 📋 Kế Hoạch Triển Khai - Logistics Control Hub

> **AI Supply Chain Control Tower System**  
> **Thời gian:** 12-13 tuần (3 tháng)  
> **Team:** 2 Backend Devs + 1 Frontend Dev

---

## 🎯 Mục Tiêu Dự Án

Xây dựng hệ thống quản lý logistics với các tính năng:
- ✅ CRUD cơ bản cho **Location, Customer**, Depot, Vehicle, Driver, Order
- ✅ Tối ưu hóa tuyến đường (Google OR-Tools)
- ✅ Real-time tracking (WebSocket)
- ✅ Disruption handling tự động
- ✅ Admin intervention & audit trail
- ✅ Analytics & reporting

---

## 🛠️ Tech Stack

### Backend
| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | Spring Boot | 3.4.4 | REST API, Business Logic |
| Language | Java | 17 | Main programming language |
| Database | PostgreSQL | 15 | Data persistence |
| ORM | JPA/Hibernate | - | Object-relational mapping |
| Event Bus | Apache Kafka | 4.x (KRaft) | Async messaging |
| Workflow | Temporal | 1.24.0 | Durable workflows |
| Optimization | Google OR-Tools | 9.8.3296 | Route optimization |
| Validation | Jakarta Validation | - | Input validation |
| Mapping | MapStruct | 1.5.5 | DTO mapping |
| API Docs | SpringDoc OpenAPI | 2.3.0 | Swagger UI |

### Frontend
| Component | Technology | Purpose |
|-----------|-----------|---------|
| Framework | React | UI library |
| Build Tool | Vite | Fast dev server & build |
| Language | JavaScript | (ES6+) |
| UI Library | Ant Design | Component library |
| Routing | React Router v6 | Navigation |
| HTTP Client | Axios | API calls |
| State | React Context / Zustand | State management |
| Map | Leaflet | Map visualization |

### Infrastructure
| Service | Technology | Purpose |
|---------|-----------|---------|
| Database | PostgreSQL (local) | Data storage |
| Message Queue | Kafka (local KRaft) | Event streaming |

---

## 📅 Timeline & Phases

### **Phase 1: Foundation & Basic CRUD** (4 tuần) ✅ Week 1 DONE

**Week 1: Project Setup** ✅
- Backend: Spring Boot + PostgreSQL + Kafka setup ✅
- Frontend: React + Vite + Ant Design setup ✅
- Exception handling, Value objects ✅

**Week 2-3: CRUD Implementation**
- **Location, Customer**, Depot, Vehicle, Driver, Order entities
- Full CRUD endpoints + UI pages
- **Order**: Manual create + Auto-generate button

**Week 4: Integration & Testing**

---

### **Phase 2: Route Optimization & Tracking** (4 tuần)

**Week 5-6:** OR-Tools routing, Distance calculation  
**Week 7:** WebSocket real-time tracking  
**Week 8:** Basic simulation engine

---

### **Phase 3: Advanced Features** (3 tuần)

**Week 8:** Disruption management  
**Week 9:** Temporal workflows  
**Week 10:** Admin override & audit

---

### **Phase 4: Analytics & Polish** (2 tuần)

**Week 11:** Analytics dashboard  
**Week 12:** Testing, optimization, documentation

---

## 📂 Project Structure

```
Logistics-Control-Hub/
├── backend/                     ✅
│   └── src/main/java/com/logistics/hub/
│       ├── feature/             ✅ (structure ready)
│       ├── shared/              ✅
│       └── config/              ✅
│
├── frontend/                    ✅
│   └── src/
│       ├── pages/
│       ├── components/
│       ├── services/            ✅
│       └── utils/
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
→ http://localhost:5174

---

**Status:** Phase 1 - Week 1 ✅ COMPLETED  
**Next:** Week 2-3 - CRUD Implementation
