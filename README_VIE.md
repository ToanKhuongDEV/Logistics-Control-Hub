# 🚛 Logistics Control Hub

Một hệ thống quản lý logistics full-stack giải quyết bài toán **Định Tuyến Xe (VRP)** bằng Google OR-Tools, cung cấp tối ưu hóa lộ trình thời gian thực, trực quan hóa bản đồ tương tác và quản lý đội xe toàn diện.

---

## 📋 Mục Lục

- [Tổng Quan](#tổng-quan)
- [Tech Stack](#tech-stack)
- [Tính Năng](#tính-năng)
- [Kiến Trúc](#kiến-trúc)
- [Cài Đặt & Chạy](#cài-đặt--chạy)
- [Biến Môi Trường](#biến-môi-trường)
- [Tài Liệu API](#tài-liệu-api)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Tài Khoản Mặc Định](#tài-khoản-mặc-định)

---

## Tổng Quan

**Logistics Control Hub** là nền tảng vận hành logistics, mô phỏng cách một hệ thống điều phối đội xe hiện đại hoạt động. Hệ thống xử lý toàn bộ vòng đời từ tạo đơn hàng → gán kho → tối ưu hóa lộ trình → hiển thị trên bản đồ thực tế.

Hệ thống tích hợp với **OSRM** (Open Source Routing Machine) để tính khoảng cách và thời gian di chuyển theo đường thực tế, và sử dụng **Google OR-Tools** để tính toán lộ trình giao hàng tối ưu theo ràng buộc tải trọng xe.

---

## Tech Stack

### Backend

| Tầng                  | Công nghệ                           |
| --------------------- | ----------------------------------- |
| Ngôn ngữ              | Java 17                             |
| Framework             | Spring Boot 3.4.4                   |
| ORM                   | Spring Data JPA + Hibernate         |
| Cơ sở dữ liệu         | PostgreSQL 15                       |
| Cache                 | Redis 7 (Spring Data Redis)         |
| Engine tối ưu         | Google OR-Tools 9.8                 |
| API định tuyến bản đồ | OSRM (Open Source Routing Machine)  |
| Bảo mật               | Spring Security + JWT (JJWT 0.11.5) |
| Tài liệu API          | SpringDoc OpenAPI (Swagger UI)      |
| Code Gen              | Lombok + MapStruct                  |

### Frontend

| Tầng          | Công nghệ             |
| ------------- | --------------------- |
| Framework     | Next.js 16 + React 19 |
| Ngôn ngữ      | TypeScript            |
| Styling       | Tailwind CSS v4       |
| UI Components | Radix UI + shadcn/ui  |
| Bản đồ        | Leaflet.js            |
| Biểu đồ       | Recharts              |
| Form          | React Hook Form + Zod |
| HTTP Client   | Axios                 |

### Hạ Tầng

| Dịch vụ           | Công nghệ                |
| ----------------- | ------------------------ |
| Container hóa     | Docker + Docker Compose  |
| Định tuyến bản đồ | OSRM v5.27 (self-hosted) |
| Cơ sở dữ liệu     | PostgreSQL 15 Alpine     |
| Cache             | Redis 7 Alpine           |

---

## Tính Năng

### 🗺️ Tối Ưu Hóa Lộ Trình

- Giải bài toán **Capacitated VRP** với Google OR-Tools
- Xét ràng buộc tải trọng và thể tích xe
- Sử dụng khoảng cách đường thực từ OSRM API
- Tối ưu hóa bất đồng bộ với cơ chế polling trạng thái
- Hiển thị lộ trình tối ưu dưới dạng polyline trên bản đồ tương tác

### 📦 Quản Lý Đơn Hàng

- CRUD đầy đủ cho đơn giao hàng
- Tự động gán đơn về kho gần nhất
- Vòng đời đơn hàng: `CREATED` → `IN_TRANSIT` → `DELIVERED`
- Lọc theo trạng thái, kho, tài xế

### 🏭 Quản Lý Kho

- Hỗ trợ nhiều kho (multi-depot)
- Mỗi kho có vị trí địa lý trên bản đồ
- Thống kê: xe đang hoạt động, đơn chờ xử lý, số lần tối ưu

### 🚗 Quản Lý Xe & Tài Xế

- Quản lý đội xe với theo dõi trạng thái (`ACTIVE`, `IDLE`, `MAINTENANCE`)
- Gán tài xế cho xe
- Cấu hình năng lực xe (tải trọng kg, thể tích m³, chi phí/km)

### 📊 Bảng Điều Khiển

- Tóm tắt thời gian thực: tổng cự ly, tổng chi phí, số tuyến đường
- Card thống kê theo kho
- Trực quan hóa lộ trình với danh sách điểm dừng

### 🔐 Xác Thực & Phân Quyền

- Xác thực JWT với refresh token
- Phân quyền theo vai trò: `DISPATCHER`, `ADMIN`
- Mã hóa mật khẩu an toàn với BCrypt

### ⚡ Redis Caching

- Cache phản hồi OSRM API (ma trận khoảng cách)
- Cache metadata cho kho, tài xế, xe

---

## Kiến Trúc

```
┌─────────────────────────────────────────────────────┐
│               Frontend (Next.js)                     │
│    Dashboard │ Bản đồ │ Đơn hàng │ Quản lý đội xe   │
└────────────────────────┬────────────────────────────┘
                         │ REST API (HTTP/JSON)
┌────────────────────────▼────────────────────────────┐
│             Backend (Spring Boot)                    │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────┐ │
│  │   Auth   │  │  Orders  │  │  Route Optimizer  │ │
│  ├──────────┤  ├──────────┤  │  (Google OR-Tools)│ │
│  │  Depots  │  │ Vehicles │  └────────┬──────────┘ │
│  ├──────────┤  ├──────────┤           │             │
│  │ Drivers  │  │ Routing  │◄──────────┘             │
│  └──────────┘  └──────────┘                         │
│         │            │                              │
│    ┌────▼────┐  ┌────▼─────────────┐                │
│    │  Redis  │  │    OSRM API      │                │
│    │ (Cache) │  │ (Khoảng cách     │                │
│    └─────────┘  │  đường thực tế)  │                │
│                 └──────────────────┘                │
│         │                                           │
│    ┌────▼─────────────────┐                         │
│    │    PostgreSQL DB      │                        │
│    └──────────────────────┘                         │
└─────────────────────────────────────────────────────┘
```

---

## Cài Đặt & Chạy

### Yêu Cầu

- [Docker](https://www.docker.com/) & Docker Compose
- Hoặc để phát triển cục bộ: Java 17, Node.js 20, PostgreSQL 15, Redis

### Cách A: Chạy bằng Docker Compose (Khuyến nghị)

**1. Clone repository**

```bash
git clone https://github.com/ToanKhuongDEV/Logistics-Control-Hub.git
cd Logistics-Control-Hub
```

**2. Tạo file môi trường**

```bash
cp .env.example .env
# Chỉnh sửa .env theo giá trị của bạn
```

**3. Chuẩn bị dữ liệu OSRM** (bản đồ Việt Nam)

```bash
# Xem DOCKER_GUIDE.md để biết hướng dẫn chi tiết setup OSRM
```

**4. Khởi động tất cả dịch vụ**

```bash
docker-compose up -d
```

**5. Truy cập ứng dụng**
| Dịch vụ | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

### Cách B: Phát Triển Cục Bộ

**Backend**

```bash
cd backend
# Tạo .env với các biến cần thiết
mvn spring-boot:run
```

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

---

## Biến Môi Trường

Tạo file `.env` ở thư mục gốc:

```env
# Database
DB_NAME=logistics_db
DB_USERNAME=postgres
DB_PASSWORD=your_db_password
DB_PORT=5432

# Redis
# Lưu ý: Khi chạy bằng Docker Compose, đặt REDIS_HOST=redis (tên service)
# Khi phát triển cục bộ (không Docker), đặt REDIS_HOST=localhost
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your_jwt_secret_min_32_chars
JWT_REFRESH_SECRET=your_refresh_secret_min_32_chars

# Server
SERVER_PORT=8080
```

---

## Tài Liệu API

Sau khi backend chạy, truy cập Swagger UI tại:

```
http://localhost:8080/swagger-ui.html
```

### Các Endpoint Chính

| Method | Endpoint                         | Mô tả                     |
| ------ | -------------------------------- | ------------------------- |
| `POST` | `/api/v1/auth/login`             | Đăng nhập, trả về JWT     |
| `GET`  | `/api/v1/orders`                 | Danh sách đơn hàng        |
| `POST` | `/api/v1/orders`                 | Tạo đơn hàng mới          |
| `GET`  | `/api/v1/depots`                 | Danh sách kho             |
| `GET`  | `/api/v1/depots/{id}/statistics` | Thống kê theo kho         |
| `POST` | `/api/v1/routing/optimize`       | Kích hoạt tối ưu lộ trình |
| `GET`  | `/api/v1/routing/runs/{id}`      | Lấy kết quả tối ưu        |
| `GET`  | `/api/v1/vehicles`               | Danh sách xe              |
| `GET`  | `/api/v1/drivers`                | Danh sách tài xế          |

---

## Cấu Trúc Dự Án

```
Logistics-Control-Hub/
├── backend/                    # Ứng dụng Spring Boot
│   ├── src/main/java/com/logistics/hub/
│   │   ├── feature/
│   │   │   ├── auth/           # Xác thực JWT
│   │   │   ├── company/        # Quản lý công ty
│   │   │   ├── dashboard/      # Thống kê dashboard
│   │   │   ├── depot/          # Quản lý kho
│   │   │   ├── dispatcher/     # Tài khoản điều phối
│   │   │   ├── driver/         # Quản lý tài xế
│   │   │   ├── geocoding/      # Geocoding địa chỉ
│   │   │   ├── location/       # Quản lý vị trí
│   │   │   ├── order/          # Quản lý đơn hàng
│   │   │   ├── redis/          # Dịch vụ cache Redis
│   │   │   ├── routing/        # Tối ưu VRP + OSRM
│   │   │   └── vehicle/        # Quản lý xe
│   │   └── shared/             # Tiện ích dùng chung
│   └── Dockerfile
├── frontend/                   # Ứng dụng Next.js
│   ├── src/
│   │   ├── app/                # Next.js app router
│   │   ├── components/         # React components
│   │   └── lib/                # Utilities, API clients
│   └── Dockerfile
├── database/
│   ├── database_schema.sql     # Định nghĩa bảng
│   └── seeding_data.sql        # Dữ liệu mẫu
├── osrm-data/                  # Dữ liệu bản đồ OSRM
├── docker-compose.yml
└── .env.example
```

---

## Tài Khoản Mặc Định

| Vai trò    | Username       | Password      |
| ---------- | -------------- | ------------- |
| Admin      | `admin01`      | `password123` |
| Dispatcher | `dispatcher01` | `password123` |

---

## Giấy Phép

Dự án được phát triển cho mục đích internship / portfolio cá nhân.
