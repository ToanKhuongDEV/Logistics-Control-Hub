# Logistics Control Hub

Copyright (c) 2026 Khương Xuân Toàn

Logistics Control Hub là nền tảng logistics full-stack dùng để quản lý kho, xe, tài xế, đơn hàng, tối ưu tuyến và giám sát audit trên cùng một hệ thống.

Hệ thống kết hợp:
- Spring Boot cho backend API
- Next.js cho giao diện web
- PostgreSQL cho dữ liệu vận hành
- Redis cho caching
- OSRM để tính khoảng cách theo mạng lưới đường thực tế
- Google OR-Tools để tối ưu bài toán định tuyến xe

## Mục Lục

- [Tổng Quan](#tổng-quan)
- [Tính Năng Chính](#tính-năng-chính)
- [Tech Stack](#tech-stack)
- [Kiến Trúc](#kiến-trúc)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Cài Đặt Và Chạy](#cài-đặt-và-chạy)
- [Biến Môi Trường](#biến-môi-trường)
- [API Chính](#api-chính)
- [Dữ Liệu Demo](#dữ-liệu-demo)

## Tổng Quan

Đây là dự án mô phỏng một AI supply chain control tower tập trung vào vận hành giao hàng theo kho và tối ưu chặng cuối.

Luồng sử dụng chính:
1. Tạo và quản lý đơn hàng.
2. Gán hoặc kiểm tra phạm vi kho phụ trách.
3. Quản lý xe, tài xế và tài khoản điều phối.
4. Chạy tối ưu tuyến cho từng kho.
5. Xem kết quả tuyến và lịch sử chạy tuyến.
6. Theo dõi các thao tác nhạy cảm qua audit log.

So với README cũ, code hiện tại đã có thêm các phần quản lý tài khoản, reset mật khẩu, cấu hình công ty và màn hình audit.

## Tính Năng Chính

### Xác Thực Và Phân Quyền

- Đăng nhập, refresh token, logout và lấy thông tin người dùng hiện tại bằng JWT
- Hỗ trợ role: `ADMIN`, `DISPATCHER`, `DRIVER`
- Giao diện và API kiểm soát theo permission
- Đổi mật khẩu, quên mật khẩu và đặt lại mật khẩu

### Quản Lý Đơn Hàng

- Tạo, xem danh sách, cập nhật và xóa đơn hàng
- Hỗ trợ lọc và phân trang
- API thống kê đơn hàng
- Cập nhật trạng thái hàng loạt
- Vòng đời đơn như `CREATED`, `IN_TRANSIT`, `DELIVERED`

### Quản Lý Đội Xe Và Tài Xế

- CRUD cho xe, tài xế và kho
- API thống kê xe và tài xế
- Chuyển kho cho nhiều xe cùng lúc
- Cấu hình tải trọng, thể tích và chi phí/km cho xe
- Lấy danh sách tài xế khả dụng

### Tối Ưu Tuyến

- Tối ưu tuyến theo từng kho với Google OR-Tools
- Tính khoảng cách và thời gian bằng OSRM
- Lưu lịch sử mỗi lần chạy tối ưu
- Lấy tuyến mới nhất theo kho
- Xem lịch sử tuyến theo kho

### Dashboard Và Quản Trị

- Dashboard thống kê vận hành tổng quan
- Quản lý thông tin công ty
- Quản lý tài khoản dành cho admin
- Audit log có bộ lọc theo hành động, tài nguyên, người thao tác, kho và thời gian

### Caching Và Độ Ổn Định

- Redis cache cho các truy vấn routing tốn kém
- Health endpoint bằng Spring Boot Actuator
- Stack Docker giúp dựng môi trường nhanh và đồng nhất

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.4.4
- Spring Web, Validation, Security, Data JPA, Mail, Actuator, WebSocket
- PostgreSQL
- Redis
- Google OR-Tools `9.8.3296`
- Temporal SDK `1.24.0` có trong dependencies
- MapStruct
- Lombok
- SpringDoc OpenAPI / Swagger UI
- JJWT `0.11.5`

### Frontend

- Next.js 16
- React 19
- TypeScript
- Tailwind CSS v4
- Radix UI
- Cấu trúc component theo kiểu shadcn/ui
- Leaflet
- Recharts
- React Hook Form + Zod
- Axios
- Framer Motion

### Hạ Tầng

- Docker và Docker Compose
- PostgreSQL 15 Alpine
- Redis Cloud hoặc Redis ngoài hệ thống
- OSRM container với dữ liệu bản đồ Hà Nội

## Kiến Trúc

```text
Frontend (Next.js)
  -> Các trang dashboard có đăng nhập
  -> Orders, fleet, drivers, depots, accounts, audit, settings
  -> Gọi REST API tới backend

Backend (Spring Boot)
  -> Auth
  -> Company
  -> Dashboard
  -> Depot
  -> Driver
  -> Order
  -> Routing
  -> Vehicle
  -> Audit
  -> Tích hợp Redis cache

Hạ tầng
  -> PostgreSQL lưu dữ liệu nghiệp vụ
  -> Redis cho cache
  -> OSRM tính khoảng cách đường thực tế
```

## Cấu Trúc Dự Án

```text
Logistics Control Hub/
|-- backend/
|   |-- src/main/java/com/logistics/hub/
|   |   |-- common/
|   |   |-- config/
|   |   `-- feature/
|   |       |-- audit/
|   |       |-- auth/
|   |       |-- company/
|   |       |-- dashboard/
|   |       |-- depot/
|   |       |-- driver/
|   |       |-- geocoding/
|   |       |-- location/
|   |       |-- order/
|   |       |-- redis/
|   |       |-- routing/
|   |       |-- user/
|   |       `-- vehicle/
|   `-- src/main/resources/application.yml
|-- frontend/
|   |-- src/app/
|   |-- src/components/
|   |-- src/contexts/
|   |-- src/lib/
|   `-- src/types/
|-- database/
|   |-- database_schema.sql
|   `-- seeding_data.sql
|-- osrm-data/
|-- docker-compose.yml
|-- DOCKER_GUIDE.md
|-- README_ENG.md
`-- README_VIE.md
```

## Cài Đặt Và Chạy

### Yêu Cầu

- Docker Desktop và Docker Compose
- Hoặc nếu chạy local:
  - Java 17
  - Maven
  - Node.js 20+
  - PostgreSQL 15
  - Redis hoặc Redis Cloud

### Cách 1: Chạy Bằng Docker Compose

1. Tạo file `.env` ở thư mục gốc với các biến cần thiết.
2. Chuẩn bị dữ liệu OSRM trong `osrm-data/data-HANOI` theo hướng dẫn ở `DOCKER_GUIDE.md`.
3. Khởi động toàn bộ stack:

```bash
docker compose up -d --build
```

4. Truy cập các dịch vụ:

| Dịch vụ | URL |
| --- | --- |
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |
| OSRM | http://localhost:5000 |

Lưu ý:
- Backend trong Docker sẽ dùng PostgreSQL từ `docker-compose.yml`.
- Redis không được dựng trong file compose hiện tại, nên cần cấu hình Redis ngoài.

### Cách 2: Chạy Local

#### Backend

```bash
cd backend
mvn spring-boot:run
```

Backend đọc cấu hình từ biến môi trường và file `.env` ở thư mục gốc thông qua `spring-dotenv`.

#### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Biến Môi Trường

Tạo file `.env` ở thư mục gốc với giá trị tương tự:

```env
DB_NAME=logistics_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/logistics_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

JWT_SECRET=your_jwt_secret_min_32_chars
JWT_REFRESH_SECRET=your_refresh_secret_min_32_chars

SERVER_PORT=8080
FRONTEND_URL=http://localhost:3000
RESET_PASSWORD_EXPIRATION_MINUTES=15

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password

NEXT_PUBLIC_API_URL=http://localhost:8080
OSRM_URL=http://localhost:5000
```

Quan trọng:
- `SPRING_DATASOURCE_*` là bắt buộc theo `application.yml`.
- Khi chạy Docker Compose, backend dùng `jdbc:postgresql://postgres:5432/...`.
- `NEXT_PUBLIC_API_URL` được dùng ở thời điểm build frontend.

## API Chính

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Các nhóm endpoint chính:

| Nhóm | Prefix |
| --- | --- |
| Auth | `/api/v1/auth` |
| Accounts | `/api/v1/auth/accounts` |
| Orders | `/api/v1/orders` |
| Vehicles | `/api/v1/vehicles` |
| Drivers | `/api/v1/drivers` |
| Depots | `/api/v1/depots` |
| Dashboard | `/api/v1/dashboard` |
| Company | `/api/v1/company` |
| Routing | `/api/v1/routing` |
| Audit Logs | `/api/v1/audit-logs` |

Ví dụ:

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `GET /api/v1/orders`
- `PATCH /api/v1/orders/bulk/status`
- `PATCH /api/v1/vehicles/bulk/depot`
- `POST /api/v1/routing/optimize`
- `GET /api/v1/routing/latest/{depotId}`
- `GET /api/v1/routing/history/{depotId}`
- `GET /api/v1/audit-logs`

## Dữ Liệu Demo

File seed hiện tại cung cấp:
- 1 công ty
- 11 tài khoản người dùng
- 12 tài xế
- 4 kho
- 14 xe
- 60 đơn hàng
- Bộ địa điểm mẫu thực tế tại Việt Nam

Tài khoản demo mặc định:

| Vai trò | Username | Password |
| --- | --- | --- |
| Admin | `admin01` | `password123` |
| Dispatcher | `user01` | `password123` |

`user01` được seed là dispatcher và đang phụ trách kho `1` và `2`.

## Tác Giả

- Chủ sở hữu: Khương Xuân Toàn
- Liên hệ: `khuongxuantoan@gmail.com`

## Giấy Phép

Dự án này được phát hành theo GNU General Public License v3.0.

## Ghi Chú

- `frontend/README.md` hiện vẫn là README mặc định của Next.js, có thể cập nhật riêng nếu muốn tài liệu frontend chi tiết hơn.
- Hai file tài liệu gốc của repo là `README_ENG.md` và `README_VIE.md`.
