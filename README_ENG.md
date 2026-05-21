# Logistics Control Hub

Copyright (c) 2026 Khuong Xuan Toan

Logistics Control Hub is a full-stack logistics operations platform for managing depots, vehicles, drivers, orders, route optimization, driver delivery work, Excel exports, and audit visibility in one place.

## Overview

The project simulates a logistics control tower focused on depot-based delivery operations and last-mile route planning.

Main workflow:

1. Admin signs in and manages company data, accounts, depots, drivers, vehicles, and orders.
2. Dispatchers work inside their assigned depot scope.
3. Orders can be created manually and assigned to a depot, or auto-assigned to the nearest accessible active depot.
4. Routing optimization selects `CREATED` orders and `ACTIVE` vehicles with assigned drivers for a depot.
5. Routes are saved with stops, route geometry, distance, duration, cost, and run history.
6. Driver users open the driver portal, view their assigned in-transit orders, and mark deliveries as completed.
7. Sensitive operations are recorded in audit logs for traceability.

## Core Features

### Authentication and Authorization

- JWT access and refresh tokens stored as HttpOnly cookies.
- Login, refresh, logout, current user, change password, forgot password, and reset password APIs.
- Role model: `ADMIN`, `DISPATCHER`, `DRIVER`.
- Permission model returned from `/api/v1/auth/me` and used by the frontend sidebar.
- Depot-scoped authorization for dispatcher users.

### Admin and Dispatcher Operations

- CRUD-style management for orders, vehicles, drivers, depots, accounts, and company settings.
- Paginated lists with search/filter support across the main modules.
- Dashboard statistics for high-level operations.
- Account management for admin users, including role and depot assignment.
- Audit log search by actor, action, resource, depot scope, status, and date range.

### Orders

- Order lifecycle: `CREATED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`.
- Auto-generated order code when no code is provided.
- Nearest active depot assignment when an admin creates an order without a depot.
- Bulk status update endpoint.
- Audit logging for create, update, delete, and bulk update operations.

### Fleet, Drivers, and Depots

- Vehicle statuses: `ACTIVE`, `MAINTENANCE`, `IDLE`.
- Vehicle types: `KG_500`, `KG_750`, `T_1`, `T_1_25`, `T_1_49`.
- Vehicle capacity fields for weight and volume.
- Vehicle operating cost per kilometer.
- Driver availability lookup.
- Bulk vehicle-to-depot reassignment with admin permission.

### Routing

- Depot-based route optimization with Google OR-Tools.
- OSRM distance matrix, duration matrix, and polyline retrieval.
- Haversine fallback when OSRM is unavailable.
- Redis-backed cache for expensive OSRM matrix calls.
- Routing run statuses: `COMPLETED`, `FAILED`.
- Route statuses: `CREATED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.
- Automatic order update to `IN_TRANSIT` and driver assignment after successful routing.

### Driver Portal

- Driver-facing page at `/driver`.
- Driver API prefix: `/api/v1/driver`.
- Drivers can view assigned in-transit delivery orders.
- Drivers can complete their own orders.
- Route status is moved to `COMPLETED` when all order stops on that route are delivered.

### Excel

- Export endpoint: `GET /api/v1/excel/export`.
- Template endpoint: `GET /api/v1/excel/template`.
- Supported export/template types: `DEPOT`, `DRIVER`, `ORDER`, `ROUTING`, `VEHICLE`.
- Export filters include search, status, depot, date range, and `maxRows`.

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.4.4
- Spring Web, Validation, Security, OAuth2 Resource Server, Data JPA, Mail, Actuator, WebSocket
- PostgreSQL
- Redis
- Google OR-Tools `9.8.3296`
- Temporal SDK `1.24.0` is present as a dependency, but no Temporal workflow is currently wired into the app
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
- Local shadcn-style components
- Axios
- Leaflet
- Recharts
- React Hook Form + Zod
- Framer Motion

### Infrastructure

- Docker and Docker Compose
- PostgreSQL 15 Alpine
- External Redis or Redis Cloud
- OSRM container using map data from `osrm-data/data-HANOI`

## Architecture

```text
Frontend (Next.js)
  -> Auth pages and protected dashboard pages
  -> Orders, fleet, drivers, depots, route history, driver portal, accounts, audit, settings
  -> Calls backend REST APIs with credentials-enabled Axios

Backend (Spring Boot)
  -> Auth and authorization
  -> Company, dashboard, depot, driver, order, vehicle
  -> Routing with OR-Tools, OSRM, and Redis cache
  -> Driver portal
  -> Excel export/template
  -> Audit logging

Data and services
  -> PostgreSQL for operational data
  -> Redis for OSRM cache
  -> OSRM for road-network distance and route geometry
```

## Project Structure

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

## Getting Started

### Prerequisites

- Docker Desktop and Docker Compose
- Or for local development:
  - Java 17
  - Maven
  - Node.js 20+
  - PostgreSQL 15
  - Redis or Redis Cloud access
  - OSRM if you want road-network routing locally

### Run with Docker Compose

1. Create a root `.env` file with the variables listed below.
2. Prepare OSRM files in `osrm-data/data-HANOI`, with `/data/hanoi.osrm` available inside the OSRM container.
3. Start the stack:

```bash
docker compose up -d --build
```

4. Open the services:

| Service | URL |
| --- | --- |
| Frontend | `http://localhost:3000` |
| Backend API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Health Check | `http://localhost:8080/actuator/health` |
| OSRM | `http://localhost:5000` |

Notes:

- PostgreSQL is internal to Docker Compose by default and is not exposed to the host.
- Redis is external and must be configured through environment variables.
- `NEXT_PUBLIC_API_URL` is passed at frontend build time.

### Run Locally

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

## Environment Variables

Root `.env` example for Docker Compose:

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

For local backend development, `backend/.env.example` contains the backend-specific variables. For local frontend development, copy `frontend/.env.local.example` to `frontend/.env.local`.

## API Highlights

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

| Area | Endpoint Prefix |
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

Examples:

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

## Demo Data

The current seed script provides:

- 1 company
- 12 user accounts
- 12 drivers
- 64 locations
- 4 depots
- 14 vehicles
- 60 orders
- 1 sample routing run
- 1 sample route
- 7 route stops
- 1 audit log row

Default demo credentials:

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin01` | `password123` |
| Dispatcher | `user01` | `password123` |

`user01` is seeded as a dispatcher assigned to depots `1` and `2`.

## Tests

Backend tests live in `backend/src/test/java`.

```bash
cd backend
mvn test
```

Frontend lint:

```bash
cd frontend
npm run lint
```

## Documentation

- Vietnamese README: `README_VIE.md`
- English README: `README_ENG.md`
- Docker guide: `DOCKER_GUIDE.md`
- Current implementation notes and roadmap: `PLAN.md`
- Business restriction notes: `document/business-restrictions.md`

## License

This project is licensed under the GNU General Public License v3.0.
