# 🚛 Logistics Control Hub

A full-stack logistics management system that solves the **Vehicle Routing Problem (VRP)** using Google OR-Tools, providing real-time route optimization, interactive map visualization, and comprehensive fleet management.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)

---

## Overview

**Logistics Control Hub** is a logistics operations platform built for intern/portfolio purposes, simulating how a modern fleet dispatch system works. It handles the full lifecycle from order creation → depot assignment → vehicle routing optimization → route visualization on a live map.

The system integrates with **OSRM** (Open Source Routing Machine) for real-world road distance and duration calculations, and uses **Google OR-Tools** to compute optimal delivery routes under vehicle capacity constraints.

---

## Tech Stack

### Backend

| Layer           | Technology                          |
| --------------- | ----------------------------------- |
| Language        | Java 17                             |
| Framework       | Spring Boot 3.4.4                   |
| ORM             | Spring Data JPA + Hibernate         |
| Database        | PostgreSQL 15                       |
| Cache           | Redis 7 (Spring Data Redis)         |
| Routing Engine  | Google OR-Tools 9.8                 |
| Map Routing API | OSRM (Open Source Routing Machine)  |
| Security        | Spring Security + JWT (JJWT 0.11.5) |
| API Docs        | SpringDoc OpenAPI (Swagger UI)      |
| Code Gen        | Lombok + MapStruct                  |

### Frontend

| Layer         | Technology            |
| ------------- | --------------------- |
| Framework     | Next.js 16 + React 19 |
| Language      | TypeScript            |
| Styling       | Tailwind CSS v4       |
| UI Components | Radix UI + shadcn/ui  |
| Map           | Leaflet.js            |
| Charts        | Recharts              |
| Forms         | React Hook Form + Zod |
| HTTP          | Axios                 |

### Infrastructure

| Service          | Technology               |
| ---------------- | ------------------------ |
| Containerization | Docker + Docker Compose  |
| Map Routing      | OSRM v5.27 (self-hosted) |
| Database         | PostgreSQL 15 Alpine     |
| Cache            | Redis 7 Alpine           |

---

## Features

### 🗺️ Route Optimization

- Solves **Capacitated VRP** with Google OR-Tools
- Considers vehicle weight & volume capacity
- Uses real road distances from OSRM API
- Asynchronous optimization with status polling
- Displays optimized routes as polylines on interactive map

### 📦 Order Management

- Full CRUD for delivery orders
- Auto-assigns orders to nearest depot
- Order lifecycle: `CREATED` → `IN_TRANSIT` → `DELIVERED`
- Filters by status, depot, driver

### 🏭 Depot Management

- Multiple depot support
- Each depot has a geographic location
- Statistics: active vehicles, pending orders, route runs

### 🚗 Vehicle & Driver Management

- Fleet management with status tracking (`ACTIVE`, `IDLE`, `MAINTENANCE`)
- Driver-vehicle assignment
- Vehicle capacity configuration (weight kg, volume m³, cost per km)

### 📊 Dashboard

- Real-time summary: total distance, total cost, number of routes
- Statistics cards per depot
- Route visualization with stop-by-stop breakdown

### 🔐 Authentication

- JWT-based authentication with refresh token
- Role-based access: `USER`, `ADMIN`
- Secure password hashing with BCrypt

### ⚡ Redis Caching

- OSRM API response caching (distance matrix)
- Metadata caching for depots, drivers, vehicles

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Frontend (Next.js)                 │
│         Dashboard │ Map │ Orders │ Fleet Mgmt        │
└────────────────────────┬────────────────────────────┘
                         │ REST API (HTTP/JSON)
┌────────────────────────▼────────────────────────────┐
│               Backend (Spring Boot)                  │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────┐ │
│  │   Auth   │  │  Orders  │  │  Route Optimizer  │ │
│  ├──────────┤  ├──────────┤  │  (Google OR-Tools)│ │
│  │  Depots  │  │ Vehicles │  └────────┬──────────┘ │
│  ├──────────┤  ├──────────┤           │             │
│  │ Drivers  │  │ Routing  │◄──────────┘             │
│  └──────────┘  └──────────┘                         │
│         │            │                              │
│    ┌────▼────┐  ┌────▼────────────┐                 │
│    │  Redis  │  │    OSRM API     │                 │
│    │ (Cache) │  │  (Road Dist.)   │                 │
│    └─────────┘  └─────────────────┘                 │
│         │                                           │
│    ┌────▼────────────────┐                          │
│    │   PostgreSQL DB      │                         │
│    └─────────────────────┘                          │
└─────────────────────────────────────────────────────┘
```

---

## Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & Docker Compose
- OR for local development: Java 17, Node.js 20, PostgreSQL 15, Redis

### Option A: Run with Docker Compose (Recommended)

**1. Clone the repository**

```bash
git clone https://github.com/ToanKhuongDEV/Logistics-Control-Hub.git
cd Logistics-Control-Hub
```

**2. Create environment file**

```bash
cp .env.example .env
# Edit .env with your values
```

**3. Prepare OSRM data** (Vietnam map)

```bash
cd osrm-data
# Download Hanoi/Vietnam OSM data and process it
# See DOCKER_GUIDE.md for detailed OSRM setup steps
```

**4. Start all services**

```bash
docker-compose up -d
```

**5. Access the application**
| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

### Option B: Local Development

**Backend**

```bash
cd backend
# Create .env file with required variables (see .env.example)
mvn spring-boot:run
```

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

---

## Environment Variables

Create a `.env` file in the root directory:

```env
# Database
DB_NAME=logistics_db
DB_USERNAME=postgres
DB_PASSWORD=your_db_password
DB_PORT=5432

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your_jwt_secret_min_32_chars
JWT_REFRESH_SECRET=your_refresh_secret_min_32_chars

# Server
SERVER_PORT=8080
```

---

## API Documentation

Once the backend is running, access Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

### Key Endpoints

| Method | Endpoint                         | Description                |
| ------ | -------------------------------- | -------------------------- |
| `POST` | `/api/v1/auth/login`             | Login, returns JWT         |
| `GET`  | `/api/v1/orders`                 | List all orders            |
| `POST` | `/api/v1/orders`                 | Create a new order         |
| `GET`  | `/api/v1/depots`                 | List all depots            |
| `GET`  | `/api/v1/depots/{id}/statistics` | Depot statistics           |
| `POST` | `/api/v1/routing/optimize`       | Trigger route optimization |
| `GET`  | `/api/v1/routing/runs/{id}`      | Get optimization result    |
| `GET`  | `/api/v1/vehicles`               | List vehicles              |
| `GET`  | `/api/v1/drivers`                | List drivers               |

---

## Project Structure

```
Logistics-Control-Hub/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/logistics/hub/
│   │   ├── feature/
│   │   │   ├── auth/           # JWT authentication
│   │   │   ├── company/        # Company management
│   │   │   ├── dashboard/      # Dashboard statistics
│   │   │   ├── depot/          # Depot management
│   │   │   ├── user/           # User accounts
│   │   │   ├── driver/         # Driver management
│   │   │   ├── geocoding/      # Address geocoding
│   │   │   ├── location/       # Location management
│   │   │   ├── order/          # Order management
│   │   │   ├── redis/          # Redis cache services
│   │   │   ├── routing/        # VRP optimization + OSRM
│   │   │   └── vehicle/        # Vehicle management
│   │   └── shared/             # Shared utilities
│   └── Dockerfile
├── frontend/                   # Next.js application
│   ├── src/
│   │   ├── app/                # Next.js app router pages
│   │   ├── components/         # React components
│   │   └── lib/                # Utilities, API clients
│   └── Dockerfile
├── database/
│   ├── database_schema.sql     # Table definitions
│   └── seeding_data.sql        # Sample data
├── osrm-data/                  # OSRM map data
├── docker-compose.yml
└── .env.example
```

---

## Default Credentials

| Role       | Username       | Password |
| ---------- | -------------- | -------- |
| Admin      | `admin01`      | `password123` |
| User | `user01` | `password123` |

---

## License

This project is developed as an internship/portfolio project.
