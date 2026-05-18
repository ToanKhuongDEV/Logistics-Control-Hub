# Logistics Control Hub

Copyright (c) 2026 Khuong Xuan Toan

Logistics Control Hub is a full-stack logistics operations platform for managing depots, vehicles, drivers, orders, route optimization, and audit visibility in one place.

It combines:
- Spring Boot for the backend API
- Next.js for the web dashboard
- PostgreSQL for operational data
- Redis for caching
- OSRM for road-network distance calculations
- Google OR-Tools for vehicle routing optimization

## Table of Contents

- [Overview](#overview)
- [Core Features](#core-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Highlights](#api-highlights)
- [Demo Data](#demo-data)

## Overview

This project simulates an AI-enabled supply chain control tower focused on last-mile and depot-based delivery planning.

Main workflow:
1. Create and manage orders.
2. Assign or review depot scope.
3. Manage vehicles, drivers, and dispatcher accounts.
4. Run route optimization for a depot.
5. Inspect optimized routes and route history.
6. Track sensitive actions through audit logs.

The current codebase also includes account administration, password reset flow, company settings, and audit monitoring beyond the original MVP scope.

## Core Features

### Authentication and Access Control

- JWT-based login, refresh, logout, and current-user endpoints
- Role support: `ADMIN`, `DISPATCHER`, `DRIVER`
- Permission-based UI and API access
- Change password, forgot password, and reset password flows

### Order Operations

- Create, list, update, and delete orders
- Filter and paginate order data
- Order statistics endpoint
- Bulk status updates
- Delivery lifecycle support such as `CREATED`, `IN_TRANSIT`, and `DELIVERED`

### Fleet and Driver Management

- CRUD for vehicles, drivers, and depots
- Vehicle statistics and driver statistics
- Bulk vehicle-to-depot reassignment
- Vehicle capacity and cost configuration
- Driver availability lookup

### Routing and Optimization

- Depot-based route optimization with Google OR-Tools
- Road distance and travel-time estimation through OSRM
- Optimization run tracking
- Latest route result by depot
- Route history by depot

### Dashboard and Administration

- Dashboard statistics for operational overview
- Company information management
- Account management for admin users
- Audit log search with filters for action, resource, actor, depot scope, and date range

### Caching and Reliability

- Redis-backed caching for expensive routing lookups
- Health endpoint via Spring Boot Actuator
- Dockerized local stack for reproducible setup

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.4.4
- Spring Web, Validation, Security, Data JPA, Mail, Actuator, WebSocket
- PostgreSQL
- Redis
- Google OR-Tools `9.8.3296`
- Temporal SDK `1.24.0` present in dependencies
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
- shadcn/ui-style component structure
- Leaflet
- Recharts
- React Hook Form + Zod
- Axios
- Framer Motion

### Infrastructure

- Docker and Docker Compose
- PostgreSQL 15 Alpine
- Redis Cloud or external Redis connection
- OSRM container with Hanoi map data

## Architecture

```text
Frontend (Next.js)
  -> Authenticated dashboard pages
  -> Orders, fleet, drivers, depots, accounts, audit, settings
  -> Calls REST APIs on the backend

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
  -> Redis cache integration

Infrastructure
  -> PostgreSQL for business data
  -> Redis for caching
  -> OSRM for road-network calculations
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

## Getting Started

### Prerequisites

- Docker Desktop and Docker Compose
- Or for local development:
  - Java 17
  - Maven
  - Node.js 20+
  - PostgreSQL 15
  - Redis or Redis Cloud access

### Option A: Run with Docker Compose

1. Create a root `.env` file with the required variables.
2. Prepare OSRM data in `osrm-data/data-HANOI` as described in `DOCKER_GUIDE.md`.
3. Start the stack:

```bash
docker compose up -d --build
```

4. Open the services:

| Service | URL |
| --- | --- |
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |
| OSRM | http://localhost:5000 |

Notes:
- The backend container expects PostgreSQL from Docker Compose.
- Redis is not started by this compose file and is expected from external configuration.

### Option B: Run Locally

#### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend reads configuration from environment variables and the root `.env` file through `spring-dotenv`.

#### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

Create a root `.env` file with values similar to the following:

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

Important:
- `SPRING_DATASOURCE_*` variables are required by `application.yml`.
- In Docker Compose, the backend uses `jdbc:postgresql://postgres:5432/...`.
- `NEXT_PUBLIC_API_URL` is used at frontend build time.

## API Highlights

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Key route groups:

| Area | Endpoint Prefix |
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

Examples:

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

## Demo Data

The seed script currently provides:
- 1 company
- 11 user accounts
- 12 drivers
- 4 depots
- 14 vehicles
- 60 orders
- Realistic Vietnam sample locations

Default demo credentials:

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin01` | `password123` |
| Dispatcher | `user01` | `password123` |

`user01` is seeded as a dispatcher and assigned to depots `1` and `2`.

## Author

- Owner: Khuong Xuan Toan
- Contact: `khuongxuantoan@gmail.com`

## License

This project is licensed under the GNU General Public License v3.0.

## Notes

- `frontend/README.md` is still the default Next.js template and can be updated separately if you want module-specific frontend documentation.
- The root documentation files are `README_ENG.md` and `README_VIE.md`.
