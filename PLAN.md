# Implementation Status And Roadmap

This document reflects the current state of the Logistics Control Hub codebase.

## Current Status

The project is a working full-stack logistics dashboard with:

- Spring Boot backend
- Next.js frontend
- PostgreSQL schema and seed data
- Docker Compose stack for PostgreSQL, OSRM, backend, and frontend
- External Redis support for OSRM cache
- Google OR-Tools route optimization
- HttpOnly-cookie based authentication
- Permission-based UI and backend authorization

Kafka is not part of the current implementation. Temporal is present as a Maven dependency, but no Temporal workflow is currently wired into the application.

## Implemented Modules

| Module | Status | Notes |
| --- | --- | --- |
| Authentication | Implemented | Login, refresh, logout, current user, change password, forgot/reset password |
| Authorization | Implemented | Roles, permissions, depot-scoped access |
| Account management | Implemented | Admin can create, list, update, and delete employee accounts |
| Company settings | Implemented | Read/update company information |
| Dashboard | Implemented | Operational statistics endpoint and page |
| Orders | Implemented | CRUD-style operations, filters, statistics, bulk status update, audit logs |
| Vehicles | Implemented | CRUD-style operations, filters, statistics, bulk depot reassignment |
| Drivers | Implemented | CRUD-style operations, filters, statistics, availability lookup |
| Depots | Implemented | CRUD-style operations, filters, statistics |
| Routing | Implemented | Auto routing by depot, run detail, latest run, paginated history |
| Driver portal | Implemented | Driver sees assigned deliveries and completes orders |
| Excel | Implemented | Export and template endpoints for depot, driver, order, routing, vehicle |
| Audit | Implemented | Audit log entity, service, filters, and admin UI |
| Docker | Implemented | PostgreSQL, OSRM, backend, frontend |
| Redis cache | Implemented | Used for OSRM matrix cache through external Redis |

## Backend Snapshot

Main package:

```text
backend/src/main/java/com/logistics/hub/
|-- common/
|-- config/
`-- feature/
    |-- audit/
    |-- auth/
    |-- company/
    |-- dashboard/
    |-- depot/
    |-- driver/
    |-- driverportal/
    |-- excel/
    |-- geocoding/
    |-- location/
    |-- order/
    |-- redis/
    |-- routing/
    |-- user/
    `-- vehicle/
```

Important runtime details:

- Java 17
- Spring Boot 3.4.4
- Database config is read from `SPRING_DATASOURCE_*`
- Redis URL is built from `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- OSRM URL defaults to `http://localhost:5000` and can be overridden by `OSRM_URL`
- Swagger UI is available at `/swagger-ui.html`
- OpenAPI JSON is available at `/api-docs`
- Health endpoint is `/actuator/health`

## Frontend Snapshot

Main routes:

| Route | Purpose |
| --- | --- |
| `/login` | Login |
| `/forgot-password` | Forgot password |
| `/reset-password` | Reset password |
| `/dashboard` | Overview |
| `/orders` | Order management |
| `/fleet` | Vehicle management |
| `/drivers` | Driver management |
| `/depots` | Depot management |
| `/history` | Routing history |
| `/driver` | Driver delivery portal |
| `/accounts` | Account management |
| `/audit` | Audit log |
| `/settings` | Settings/company |

Frontend auth relies on backend HttpOnly cookies. Axios is configured with `withCredentials: true` in `frontend/src/lib/api.ts`.

## API Groups

| Area | Prefix |
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

## Current Data Model

The SQL schema currently defines:

- `companies`
- `users`
- `drivers`
- `locations`
- `depots`
- `vehicles`
- `orders`
- `routing_runs`
- `routes`
- `route_stops`
- `refresh_tokens`
- `password_reset_tokens`
- `audit_logs`

The seed file currently inserts:

- 1 company
- 12 users
- 12 drivers
- 64 locations
- 4 depots
- 14 vehicles
- 60 orders
- 1 routing run
- 1 route
- 7 route stops
- 1 audit log

## Known Gaps And Next Work

### High Priority

- Add stronger lifecycle validation for orders, vehicles, and routes so terminal/in-progress states cannot be changed incorrectly.
- Add more focused tests for routing, driver portal, audit failure logs, and authorization boundaries.
- Decide whether PostgreSQL should remain internal-only in Docker or expose a local development port.
- Align CORS production origins with deployment environment variables instead of hard-coded host values.

### Medium Priority

- Add import flow if Excel templates are intended for uploads, not only downloads.
- Improve deployment documentation for VPS/domain/HTTPS/cookie behavior.
- Add frontend tests for permission-based menu visibility and auth refresh handling.
- Add structured sample `.env` documentation for root Docker Compose usage.

### Optional / Future

- Remove Temporal dependency if workflows are not planned.
- Add real async workflow processing if Temporal is adopted.
- Add notification layer for route completion or delivery exceptions.
- Add map data preparation scripts for OSRM.

## Verification Commands

Backend:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm run lint
npm run build
```

Docker:

```bash
docker compose up -d --build
docker compose ps
```
