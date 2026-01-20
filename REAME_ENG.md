# AI Supply Chain Control Tower

A logistics system that performs real-time route optimization, event-driven disruption handling, and durable delivery workflows.  
This project simulates how modern logistics companies manage fleet operations under real-world constraints and unexpected incidents.

---

## 1. Project Overview

In real-world logistics, route planning is not a one-time static task. Vehicles move continuously, orders have delivery windows, and disruptions such as traffic jams or vehicle breakdowns can occur at any time.

**AI Supply Chain Control Tower** is built to simulate a real operational environment where the system:
- Optimizes delivery routes for multiple vehicles and orders
- Continuously tracks vehicle movement in near real-time
- Automatically reacts to disruptions without human intervention
- Allows administrators to override decisions when necessary
- Manages long-running delivery processes reliably

---

## 2. Key Capabilities

### 2.1 Route Optimization (Core Feature)

- Solves the **Vehicle Routing Problem (VRP)** for multiple vehicles and orders
- Considers:
    - Vehicle capacity
    - Delivery time windows
    - Distance and estimated travel time
- Optimizes for:
    - Minimum total distance
    - Reduced delivery delays
- Supports **re-optimization** when conditions change

---

### 2.2 Real-time Fleet Tracking (Simulated)

- Vehicles periodically emit GPS data
- Location updates are streamed through Kafka
- Vehicle positions and order statuses are pushed to the frontend via WebSocket
- Enables near real-time visibility of fleet operations

---

### 2.3 Disruption Management (Autonomous Decision Making)

The system automatically reacts to operational disruptions such as:
- Traffic congestion
- Vehicle breakdowns
- Delivery delays

For each disruption, the system:
1. Detects the event
2. Analyzes affected routes and orders
3. Decides the best mitigation strategy
4. Triggers partial or full route re-optimization
5. Updates ETA and order status

This removes the need for manual dispatch intervention.

---

### 2.4 Human-in-the-loop Control

Administrators can intervene when needed by:
- Forcing route re-optimization
- Disabling vehicles
- Locking routes to prevent changes
- Increasing priority for critical orders

All manual actions are validated against constraints and recorded in audit logs.

---

### 2.5 Durable Delivery Workflows

- Each delivery is modeled as a **long-running workflow**
- Workflow steps include:
    - Route assignment
    - Progress tracking
    - Disruption handling
    - Delivery completion
- Workflows are **durable**:
    - Application restarts do not lose delivery state
    - Failures are retried safely

---

## 3. System Architecture

The system is implemented as a **monolithic application** with a modular, feature-based structure.

### Technology Stack

**Backend**
- Java 17
- Spring Boot
- Kafka (event streaming)
- PostgreSQL
- Temporal (workflow engine)
- OptaPlanner / OR-Tools (route optimization)
- WebSocket (real-time updates)

**Frontend**
- React.js
- Map visualization (Leaflet / Mapbox)

**Infrastructure**
- Docker & Docker Compose

---

## 4. Architecture Principles

- Monolithic deployment for simplicity
- Feature-based modularization
- Clear separation between:
    - Domain logic
    - Application services
    - Infrastructure and adapters
- Event-driven internal communication
- Designed to be easily split into microservices if needed

---

## 5. Main Functional Modules

- **Order Management**  
  Create and track delivery orders with lifecycle states

- **Vehicle & Driver Management**  
  Manage fleet availability, capacity, and status

- **Routing & Optimization**  
  Compute and update optimal delivery routes

- **Event Processing**  
  Handle GPS updates and disruption events

- **Disruption Handling**  
  Automatically mitigate operational incidents

- **Workflow Management**  
  Ensure reliable, long-running delivery execution

- **Control Tower Dashboard**  
  Provide operational visibility and control

---

## 6. Running the Project

### Prerequisites
- Docker
- Docker Compose

### Start the system
```bash
docker-compose up
