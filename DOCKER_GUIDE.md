# 🐳 Docker Infrastructure Guide

Hướng dẫn cài đặt và vận hành toàn bộ hạ tầng Docker cho **Logistics Control Hub**.

---

## ⚡ Quick Start

```bash
# 1. Tạo file môi trường
cp .env.example .env
# Chỉnh sửa .env với các giá trị thực của bạn

# 2. Setup OSRM (xem phần bên dưới)

# 3. Khởi động tất cả dịch vụ
docker-compose up -d

# 4. Kiểm tra trạng thái
docker-compose ps
```

---

## 🗺️ OSRM Setup (Bắt buộc)

OSRM cần dữ liệu bản đồ Việt Nam để tính toán khoảng cách đường thực tế.

### Bước 1: Tải dữ liệu OpenStreetMap

```bash
cd osrm-data

# Tải bản đồ Việt Nam (khoảng 100MB)
curl -O https://download.geofabrik.de/asia/vietnam-latest.osm.pbf
```

### Bước 2: Xử lý dữ liệu với OSRM

```bash
# Trích xuất (Extract)
docker run -t -v $(pwd):/data ghcr.io/project-osrm/osrm-backend:v5.27.1 \
  osrm-extract -p /opt/car.lua /data/vietnam-latest.osm.pbf

# Phân vùng (Partition)
docker run -t -v $(pwd):/data ghcr.io/project-osrm/osrm-backend:v5.27.1 \
  osrm-partition /data/vietnam-latest.osrm

# Tùy chỉnh (Customize)
docker run -t -v $(pwd):/data ghcr.io/project-osrm/osrm-backend:v5.27.1 \
  osrm-customize /data/vietnam-latest.osrm
```

> ⏱️ Quá trình này mất khoảng 5-15 phút tùy máy.

### Bước 3: Đổi tên file

Cập nhật `docker-compose.yml` nếu cần, đảm bảo đường dẫn file `.osrm` khớp:

```yaml
osrm:
  command: osrm-routed --algorithm mld /data/vietnam-latest.osrm
```

---

## 🔧 Services Overview

### PostgreSQL (Port 5432)

- **Database**: `logistics_db`
- **Connection**: `jdbc:postgresql://localhost:5432/logistics_db`
- Schema & seed data được tự động import khi container khởi động lần đầu
- Data được lưu trữ persistent tại volume `postgres_data`

### Redis (Port 6379)

- **Purpose**: Cache OSRM API responses, metadata entities
- **Connection**: `redis://localhost:6379`
- Yêu cầu password (cấu hình trong `.env`)

### OSRM (Port 5000)

- **Purpose**: Tính toán khoảng cách và thời gian di chuyển theo đường thực
- **Test**: `curl http://localhost:5000/health`

### Backend – Spring Boot (Port 8080)

- **API Base**: `http://localhost:8080/api/v1`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Health**: `http://localhost:8080/actuator/health`

### Frontend – Next.js (Port 3000)

- **URL**: `http://localhost:3000`

---

## 📋 Common Commands

```bash
# Khởi động tất cả services
docker-compose up -d

# Khởi động service cụ thể
docker-compose up -d postgres redis

# Xem logs
docker-compose logs -f
docker-compose logs -f backend
docker-compose logs -f postgres

# Kiểm tra trạng thái + health
docker-compose ps

# Dừng tất cả services
docker-compose down

# Dừng và xóa toàn bộ data (reset hoàn toàn)
docker-compose down -v

# Rebuild image sau khi thay đổi code
docker-compose build backend
docker-compose up -d backend
```

---

## 🌐 Environment Variables

File `.env` cần có các biến sau:

```env
# ── Database ──────────────────────
DB_NAME=logistics_db
DB_USERNAME=postgres
DB_PASSWORD=your_secure_db_password
DB_PORT=5432

# ── Redis ─────────────────────────
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# ── JWT ───────────────────────────
JWT_SECRET=min_32_chars_secret_key_here
JWT_REFRESH_SECRET=min_32_chars_refresh_secret_here

# ── Server ────────────────────────
SERVER_PORT=8080
```

---

## 🔍 Troubleshooting

### PostgreSQL không kết nối được

```bash
docker-compose logs postgres
docker-compose restart postgres
# Kiểm tra biến DB_USERNAME, DB_PASSWORD trong .env
```

### Redis lỗi xác thực

```bash
docker-compose logs redis
# Đảm bảo REDIS_PASSWORD khớp giữa .env và redis command
```

### OSRM trả về lỗi

```bash
docker-compose logs osrm
# Kiểm tra file .osrm đã được xử lý chưa (xem phần OSRM Setup)
# Đảm bảo tên file trong command khớp với file trong osrm-data/
```

### Backend không start

```bash
docker-compose logs backend
# Kiểm tra PostgreSQL và Redis đã healthy chưa
docker-compose ps
```

### Reset hoàn toàn

```bash
docker-compose down -v
docker-compose up -d
```

---

## ✅ Health Checks

| Service    | Endpoint / Command     | Thời gian sẵn sàng |
| ---------- | ---------------------- | ------------------ |
| PostgreSQL | `pg_isready`           | ~5 giây            |
| Redis      | `redis-cli ping`       | ~3 giây            |
| OSRM       | `GET /health`          | ~10 giây           |
| Backend    | `GET /actuator/health` | ~30 giây           |
| Frontend   | `GET /`                | ~15 giây           |

```bash
# Check manual
docker-compose ps  # "Status" column must show "healthy"
curl http://localhost:8080/actuator/health
curl http://localhost:5000/health
```

---

## 🌐 VPS Deployment

### How OSRM data works on a VPS

The `docker-compose.yml` mounts a local directory into the OSRM container:

```yaml
osrm:
  volumes:
    - ./osrm-data:/data # local osrm-data/ → /data inside container
```

This means you need to prepare the OSRM processed files **directly on the VPS** — do NOT upload them from your machine (they are 2–5 GB after processing).

### Step-by-step VPS setup

```bash
# 1. Clone the repository
git clone https://github.com/ToanKhuongDEV/Logistics-Control-Hub.git
cd Logistics-Control-Hub

# 2. Download Vietnam map data (~100 MB)
cd osrm-data
wget https://download.geofabrik.de/asia/vietnam-latest.osm.pbf

# 3. Process the map data (takes ~10-20 minutes)
docker run -t -v "$(pwd):/data" ghcr.io/project-osrm/osrm-backend:v5.27.1 \
  osrm-extract -p /opt/car.lua /data/vietnam-latest.osm.pbf

docker run -t -v "$(pwd):/data" ghcr.io/project-osrm/osrm-backend:v5.27.1 \
  osrm-partition /data/vietnam-latest.osrm

docker run -t -v "$(pwd):/data" ghcr.io/project-osrm/osrm-backend:v5.27.1 \
  osrm-customize /data/vietnam-latest.osrm

# 4. Set up environment variables
cd ..
cp backend/.env.example backend/.env
# Edit backend/.env with production values
nano backend/.env

# 5. Start all services
docker-compose up -d

# 6. Verify OSRM is working
curl http://localhost:5000/health
```

### Why process on VPS instead of uploading?

| Method                                    | Transfer size                        | Speed                    |
| ----------------------------------------- | ------------------------------------ | ------------------------ |
| Upload processed files from local machine | 2–5 GB                               | Slow, depends on network |
| Download `.osm.pbf` + process on VPS      | ~100 MB download, ~15 min processing | **Much faster**          |

### Add osrm-data to .gitignore

Make sure the large processed files are never committed to Git:

```gitignore
# OSRM processed map data (too large for Git)
osrm-data/*.osm.pbf
osrm-data/*.osrm
osrm-data/*.osrm.*
```
