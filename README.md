<div align="center">

# 🌾 Kisan Suvidha

### Digital Procurement, Marketplace & Logistics Platform for Farmers

**Kisan Suvidha** is a full-stack agricultural platform designed to connect **farmers, buyers, and procurement officers** through a single digital workflow — from crop registration and procurement queues to marketplace offers, transactions, transport, pickup, and delivery.

<br/>

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![React](https://img.shields.io/badge/React-Frontend-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-Frontend-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-Styling-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)

<br/>

**Built for Smart India Hackathon**

</div>

---

## 📌 Overview

Agricultural procurement often involves fragmented processes across crop registration, physical queues, price discovery, buyer coordination, transportation, and delivery.

**Kisan Suvidha** brings these workflows together into one platform.

The system provides role-based experiences for:

- 👨‍🌾 **Farmers** — register crops, join procurement queues, receive and negotiate offers, and track transactions.
- 🏢 **Procurement Officers** — manage procurement queues, call farmers, manage transport, schedule pickups, and update delivery status.
- 🛒 **Buyers** — discover available crops, submit offers, negotiate prices, and create purchase transactions.

The current backend is implemented as a **modular monolith** using Spring Boot and PostgreSQL, with a REST API designed for integration with the React/TanStack Start frontend.

---

## 🎯 Problem Statement

Farmers can face several disconnected steps when trying to sell agricultural produce:

```text
Crop Ready
    ↓
Find Procurement Centre
    ↓
Join Physical Queue
    ↓
Wait for Procurement
    ↓
Find Buyer / Price
    ↓
Negotiate
    ↓
Arrange Transport
    ↓
Pickup
    ↓
Delivery
    ↓
Completion
```

Kisan Suvidha aims to digitize this journey:

```text
                    KISAN SUVIDHA
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
     FARMER            BUYER           OFFICER
        │                │                │
        └────────────┬───┴────────────────┘
                     │
                     ▼
              Unified Workflow
                     │
                     ▼
        Crop → Queue → Offer → Transaction
                     │
                     ▼
             Transport → Pickup
                     │
                     ▼
                  Delivery
```

---

# ✨ Key Features

## 👨‍🌾 Farmer

- Secure registration and login
- JWT-based authentication
- Role-based access control
- Farmer profile management
- Location update with geocoding
- Crop registration
- Crop quantity and expected-price management
- Procurement-centre discovery
- Queue joining
- Queue status tracking
- Backend-authoritative queue token assignment
- Received buyer offers
- Accept / reject offers
- Counter offers
- Transaction tracking
- Transport and pickup visibility

---

## 🛒 Buyer

- Secure registration and login
- Buyer profile management
- Location update
- Browse available crops
- Submit purchase offers
- Track submitted offers
- Respond to farmer counter offers
- Create transaction from accepted offer
- Track purchases and transaction status

---

## 🏢 Procurement Officer

- Secure officer authentication
- Procurement-centre queue management
- View centre queue
- Call the next farmer
- Complete queue entries
- Create transport options
- Assign transport to transactions
- Schedule crop pickup
- Mark crops as delivered

---

# 🔄 Core Business Workflow

```text
                    FARMER
                       │
                       ▼
                 Register Crop
                       │
                       ▼
            Select Procurement Centre
                       │
                       ▼
                  Join Queue
                       │
                       ▼
                Queue Processing
                       │
                       ▼
                   MARKETPLACE
                       ▲
                       │
                 Buyer Creates Offer
                       │
                       ▼
              Farmer Accepts / Counters
                       │
                       ▼
                  TRANSACTION
                       │
                       ▼
             Officer Assigns Transport
                       │
                       ▼
                Schedule Pickup
                       │
                       ▼
                 Crop Delivered
                       │
                       ▼
                    Complete
```

---

# 🏗️ Architecture

Kisan Suvidha currently follows a **modular monolithic architecture**.

```text
┌─────────────────────────────────────────────────────────────┐
│                         FRONTEND                            │
│                                                             │
│       TanStack Start + React + TypeScript + Tailwind        │
│                         + shadcn                            │
└─────────────────────────────┬───────────────────────────────┘
                              │
                         REST / JSON
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                      │
│                                                             │
│  ┌────────┐ ┌────────────┐ ┌────────────┐ ┌─────────────┐ │
│  │  Auth  │ │ Agriculture│ │ Procurement│ │ Marketplace │ │
│  └────────┘ └────────────┘ └────────────┘ └─────────────┘ │
│                                                             │
│  ┌──────────────┐ ┌───────────┐ ┌────────────────────────┐ │
│  │ Transaction  │ │ Logistics │ │ Common / Configuration │ │
│  └──────────────┘ └───────────┘ └────────────────────────┘ │
│                                                             │
│                 Spring Security + JWT                       │
└─────────────────────────────┬───────────────────────────────┘
                              │
                         JPA / Hibernate
                              │
                              ▼
                    ┌──────────────────┐
                    │    PostgreSQL    │
                    └──────────────────┘
```

### Why a Modular Monolith?

The current architecture intentionally avoids premature microservice complexity.

Benefits:

- Easier development
- Easier debugging
- Simpler deployment
- Strong transactional consistency
- One source of truth
- Clear domain boundaries
- Easy future migration to microservices if scale requires it

---

# 🧩 Backend Modules

```text
backend/
└── com.SmartIndiaHackathon.kishan_suvidha_backend/
    │
    ├── auth/
    │   ├── controller/
    │   ├── dto/
    │   ├── entity/
    │   ├── repository/
    │   ├── security/
    │   └── service/
    │
    ├── agriculture/
    │   ├── controller/
    │   ├── dto/
    │   ├── entity/
    │   ├── repository/
    │   └── service/
    │
    ├── procurement/
    │   ├── controller/
    │   ├── dto/
    │   ├── entity/
    │   ├── repository/
    │   └── service/
    │
    ├── marketplace/
    │   ├── controller/
    │   ├── dto/
    │   ├── entity/
    │   ├── enums/
    │   ├── repository/
    │   └── service/
    │
    ├── transaction/
    │   ├── controller/
    │   ├── dtos/
    │   ├── entity/
    │   ├── enums/
    │   ├── repository/
    │   └── service/
    │
    ├── logistics/
    │   ├── controller/
    │   ├── dtos/
    │   ├── entity/
    │   ├── enums/
    │   ├── repository/
    │   └── service/
    │
    ├── notification/       # Planned
    ├── recommendation/     # Planned
    │
    ├── common/
    │   ├── exceptions/
    │   ├── response/
    │   └── validation/
    │
    └── config/
```

---

# 🔐 Security

Security is implemented using **Spring Security + JWT**.

Authentication flow:

```text
Login
  │
  ▼
Authentication Service
  │
  ▼
JWT Generated
  │
  ▼
Frontend
  │
  │ Authorization: Bearer <JWT>
  ▼
JwtAuthenticationFilter
  │
  ▼
User Identification
  │
  ▼
Role-Based Authorization
  │
  ▼
Protected API
```

### Supported Roles

| Role | Responsibilities |
|---|---|
| `FARMER` | Crops, queues, received offers, offer decisions |
| `BUYER` | Crop discovery, offers, purchases |
| `OFFICER` | Queue management and logistics |

The backend remains the final authorization authority.

---

# 🗄️ Database

Kisan Suvidha uses **PostgreSQL** as its primary persistent data store.

Core entities include:

```text
User
  ├── Farmer
  └── Buyer

Farmer
  ├── Crop
  ├── QueueEntry
  └── Transaction

Buyer
  ├── Offer
  └── Transaction

ProcurementCentre
  └── QueueEntry

Crop
  ├── Offer
  └── Transaction

Offer
  └── Transaction

TransportOption
  └── Transaction
```

### Persistence Strategy

During development, Hibernate schema update is used:

```properties
spring.jpa.hibernate.ddl-auto=update
```

A production deployment can move toward:

```text
ddl-auto=validate
+
versioned production migrations
```

---

# ⚡ Concurrency & Data Integrity

Kisan Suvidha uses database transactions and pessimistic locking for critical operations.

Important protected resources include:

- Procurement centres
- Crops
- Offers
- Transactions
- Transport options

Example:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This is especially important for queue token assignment and transaction state changes.

### Queue Token Safety

```text
Farmer A ──┐
Farmer B ──┼──► Lock Centre
Farmer C ──┘        │
                    ▼
              Issue next token
                    │
                    ▼
              Save Queue Entry
                    │
                    ▼
                  Commit
```

---

# 💰 Transaction & Logistics Calculation

The backend is responsible for all authoritative financial calculations.

### Gross Amount

```text
grossAmount = agreedPrice × quantity
```

### Transport Cost

```text
transportCost =
    distanceKm
    × ratePerKmPerQuintal
    × quantityInQuintals
```

### Net Amount

```text
netAmount = grossAmount - transportCost
```

Distance is calculated server-side using the Haversine formula.

---

# 🚚 Transaction Lifecycle

```text
OFFER_ACCEPTED
       │
       ▼
TRANSPORT_ASSIGNED
       │
       ▼
PICKUP_SCHEDULED
       │
       ▼
CROP_DELIVERED
       │
       ▼
COMPLETED
```

The `COMPLETED` state exists in the domain model, while the final completion endpoint is still planned.

---

# 🏷️ Offer Lifecycle

```text
             PENDING
             /  |              /   |              ▼    ▼    ▼
      ACCEPTED REJECTED COUNTERED
                         /                           ▼     ▼
                   ACCEPTED  REJECTED
```

Supported states:

```text
PENDING
ACCEPTED
REJECTED
COUNTERED
```

---

# 🎟️ Queue Lifecycle

```text
WAITING
   │
   ▼
SERVING
   │
   ▼
COMPLETED

WAITING
   │
   ▼
CANCELLED
```

Queue position and estimated wait time are calculated by the backend.

---

# 🌐 API

Backend base URL during local development:

```text
http://localhost:8081
```

API base path:

```text
/api/v1
```

Examples:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me

POST /api/v1/crops
GET  /api/v1/crops/my

GET  /api/v1/procurement-centres
POST /api/v1/queues/join
GET  /api/v1/queues/my

POST /api/v1/offers
GET  /api/v1/offers/received
POST /api/v1/offers/{offerId}/accept
POST /api/v1/offers/{offerId}/counter

POST /api/v1/transactions/from-offer/{offerId}
GET  /api/v1/transactions/my
GET  /api/v1/transactions/my-sales

POST /api/v1/transactions/{transactionId}/assign-transport
POST /api/v1/transactions/{transactionId}/schedule-pickup
POST /api/v1/transactions/{transactionId}/mark-delivered

POST /api/v1/transport-options
GET  /api/v1/transport-options/available
```

For the complete API contract, see:

📖 **[API Reference](docs/api-reference.md)**

---

# 🛠️ Technology Stack

## Backend

| Technology | Purpose |
|---|---|
| Java 21 | Backend language |
| Spring Boot 4.1.1 | Application framework |
| Spring MVC | REST API |
| Spring Security | Authentication & authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Data access |
| Hibernate | ORM |
| PostgreSQL | Relational database |
| OpenStreetMap Nominatim | Geocoding |

## Frontend

| Technology | Purpose |
|---|---|
| TanStack Start | React application framework |
| React | UI |
| TypeScript | Type-safe frontend development |
| Tailwind CSS | Styling |
| shadcn/ui | UI components |

## Planned Infrastructure

```text
Redis
WebSockets
Notifications
Recommendation Engine
```

---

# 📁 Documentation

The project documentation is maintained under `docs/`.

```text
docs/
├── README.md
├── auth.md
├── agriculture.md
├── procurement.md
├── marketplace.md
├── transaction.md
├── logistics.md
├── notification.md
├── recommendation.md
├── api-reference.md
├── architecture.md
└── frontend-integration.md
```

### Documentation

- 📘 [Architecture](docs/architecture.md)
- 🔐 [Authentication](docs/auth.md)
- 🌱 [Agriculture / Crops](docs/agriculture.md)
- 🎟️ [Procurement & Queue](docs/procurement.md)
- 🤝 [Marketplace & Offers](docs/marketplace.md)
- 💰 [Transactions](docs/transaction.md)
- 🚚 [Logistics](docs/logistics.md)
- 🌐 [API Reference](docs/api-reference.md)

---

# 🚀 Getting Started

## Prerequisites

Install:

- Java 21
- Maven
- PostgreSQL
- Node.js / npm for frontend development
- Git

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

Verify PostgreSQL:

```bash
psql --version
```

---

## ⚙️ Backend Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd kishan-suvidha-backend
```

### 2. Configure PostgreSQL

Create the database:

```sql
CREATE DATABASE kishan_suvidha;
```

Configure database credentials through the application's configuration/environment.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/kishan_suvidha
spring.datasource.username=postgres
spring.datasource.password=your_password
```

**Never commit real credentials.**

### 3. Configure JWT

Provide the JWT secret through configuration/environment variables.

Example:

```properties
jwt.secret=${JWT_SECRET}
```

### 4. Run the backend

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8081
```

---

# 🖥️ Frontend Setup

Install dependencies:

```bash
npm install
```

Configure the backend API base URL:

```text
http://localhost:8081/api/v1
```

Start the development server:

```bash
npm run dev
```

The frontend integration process is documented in:

```text
docs/frontend-integration.md
```

---

# 🔗 Frontend ↔ Backend Integration

Recommended frontend API structure:

```text
frontend/src/
└── lib/
    └── api/
        ├── client.ts
        ├── auth.ts
        ├── crops.ts
        ├── procurement.ts
        ├── offers.ts
        ├── transactions.ts
        └── logistics.ts
```

The centralized API layer should handle:

- Base URL
- JWT authorization
- HTTP requests
- JSON serialization
- Error handling
- Module-specific API functions

This keeps UI components focused on presentation and application state.

---

# 🧪 Development Workflow

Recommended sequence:

```text
Backend Core
     │
     ▼
API Documentation
     │
     ▼
Frontend API Client
     │
     ▼
Authentication
     │
     ▼
Farmer Workflow
     │
     ▼
Buyer Workflow
     │
     ▼
Officer Workflow
     │
     ▼
End-to-End Integration
     │
     ▼
Backend Hardening
     │
     ▼
Notifications / Recommendations
     │
     ▼
WebSockets / Redis Optimization
```

---

# 🗺️ Roadmap

## Phase 1 — Core Backend

- [x] Authentication
- [x] JWT security
- [x] Role-based access control
- [x] Farmer profile
- [x] Buyer profile
- [x] Location/geocoding
- [x] Crop registration
- [x] Procurement centres
- [x] Queue management
- [x] Marketplace offers
- [x] Counter offers
- [x] Transactions
- [x] Transport options
- [x] Distance calculation
- [x] Transport cost calculation
- [x] Pickup scheduling
- [x] Delivery status

## Phase 2 — Frontend Integration

- [ ] Centralized API client
- [ ] Authentication integration
- [ ] Farmer dashboard integration
- [ ] Buyer dashboard integration
- [ ] Officer dashboard integration
- [ ] Queue integration
- [ ] Marketplace integration
- [ ] Transaction tracking
- [ ] Logistics integration
- [ ] Remove obsolete mock data

## Phase 3 — Advanced Features

- [ ] Transaction completion endpoint
- [ ] Notifications
- [ ] Recommendation engine
- [ ] WebSockets
- [ ] Redis caching / real-time infrastructure
- [ ] Production hardening
- [ ] Deployment automation
- [ ] Monitoring and observability improvements

---

# 🧱 Design Principles

### 1. Backend is the source of truth

The backend owns business-critical calculations and state transitions.

### 2. Security is enforced server-side

Frontend role checks are for UX only. Authorization is enforced by Spring Security.

### 3. Authenticated identity comes from JWT

Do not rely on client-provided owner IDs when identity can be derived from authentication.

### 4. DTOs over exposed entities

JPA entities are not directly exposed through REST APIs.

### 5. Business logic belongs in services

Controllers remain thin and focused on HTTP concerns.

### 6. Critical updates are transactional

Queue, offer, transaction, and logistics operations use transactional boundaries where required.

### 7. Concurrency matters

Shared mutable resources use pessimistic locking where necessary.

### 8. Avoid premature complexity

Redis, WebSockets, notifications, recommendations, and microservices are introduced only when they provide real value.

---

# 🔒 Security Considerations

Before production deployment:

- Use environment variables or a secret manager for credentials.
- Never commit JWT secrets.
- Use HTTPS.
- Use a strong database password.
- Configure CORS for the actual frontend origin.
- Review JWT expiration and refresh strategy.
- Validate all incoming data server-side.
- Keep authorization checks on protected endpoints.
- Avoid exposing sensitive entity fields.
- Add rate limiting where appropriate.
- Replace development infrastructure configuration with production-safe settings.

---

# 📈 Future Scalability

The modular monolith is intentionally designed to evolve.

A future deployment could split domains into services:

```text
                     API Gateway
                          │
       ┌──────────────────┼──────────────────┐
       │                  │                  │
       ▼                  ▼                  ▼
   Auth Service      Agriculture       Procurement
                          │                  │
                          ▼                  ▼
                    Marketplace         Logistics
                          │
                          ▼
                     Transaction
                          │
                          ▼
                     Notification
```

Microservices are **not required for the current hackathon implementation**.

The immediate priority is a reliable end-to-end workflow.

---

# 🤝 Contribution

Contributions are welcome.

Recommended flow:

```text
Fork
  ↓
Create Feature Branch
  ↓
Implement Change
  ↓
Run Tests / Verification
  ↓
Commit
  ↓
Push
  ↓
Open Pull Request
```

Suggested branch naming:

```text
feature/<short-description>
fix/<short-description>
docs/<short-description>
refactor/<short-description>
```

Please keep changes focused and maintain the domain boundaries defined in the architecture.

---

# 📜 Project Status

**Current status: Active Development**

```text
Authentication        ✅
Crop Management       ✅
Procurement           ✅
Queue Management      ✅
Marketplace           ✅
Offers                ✅
Transactions          ✅
Logistics             ✅
Notifications         ⏸️ Planned
Recommendations       ⏸️ Planned
Frontend Integration  🚧 In Progress
```

The immediate objective is to connect the existing frontend to the implemented REST API and validate the complete business workflow end-to-end.

---

# 👥 User Roles at a Glance

```text
┌──────────────────────────────────────────────────────────────┐
│                         KISAN SUVIDHA                        │
├───────────────────┬───────────────────┬──────────────────────┤
│      FARMER       │       BUYER       │       OFFICER        │
├───────────────────┼───────────────────┼──────────────────────┤
│ Register Crop     │ Browse Crops      │ Manage Queue         │
│ Join Queue        │ Create Offer      │ Call Next Farmer     │
│ Track Queue       │ Counter Response  │ Complete Queue       │
│ Receive Offers    │ Create Transaction│ Create Transport     │
│ Accept/Reject     │ Track Purchases   │ Assign Transport     │
│ Counter Offer     │                   │ Schedule Pickup      │
│ Track Sales       │                   │ Mark Delivered       │
└───────────────────┴───────────────────┴──────────────────────┘
```

---

# 🌾 Vision

Kisan Suvidha aims to make agricultural procurement more:

**Transparent. Accessible. Organized. Data-driven.**

By bringing crop registration, procurement queues, price discovery, buyer interaction, transactions, and logistics into a unified platform, the system creates a foundation for a more efficient digital agricultural marketplace.

---

<div align="center">

### Built with ❤️ for Smart India Hackathon

**Kisan Suvidha — Connecting Farmers, Buyers & Procurement**

</div>
