# 🐳 Docker Infrastructure Guide

Hướng dẫn cài đặt và vận hành toàn bộ hạ tầng Docker cho **Logistics Control Hub**.

## Stack Hiện Tại

`docker-compose.yml` đang chạy 4 service:

| Service | Container | Port host | Ghi chú |
| --- | --- | --- | --- |
| PostgreSQL | `logistics-postgres` | Không expose mặc định | Backend kết nối qua Docker network |
| OSRM | `logistics-osrm` | `5000` | Đọc dữ liệu từ `./osrm-data/data-HANOI` |
| Backend | `logistics-backend` | `${SERVER_PORT:-8080}` | Spring Boot API |
| Frontend | `logistics-frontend` | `3000` | Next.js standalone |

Redis không được dựng thành container trong compose hiện tại. Backend cần kết nối tới Redis bên ngoài, ví dụ Redis Cloud hoặc một Redis local do bạn tự chạy riêng.

## Quick Start

1. Tạo file `.env` ở thư mục gốc repo:

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

2. Chuẩn bị dữ liệu OSRM tại `osrm-data/data-HANOI`.

3. Build và chạy stack:

```bash
docker compose up -d --build
```

4. Kiểm tra trạng thái:

```bash
docker compose ps
docker compose logs -f backend
```

## URL Dịch Vụ

| Dịch vụ | URL |
| --- | --- |
| Frontend | `http://localhost:3000` |
| Backend API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Health check | `http://localhost:8080/actuator/health` |
| OSRM | `http://localhost:5000` |

## OSRM Setup

Compose mount thư mục sau vào container OSRM:

```yaml
volumes:
  - ./osrm-data/data-HANOI:/data
command: osrm-routed --algorithm mld /data/hanoi.osrm
```

Vì vậy trong `osrm-data/data-HANOI` cần có các file đã xử lý với tên gốc `hanoi.osrm`.

Ví dụ nếu bạn có file OSM/PBF cho Hà Nội:

```bash
cd osrm-data/data-HANOI

docker run -t -v "${PWD}:/data" osrm/osrm-backend:latest \
  osrm-extract -p /opt/car.lua /data/hanoi.osm.pbf

docker run -t -v "${PWD}:/data" osrm/osrm-backend:latest \
  osrm-partition /data/hanoi.osrm

docker run -t -v "${PWD}:/data" osrm/osrm-backend:latest \
  osrm-customize /data/hanoi.osrm
```

Trên PowerShell, `${PWD}` thường hoạt động trong Docker Desktop. Nếu gặp lỗi mount path, dùng đường dẫn tuyệt đối tới thư mục `osrm-data/data-HANOI`.

Backend trong Docker nhận `OSRM_URL=http://osrm:5000` từ `docker-compose.yml`. Khi chạy backend local, giá trị mặc định trong `application.yml` là `http://localhost:5000`.

## Database

PostgreSQL sử dụng image `postgres:15-alpine`.

- Database: `${DB_NAME:-logistics_db}`
- User: `${DB_USERNAME:-postgres}`
- Password: `${DB_PASSWORD:-postgres}`
- Volume: `postgres_data`
- Init scripts:
  - `database/database_schema.sql`
  - `database/seeding_data.sql`

Lưu ý: PostgreSQL chỉ import schema/seed khi volume mới được tạo lần đầu. Nếu đã có volume cũ và muốn import lại từ đầu:

```bash
docker compose down -v
docker compose up -d --build
```

## Redis

Compose hiện tại không khai báo service Redis. Backend cần các biến:

```env
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

`application.yml` tạo Redis URL theo mẫu:

```text
redis://default:${REDIS_PASSWORD}@${REDIS_HOST}:${REDIS_PORT}
```

Nếu muốn chạy Redis local bên ngoài compose, bạn có thể tự chạy:

```bash
docker run --name logistics-redis -p 6379:6379 redis:7-alpine redis-server --requirepass your_redis_password
```

Sau đó đặt `REDIS_HOST=host.docker.internal` khi backend chạy trong Docker, hoặc `REDIS_HOST=localhost` khi backend chạy local.

## Backend

Backend image build từ `backend/Dockerfile`:

- Build bằng Maven và Java 17
- Runtime bằng `eclipse-temurin:17-jre-jammy`
- Jar output: `target/app.jar`
- Port trong container: `8080`

Biến môi trường quan trọng:

| Biến | Mục đích |
| --- | --- |
| `SPRING_DATASOURCE_URL` | Được compose set thành `jdbc:postgresql://postgres:5432/...` |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` | Redis external |
| `JWT_SECRET`, `JWT_REFRESH_SECRET` | Ký JWT access/refresh token |
| `OSRM_URL` | Override `osrm.url` cho backend trong Docker |
| `SPRING_MAIL_*` | Cấu hình gửi email reset mật khẩu |
| `RESET_PASSWORD_EXPIRATION_MINUTES` | Thời gian sống của reset token |

## Frontend

Frontend image build từ `frontend/Dockerfile`:

- Node 20 Alpine
- `npm ci`
- `npm run build`
- Next.js standalone output
- Runtime chạy `node server.js`

`NEXT_PUBLIC_API_URL` phải có ở build time:

```yaml
args:
  NEXT_PUBLIC_API_URL: ${NEXT_PUBLIC_API_URL:-http://localhost:8080}
```

Nếu đổi backend URL sau khi image đã build, cần rebuild frontend image.

## Lệnh Thường Dùng

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f osrm
docker compose down
docker compose down -v
docker compose build backend
docker compose up -d backend
```

## Troubleshooting

### Backend không kết nối được PostgreSQL

```bash
docker compose logs postgres
docker compose logs backend
docker compose ps
```

Kiểm tra `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` trong `.env`. Nếu vừa sửa schema/seed và cần tạo lại DB, chạy `docker compose down -v`.

### Backend không kết nối được Redis

Kiểm tra `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`. Nếu Redis chạy trên máy host và backend chạy trong Docker, thử dùng `host.docker.internal` thay vì `localhost`.

### OSRM không sẵn sàng

```bash
docker compose logs osrm
```

Kiểm tra thư mục `osrm-data/data-HANOI` có file `hanoi.osrm` và các file phụ trợ sau khi `osrm-partition`/`osrm-customize` chưa.

### Frontend gọi sai Backend URL

Kiểm tra `NEXT_PUBLIC_API_URL` trong `.env`, sau đó rebuild frontend:

```bash
docker compose build frontend
docker compose up -d frontend
```

### Reset môi trường từ đầu

```bash
docker compose down -v
docker compose up -d --build
```

Lệnh này xóa volume PostgreSQL, nên dữ liệu local sẽ mất.

## VPS Deployment Notes

Trên VPS nên xử lý OSRM trực tiếp trên máy chủ thay vì upload file đã process, vì bộ file `.osrm*` có thể rất lớn.

Luôn đảm bảo các file OSRM lớn không bị commit:

```gitignore
osrm-data/*.osm.pbf
osrm-data/*.osrm
osrm-data/*.osrm.*
osrm-data/data-HANOI/*.osm.pbf
osrm-data/data-HANOI/*.osrm
osrm-data/data-HANOI/*.osrm.*
```
