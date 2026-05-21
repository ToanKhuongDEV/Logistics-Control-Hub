# Logistics Control Hub

Copyright (c) 2026 Khương Xuân Toàn

Logistics Control Hub là nền tảng quản lý vận hành logistics full-stack, dùng để quản lý kho, xe, tài xế, đơn hàng, tối ưu tuyến, ca giao của tài xế, xuất Excel và audit log trên cùng một hệ thống.

## Tổng Quan

Dự án mô phỏng một logistics control tower tập trung vào vận hành giao hàng theo kho và tối ưu chặng cuối.

Luồng sử dụng chính:

1. Admin đăng nhập và quản lý thông tin công ty, tài khoản, kho, tài xế, xe và đơn hàng.
2. Dispatcher thao tác trong phạm vi kho được phân công.
3. Đơn hàng có thể được tạo thủ công, gán kho trực tiếp hoặc tự gán vào kho đang hoạt động gần nhất trong phạm vi truy cập.
4. Chạy tối ưu tuyến cho một kho với các đơn `CREATED` và xe `ACTIVE` đã có tài xế.
5. Hệ thống lưu kết quả tuyến, điểm dừng, polyline, khoảng cách, thời gian, chi phí và lịch sử chạy tuyến.
6. Tài xế đăng nhập vào portal riêng để xem đơn đang giao và xác nhận hoàn tất giao hàng.
7. Các thao tác quan trọng được ghi audit log để truy vết.

## Tính Năng Chính

### Xác Thực Và Phân Quyền

- JWT access token và refresh token được lưu bằng HttpOnly cookie.
- Có API đăng nhập, refresh token, đăng xuất, lấy thông tin người dùng hiện tại, đổi mật khẩu, quên mật khẩu và reset mật khẩu.
- Hỗ trợ role: `ADMIN`, `DISPATCHER`, `DRIVER`.
- Backend trả danh sách permission qua `/api/v1/auth/me`; frontend dùng permission này để ẩn/hiện menu.
- Dispatcher bị giới hạn theo phạm vi kho được phân công.

### Quản Trị Và Điều Phối

- Quản lý đơn hàng, xe, tài xế, kho, tài khoản và thông tin công ty.
- Các màn hình chính có phân trang, tìm kiếm và lọc dữ liệu.
- Dashboard thống kê tổng quan vận hành.
- Admin quản lý tài khoản, role và danh sách kho được phân công.
- Audit log có thể lọc theo người thao tác, hành động, loại tài nguyên, phạm vi kho, trạng thái và khoảng thời gian.

### Đơn Hàng

- Vòng đời đơn hàng: `CREATED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`.
- Tự sinh mã đơn nếu người dùng không nhập mã.
- Admin có thể tạo đơn không chọn kho; hệ thống tự chọn kho active gần nhất.
- Hỗ trợ cập nhật trạng thái hàng loạt.
- Ghi audit log cho tạo, sửa, xóa mềm và bulk update.

### Xe, Tài Xế Và Kho

- Trạng thái xe: `ACTIVE`, `MAINTENANCE`, `IDLE`.
- Loại xe: `KG_500`, `KG_750`, `T_1`, `T_1_25`, `T_1_49`.
- Xe có cấu hình tải trọng, thể tích và chi phí/km.
- API lấy danh sách tài xế khả dụng.
- Admin có quyền chuyển kho hàng loạt cho xe.

### Tối Ưu Tuyến

- Tối ưu tuyến theo từng kho bằng Google OR-Tools.
- Lấy ma trận khoảng cách, thời gian và polyline từ OSRM.
- Có fallback Haversine nếu OSRM không khả dụng.
- Redis cache cho các truy vấn ma trận OSRM tốn kém.
- Trạng thái lần chạy routing: `COMPLETED`, `FAILED`.
- Trạng thái route: `CREATED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.
- Sau khi tối ưu thành công, đơn được chuyển sang `IN_TRANSIT` và gán tài xế theo xe được solver chọn.

### Portal Tài Xế

- Màn hình tài xế nằm tại `/driver`.
- API prefix: `/api/v1/driver`.
- Tài xế xem các đơn `IN_TRANSIT` được gán cho mình.
- Tài xế có thể xác nhận hoàn tất đơn của chính mình.
- Route tự chuyển sang `COMPLETED` khi tất cả order stop trong route đã giao xong.

### Excel

- Export endpoint: `GET /api/v1/excel/export`.
- Template endpoint: `GET /api/v1/excel/template`.
- Loại file hỗ trợ: `DEPOT`, `DRIVER`, `ORDER`, `ROUTING`, `VEHICLE`.
- Bộ lọc export gồm search, status, depot, khoảng ngày và `maxRows`.

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.4.4
- Spring Web, Validation, Security, OAuth2 Resource Server, Data JPA, Mail, Actuator, WebSocket
- PostgreSQL
- Redis
- Google OR-Tools `9.8.3296`
- Temporal SDK `1.24.0` đang có trong dependency, nhưng hiện chưa có workflow Temporal được nối vào app
- MapStruct `1.5.5.Final`
- Lombok `1.18.36`
- SpringDoc OpenAPI `2.8.5`
- Apache POI `5.3.0`
- JJWT `0.11.5`
- spring-dotenv `4.0.0`

### Frontend

- Next.js 16
- React 19
- TypeScript
- Tailwind CSS v4
- Radix UI
- Component nội bộ theo phong cách shadcn/ui
- Axios
- Leaflet
- Recharts
- React Hook Form + Zod
- Framer Motion

### Hạ Tầng

- Docker và Docker Compose
- PostgreSQL 15 Alpine
- Redis external hoặc Redis Cloud
- OSRM container dùng dữ liệu bản đồ từ `osrm-data/data-HANOI`

## Kiến Trúc

```text
Frontend (Next.js)
  -> Trang auth và các trang dashboard có bảo vệ đăng nhập
  -> Orders, fleet, drivers, depots, history, driver portal, accounts, audit, settings
  -> Gọi REST API backend bằng Axios có withCredentials

Backend (Spring Boot)
  -> Auth và phân quyền
  -> Company, dashboard, depot, driver, order, vehicle
  -> Routing với OR-Tools, OSRM và Redis cache
  -> Driver portal
  -> Excel export/template
  -> Audit logging

Dữ liệu và dịch vụ
  -> PostgreSQL lưu dữ liệu vận hành
  -> Redis cache kết quả OSRM
  -> OSRM tính khoảng cách và polyline theo mạng lưới đường
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
|   |       |-- driverportal/
|   |       |-- excel/
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
|-- document/
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
  - OSRM nếu muốn routing theo mạng lưới đường khi chạy local

### Chạy Bằng Docker Compose

1. Tạo file `.env` ở thư mục gốc với các biến trong phần bên dưới.
2. Chuẩn bị OSRM trong `osrm-data/data-HANOI`, bảo đảm trong container có file `/data/hanoi.osrm`.
3. Chạy stack:

```bash
docker compose up -d --build
```

4. Truy cập các dịch vụ:

| Dịch vụ | URL |
| --- | --- |
| Frontend | `http://localhost:3000` |
| Backend API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Health Check | `http://localhost:8080/actuator/health` |
| OSRM | `http://localhost:5000` |

Lưu ý:

- PostgreSQL trong compose không expose port ra host theo mặc định.
- Redis không nằm trong compose hiện tại, cần cấu hình Redis external.
- `NEXT_PUBLIC_API_URL` được dùng tại thời điểm build frontend.

### Chạy Local

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Biến Môi Trường

Ví dụ `.env` ở thư mục gốc cho Docker Compose:

```env
DB_NAME=logistics_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432

REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

JWT_SECRET=change_me_to_a_32_char_or_longer_secret
JWT_REFRESH_SECRET=change_me_to_another_32_char_secret

SERVER_PORT=8080
FRONTEND_URL=http://localhost:3000
RESET_PASSWORD_EXPIRATION_MINUTES=15

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_app_password

NEXT_PUBLIC_API_URL=http://localhost:8080
DOCKERHUB_USERNAME=logistics
```

Khi chạy backend local, xem `backend/.env.example`. Khi chạy frontend local, copy `frontend/.env.local.example` thành `frontend/.env.local`.

## API Chính

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

| Nhóm | Prefix |
| --- | --- |
| Auth | `/api/v1/auth` |
| Accounts | `/api/v1/auth/accounts` |
| Orders | `/api/v1/orders` |
| Vehicles | `/api/v1/vehicles` |
| Drivers | `/api/v1/drivers` |
| Driver Portal | `/api/v1/driver` |
| Depots | `/api/v1/depots` |
| Dashboard | `/api/v1/dashboard` |
| Company | `/api/v1/company` |
| Routing | `/api/v1/routing` |
| Audit Logs | `/api/v1/audit-logs` |
| Excel | `/api/v1/excel` |

Ví dụ:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `GET /api/v1/orders`
- `PATCH /api/v1/orders/bulk/status`
- `PATCH /api/v1/vehicles/bulk/depot`
- `POST /api/v1/routing/optimize?depotId=1`
- `GET /api/v1/routing/runs/{id}`
- `GET /api/v1/routing/latest/{depotId}`
- `GET /api/v1/routing/history/{depotId}`
- `GET /api/v1/driver/me/orders`
- `PATCH /api/v1/driver/me/orders/{orderId}/complete`
- `GET /api/v1/excel/export?type=ORDER`
- `GET /api/v1/audit-logs`

## Dữ Liệu Demo

Seed hiện tại cung cấp:

- 1 công ty
- 12 tài khoản người dùng
- 12 tài xế
- 64 địa điểm
- 4 kho
- 14 xe
- 60 đơn hàng
- 1 lần chạy routing mẫu
- 1 route mẫu
- 7 route stop
- 1 audit log

Tài khoản demo mặc định:

| Vai trò | Username | Password |
| --- | --- | --- |
| Admin | `admin01` | `password123` |
| Dispatcher | `user01` | `password123` |

`user01` được seed là dispatcher và được phân công kho `1` và `2`.

## Kiểm Thử

Backend test:

```bash
cd backend
mvn test
```

Frontend lint:

```bash
cd frontend
npm run lint
```

## Tài Liệu

- README tiếng Việt: `README_VIE.md`
- README tiếng Anh: `README_ENG.md`
- Docker guide: `DOCKER_GUIDE.md`
- Ghi chú trạng thái triển khai và roadmap: `PLAN.md`
- Ghi chú ràng buộc nghiệp vụ: `document/business-restrictions.md`

## Giấy Phép

Dự án được phát hành theo GNU General Public License v3.0.
