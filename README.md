<div align="center">

# 🌾 Kisan Suvidha

### Digital Agricultural Procurement, Marketplace, Logistics & AI Advisory Platform

**Kisan Suvidha** is a full-stack agricultural platform that connects **farmers, buyers, and procurement officers** through a unified digital workflow — from crop registration and procurement queues to marketplace offers, transactions, transportation, pickup, delivery, and **AI-powered selling assistance**.

<br/>

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.1-6DB33F?style=for-the-badge)](https://spring.io/projects/spring-ai)
[![Google Gemini](https://img.shields.io/badge/Google%20Gemini-AI-4285F4?style=for-the-badge&logo=google)](https://ai.google.dev/)
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

Agricultural selling and procurement can involve several disconnected activities — crop registration, procurement-centre queues, price discovery, buyer negotiations, transportation, pickup, and delivery.

**Kisan Suvidha** brings these workflows together into a single digital platform while adding an **agentic AI layer** that can retrieve application data through backend tools and help farmers make more informed selling decisions.

### The platform supports three primary roles

| Role | Purpose |
|---|---|
| 👨‍🌾 **Farmer** | Manage crops, queues, offers, transactions, and selling decisions |
| 🛒 **Buyer** | Discover crops, submit offers, negotiate, and track purchases |
| 🏢 **Procurement Officer** | Manage procurement queues and operational procurement workflows |

---

# 🎯 Problem Statement

A farmer may need to move through multiple disconnected steps to sell agricultural produce:

```text
Crop Ready
    ↓
Find Procurement Centre
    ↓
Join Queue
    ↓
Wait for Procurement
    ↓
Discover Price / Buyer
    ↓
Receive & Negotiate Offers
    ↓
Choose Selling Option
    ↓
Arrange Transport
    ↓
Pickup
    ↓
Delivery
    ↓
Transaction Completion
```

Kisan Suvidha digitizes this journey:

```text
                         KISAN SUVIDHA
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
       FARMER               BUYER              OFFICER
          │                   │                   │
          └───────────────────┼───────────────────┘
                              │
                              ▼
                       Unified Workflow
                              │
                              ▼
                 Crop → Queue → Offer
                              │
                              ▼
                    Transaction → Logistics
                              │
                              ▼
                     Pickup → Delivery
                              │
                              ▼
                       AI Decision Support
```

---

# ✨ Key Features

## 👨‍🌾 Farmer

- Secure registration and login
- JWT-based authentication
- Role-based access control
- Farmer profile management
- Location management with geocoding
- Crop registration and tracking
- Expected price and quantity management
- Procurement-centre discovery
- Procurement queue joining
- Real-time queue status information
- Backend-authoritative queue token assignment
- Marketplace offer discovery
- Accept / reject offers
- Counter offers
- Transaction tracking
- Transport cost estimation
- AI-powered crop selling advice

## 🛒 Buyer

- Secure registration and login
- Buyer profile management
- Location management
- Browse available crops
- Submit purchase offers
- Track submitted offers
- Respond to counter offers
- Create transactions from accepted offers
- Track purchase status

## 🏢 Procurement Officer

- Secure officer authentication
- Procurement-centre queue management
- View centre queues
- Call the next farmer
- Complete queue entries
- Manage transport options
- Support pickup and delivery workflows

---

# 🤖 Agentic AI Assistant

Kisan Suvidha includes a **Gemini-powered agentic AI assistant** built with **Spring AI**.

The AI is not implemented as a simple question-answer chatbot. It can dynamically invoke registered backend tools, retrieve real application data, and reason over those results.

```text
                         FARMER
                           │
                           ▼
                    POST /api/v1/ai/chat
                           │
                           ▼
                    KisanAiService
                           │
                           ▼
                      ChatClient
                           │
                           ▼
                     Gemini Agent
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
           Crops        Offers      Reference Price
              │            │            │
              └────────────┼────────────┘
                           │
                 ┌─────────┴─────────┐
                 ▼                   ▼
          Procurement            Marketplace
          Transport Cost         Transport Cost
                 │                   │
                 └─────────┬─────────┘
                           ▼
                    Queue Information
                           │
                           ▼
                     Agent Reasoning
                           │
                           ▼
                  Selling Recommendation
```

## Current AI Capabilities

The agent can access read-only tools for:

- 🌱 Farmer crop information
- 🤝 Marketplace offers
- 🏢 Procurement-centre information
- 🎟️ Farmer queue status
- 🚚 Available transport options
- 📍 Procurement-centre transport-cost estimation
- 📍 Marketplace-buyer transport-cost estimation
- 💰 Current reference-price information

### Example

A farmer can ask:

> "I have wheat to sell. Check my offers and compare them with the current reference price and transport costs. Which option appears financially better?"

The agent can orchestrate multiple tools to gather the required information and produce a data-backed recommendation.

### AI decision flow

```text
Farmer's Crops
      │
      ▼
Marketplace Offers
      │
      ▼
Reference Price
      │
      ▼
Available Transport
      │
      ├──────────────────────┐
      ▼                      ▼
Marketplace Route      Procurement Route
      │                      │
      ▼                      ▼
Transport Cost          Transport Cost
      │                      │
      └──────────┬───────────┘
                 ▼
          Financial Comparison
                 │
                 ▼
         AI Recommendation
```

## 🔐 AI Security Boundary

The AI layer follows a strict architecture:

```text
AI Agent
   │
   ▼
AI Tool
   │
   ▼
Business Service
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

The AI does **not** directly query repositories.

For personal farmer data, the authenticated user ID is obtained from Spring Security and passed through Spring AI `ToolContext`.

This prevents the model or frontend from supplying an arbitrary farmer identity.

### Read-only advisor

The current advisor follows:

```text
READ → ANALYZE → COMPARE → RECOMMEND
```

It does not automatically:

```text
❌ Accept an offer
❌ Reject an offer
❌ Counter an offer
❌ Create a transaction
❌ Assign transport
❌ Schedule pickup
❌ Mark a crop delivered
```

State-changing actions remain under the application's normal secured APIs and explicit user workflows.

📖 **[Agentic AI Orchestration & Integration](docs/agent-orchestration.md)**

---

# 🔄 Core Business Workflow

```text
                         FARMER
                            │
                            ▼
                      Register Crop
                            │
                            ▼
                 Find Procurement Centre
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
                      Buyer Offer
                            │
                            ▼
                 Accept / Counter Offer
                            │
                            ▼
                       TRANSACTION
                            │
                            ▼
                       LOGISTICS
                            │
                            ▼
                    Schedule Pickup
                            │
                            ▼
                         Delivery
                            │
                            ▼
                        Completion
```

The AI advisor operates alongside this workflow to help the farmer evaluate selling options before taking an action.

---

# 🏗️ Architecture

Kisan Suvidha currently follows a **modular monolithic architecture**.

```text
┌────────────────────────────────────────────────────────────────┐
│                           FRONTEND                             │
│                                                                │
│          TanStack Start + React + TypeScript + Tailwind        │
│                           + shadcn                             │
└───────────────────────────────┬────────────────────────────────┘
                                │
                           REST / JSON
                                │
              ┌─────────────────┴─────────────────┐
              │                                   │
              ▼                                   ▼
┌──────────────────────────────┐     ┌───────────────────────────┐
│     SPRING BOOT BACKEND      │     │       AI ASSISTANT        │
│                              │     │                           │
│ Auth                         │     │ Spring AI + Gemini        │
│ Agriculture                  │     │ Agent Tool Calling        │
│ Procurement                 │     │ Crop Selling Advisor      │
│ Marketplace                 │     │                           │
│ Transaction                 │     └─────────────┬─────────────┘
│ Logistics                   │                   │
│ Common / Configuration      │                   │ Tools
└──────────────┬───────────────┘                   │
               │                         ┌─────────▼─────────┐
               │                         │  Domain Services  │
               │                         └─────────┬─────────┘
               │                                   │
               └─────────────────┬─────────────────┘
                                 │
                            JPA / Hibernate
                                 │
                                 ▼
                       ┌──────────────────┐
                       │    PostgreSQL    │
                       └──────────────────┘
```

## Why a Modular Monolith?

The system intentionally avoids premature microservice complexity.

Benefits include:

- Clear domain boundaries
- Simpler development
- Easier debugging
- Strong transactional consistency
- Simpler local deployment
- One authoritative data store
- Straightforward future migration to services if scale requires it

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
    │   ├── enums/
    │   ├── repository/
    │   └── service/
    │
    ├── procurement/
    │   ├── controller/
    │   ├── dtos/
    │   ├── entity/
    │   ├── enums/
    │   ├── repository/
    │   └── service/
    │
    ├── marketplace/
    │   ├── controller/
    │   ├── dtos/
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
    ├── ai/
    │   ├── controller/
    │   ├── dto/
    │   ├── service/
    │   ├── tools/
    │   └── config/
    │
    ├── notification/       # Planned
    ├── recommendation/     # Planned / Future Expansion
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
Authenticated User
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
| `FARMER` | Crops, queues, received offers, selling decisions |
| `BUYER` | Crop discovery, offers, purchases |
| `OFFICER` | Procurement queue and operational workflows |

The backend remains the final authorization authority.

---

# 🗄️ Database

Kisan Suvidha uses **PostgreSQL** as its primary persistent data store.

Core relationships include:

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

### Development Persistence

During development:

```properties
spring.jpa.hibernate.ddl-auto=update
```

For production, the application can move toward:

```text
ddl-auto=validate
+
versioned database migrations
```

---

# ⚡ Concurrency & Data Integrity

Critical operations use transactional boundaries and pessimistic locking where concurrent access can affect correctness.

Important shared resources include:

- Procurement centres
- Crops
- Offers
- Transactions
- Transport options

Example:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

### Queue Token Safety

```text
Farmer A ──┐
Farmer B ──┼──► Lock Procurement Centre
Farmer C ──┘            │
                        ▼
                  Issue next token
                        │
                        ▼
                  Save Queue Entry
                        │
                        ▼
                      Commit
```

This prevents concurrent requests from receiving conflicting queue tokens.

---

# 💰 Financial & Logistics Calculations

Authoritative calculations remain in backend services.

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

Distance is calculated server-side using the Haversine-based `DistanceService`.

The AI consumes these backend-calculated values instead of inventing its own distances or rates.

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

---

# 🏷️ Offer Lifecycle

```text
                  PENDING
                 /   |   \
                /    |    \
               ▼     ▼     ▼
         ACCEPTED  REJECTED  COUNTERED
                              /      \
                             ▼        ▼
                        ACCEPTED    REJECTED
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

Queue position and estimated waiting time are calculated by the backend.

---

# 🌐 API

Local backend:

```text
http://localhost:8081
```

API base path:

```text
/api/v1
```

### Authentication

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
```

### Agriculture

```text
POST /api/v1/crops
GET  /api/v1/crops/my
```

### Procurement & Queue

```text
GET  /api/v1/procurement-centres
POST /api/v1/queues/join
GET  /api/v1/queues/my
```

### Marketplace

```text
POST /api/v1/offers
GET  /api/v1/offers/received
POST /api/v1/offers/{offerId}/accept
POST /api/v1/offers/{offerId}/counter
```

### Transactions

```text
POST /api/v1/transactions/from-offer/{offerId}
GET  /api/v1/transactions/my
GET  /api/v1/transactions/my-sales
POST /api/v1/transactions/{transactionId}/assign-transport
POST /api/v1/transactions/{transactionId}/schedule-pickup
POST /api/v1/transactions/{transactionId}/mark-delivered
```

### Logistics

```text
POST /api/v1/transport-options
GET  /api/v1/transport-options/available
```

### AI

```text
POST /api/v1/ai/chat
```

Example:

```json
{
  "message": "What marketplace offers have I received?"
}
```

Response:

```json
{
  "response": "..."
}
```

The AI endpoint is authenticated and uses the authenticated user's identity when accessing personal data.

📖 **[API Reference](docs/api-reference.md)**

---

# 🛠️ Technology Stack

## Backend

| Technology | Purpose |
|---|---|
| Java 21 | Backend language |
| Spring Boot 4.1.1 | Application framework |
| Spring MVC | REST API |
| Spring AI 2.0.1 | AI orchestration and tool calling |
| Google Gemini | LLM powering the AI agent |
| Spring Security | Authentication & authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Data access |
| Hibernate | ORM |
| PostgreSQL | Persistent data store |
| OpenStreetMap Nominatim | Geocoding |

## Frontend

| Technology | Purpose |
|---|---|
| TanStack Start | React application framework |
| React | UI |
| TypeScript | Type-safe frontend development |
| Tailwind CSS | Styling |
| shadcn/ui | UI components |

## Planned / Future Infrastructure

```text
Redis
WebSockets
Notifications
Additional AI agents
RAG / verified agricultural knowledge
```

---

# 📁 Documentation

Project documentation is maintained under `docs/`.

```text
docs/
├── README.md
├── auth.md
├── agriculture.md
├── procurement.md
├── marketplace.md
├── transaction.md
├── logistics.md
├── agent-orchestration.md
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
- 🤖 [Agentic AI Orchestration & Integration](docs/agent-orchestration.md)
- 🌐 [API Reference](docs/api-reference.md)

---

# 🚀 Getting Started

## Prerequisites

Install:

- Java 21
- Maven
- PostgreSQL
- Node.js / npm
- Git

Verify:

```bash
java -version
mvn -version
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

Configure credentials through application configuration or environment variables.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/kishan_suvidha
spring.datasource.username=postgres
spring.datasource.password=your_password
```

**Never commit real credentials.**

### 3. Configure JWT

Provide the JWT secret through environment/configuration:

```properties
jwt.secret=${JWT_SECRET}
```

### 4. Configure Gemini

Provide the Gemini API key through an environment variable:

```text
GEMINI_API_KEY=<your-key>
```

Spring AI configuration:

```properties
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.model=gemini-3.6-flash
```

**Never commit the Gemini API key.**

### 5. Run the backend

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

---

# 🔗 Frontend ↔ Backend Integration

The frontend should communicate with the backend through a centralized API layer.

Recommended structure:

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
        ├── logistics.ts
        └── ai.ts
```

The API client should handle:

- Base URL
- JWT authorization
- HTTP requests
- JSON serialization
- Error handling
- Module-specific API functions

This keeps UI components focused on presentation and application state.

---

# 🧪 Development Workflow

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
Agentic AI Integration
     │
     ▼
AI Selling Advisor
     │
     ▼
Structured AI Recommendation UI
     │
     ▼
Backend Hardening
     │
     ▼
Notifications / WebSockets / Redis
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

## Phase 2 — Agentic AI

- [x] Gemini integration
- [x] Spring AI integration
- [x] AI chat endpoint
- [x] Spring AI tool calling
- [x] Authenticated `ToolContext`
- [x] Crop retrieval tool
- [x] Marketplace offer tool
- [x] Procurement-centre tool
- [x] Queue status tool
- [x] Available transport tool
- [x] Reference-price tool
- [x] Procurement transport-cost estimation
- [x] Marketplace transport-cost estimation
- [x] Multi-tool Crop Selling Advisor
- [ ] Structured AI recommendation response
- [ ] AI recommendation frontend card

## Phase 3 — Frontend Integration

- [ ] Centralized API client
- [ ] Authentication integration
- [ ] Farmer dashboard integration
- [ ] Buyer dashboard integration
- [ ] Officer dashboard integration
- [ ] Queue integration
- [ ] Marketplace integration
- [ ] Transaction tracking
- [ ] Logistics integration
- [ ] AI assistant UI
- [ ] Remove obsolete mock data

## Phase 4 — Advanced Infrastructure

- [ ] Notifications
- [ ] WebSockets
- [ ] Redis caching / real-time infrastructure
- [ ] Production database migrations
- [ ] Deployment automation
- [ ] Monitoring and observability
- [ ] Additional AI agents
- [ ] RAG / verified agricultural knowledge

---

# 🧱 Design Principles

### 1. Backend is the source of truth

Business-critical state and calculations are owned by backend services.

### 2. AI is an orchestration layer

AI tools delegate to domain services rather than becoming a second implementation of business logic.

### 3. Security is enforced server-side

Frontend checks are for user experience. Authorization is enforced by Spring Security.

### 4. Authenticated identity comes from JWT

Personal-data tools derive identity from the authenticated security context instead of trusting client-provided owner IDs.

### 5. DTOs over exposed entities

REST APIs return DTOs rather than directly exposing JPA entities.

### 6. Business logic belongs in services

Controllers remain focused on HTTP concerns.

### 7. Critical updates are transactional

Queue, offer, transaction, and logistics operations use appropriate transactional boundaries.

### 8. Concurrency is treated as a first-class concern

Shared mutable resources use pessimistic locking where required.

### 9. AI recommendations are read-only

The advisor recommends; the farmer explicitly performs state-changing actions.

### 10. Avoid premature complexity

Redis, WebSockets, microservices, and additional AI infrastructure are introduced when they provide measurable value.

---

# 🔒 Security Considerations

Before production deployment:

- Store secrets in environment variables or a secret manager.
- Never commit JWT secrets.
- Never commit Gemini API keys.
- Use HTTPS.
- Use strong database credentials.
- Configure CORS for trusted frontend origins.
- Review JWT expiration and refresh strategy.
- Validate incoming request data server-side.
- Keep authorization checks on protected endpoints.
- Avoid exposing sensitive entity fields.
- Add rate limiting where appropriate.
- Replace development database configuration with production-safe settings.
- Review AI tool permissions before adding state-changing tools.

---

# 📈 Future Scalability

The modular monolith is intentionally designed to evolve.

If scale eventually requires service decomposition, domains can be separated behind an API gateway:

```text
                         API Gateway
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
      Auth Service       Agriculture         Procurement
                              │                   │
                              ▼                   ▼
                         Marketplace          Logistics
                              │
                              ▼
                         Transaction
                              │
                              ▼
                         Notification
                              │
                              ▼
                           AI Layer
```

The current implementation does **not** require microservices for the hackathon. The priority is a reliable end-to-end workflow with clear domain boundaries.

---

# 📊 Current Project Status

**Status: Active Development**

```text
Authentication        ✅
Crop Management       ✅
Procurement           ✅
Queue Management      ✅
Marketplace           ✅
Offers                ✅
Transactions          ✅
Logistics             ✅
Agentic AI            ✅
Selling Advisor       ✅
Frontend Integration  🚧 In Progress
Notifications         ⏸️ Planned
Advanced AI           🚧 Future
```

### Current AI milestone

> **Gemini-powered agentic orchestration over authenticated, read-only Kisan Suvidha business capabilities is working end-to-end.**

The agent can retrieve real application data through backend tools and use that information to provide a selling recommendation.

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
│ Receive Offers    │ Create Purchase   │ Manage Transport     │
│ Accept/Reject     │ Track Purchases   │ Pickup / Delivery    │
│ Counter Offer     │                   │ Operations           │
│ AI Selling Advice │                   │                      │
└───────────────────┴───────────────────┴──────────────────────┘
```

---

# 🌾 Vision

Kisan Suvidha aims to make agricultural procurement more:

**Transparent. Accessible. Organized. Data-driven. AI-assisted.**

By combining crop registration, procurement queues, price discovery, marketplace interaction, transactions, logistics, and AI-powered decision support in one platform, Kisan Suvidha provides a foundation for a more connected and efficient digital agricultural ecosystem.

---

<div align="center">

### Built with ❤️ for Smart India Hackathon

**Kisan Suvidha — Connecting Farmers, Buyers & Procurement Through Technology**

</div>
