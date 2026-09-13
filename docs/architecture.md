# Kisan Suvidha — Architecture

> **Status:** Current implemented architecture  
> **Architecture style:** Modular Monolith  
> **Backend:** Java 21 + Spring Boot 4.1.1  
> **Database:** PostgreSQL  
> **Frontend:** TanStack Start / React / TypeScript / Tailwind / shadcn  
> **Local backend port:** `8081`

---

## 1. Architecture Overview

Kisan Suvidha uses a **modular monolithic architecture**. The backend runs as one Spring Boot application with one PostgreSQL database, while the codebase is divided into clear business domains.

```text
                         KISAN SUVIDHA
                              |
                 +------------+------------+
                 |                         |
             FRONTEND                  BACKEND
                 |                         |
      TanStack Start / React       Spring Boot Monolith
                 |                         |
                 |                  +------+------+
                 |                  |             |
                 |              REST API       Security
                 |                  |             |
                 |        +---------+---------+   |
                 |        |                   |   |
                 |    Domain Modules      Common Layer
                 |        |                   |
                 |        +---------+---------+
                 |                  |
                 |              JPA/Hibernate
                 |                  |
                 |              PostgreSQL
                 |
                 +-------- HTTP / JSON --------+
```

The architecture is intentionally simple enough for the hackathon while maintaining strong separation of business responsibilities.

---

# 2. Why Modular Monolith?

The current system intentionally uses a modular monolith instead of separate microservices.

Reasons:

1. The project is being developed by a small team.
2. Core workflows are strongly connected.
3. Several operations require data from multiple domains.
4. One PostgreSQL database simplifies consistency.
5. Deployment and debugging are simpler.
6. Domain modules can be extracted into services later if required.

The code is organized by **business domain**, rather than putting all controllers, services, and entities into one global package.

---

# 3. High-Level System Architecture

```text
+--------------------------------------------------------------+
|                         FRONTEND                             |
|                                                              |
|  TanStack Start + React + TypeScript + Tailwind + shadcn    |
|                                                              |
|  Farmer UI | Buyer UI | Officer UI | Authentication          |
+---------------------------+----------------------------------+
                            |
                       HTTP / JSON
                            |
                            v
+--------------------------------------------------------------+
|                    SPRING BOOT BACKEND                       |
|                                                              |
|  +--------------------------------------------------------+  |
|  |                  Security Layer                         |  |
|  |              JWT + Spring Security                      |  |
|  +---------------------------+----------------------------+  |
|                              |                               |
|  +-----------+-----------+---+---------+----------------+  |
|  |           |           |             |                |  |
|  v           v           v             v                v  |
| Auth     Agriculture  Procurement  Marketplace     Transaction |
|                                     |                |       |
|                                     +--------+-------+       |
|                                              v               |
|                                          Logistics           |
|                                                              |
|  +--------------------------------------------------------+  |
|  | Common: Exceptions | Responses | Validation             |  |
|  +--------------------------------------------------------+  |
+---------------------------+----------------------------------+
                            |
                       JPA / Hibernate
                            |
                            v
                 +----------------------+
                 |      PostgreSQL      |
                 +----------------------+
```

---

# 4. Backend Package Architecture

Root package:

```text
com.SmartIndiaHackathon.kishan_suvidha_backend
```

Current structure:

```text
com.SmartIndiaHackathon.kishan_suvidha_backend/
|
+-- auth/
|   +-- controller/
|   +-- dto/
|   +-- entity/
|   +-- repository/
|   +-- security/
|   +-- service/
|
+-- agriculture/
|   +-- controller/
|   +-- dto/
|   +-- entity/
|   +-- repository/
|   +-- service/
|
+-- procurement/
|   +-- controller/
|   +-- dto/
|   +-- entity/
|   +-- repository/
|   +-- service/
|
+-- marketplace/
|   +-- controller/
|   +-- dto/
|   +-- entity/
|   +-- enums/
|   +-- repository/
|   +-- service/
|
+-- transaction/
|   +-- controller/
|   +-- dtos/
|   +-- entity/
|   +-- enums/
|   +-- repository/
|   +-- service/
|
+-- logistics/
|   +-- controller/
|   +-- dtos/
|   +-- entity/
|   +-- enums/
|   +-- repository/
|   +-- service/
|
+-- notification/       # Planned
+-- recommendation/     # Planned
|
+-- common/
|   +-- exceptions/
|   +-- response/
|   +-- validation/
|
+-- config/
```

---

# 5. Domain Responsibilities

## 5.1 Auth

Responsible for:

- User registration
- Login
- Password handling
- JWT authentication
- Role management
- Current-user information
- Farmer profile
- Buyer profile
- Location updates

Roles:

```text
FARMER
BUYER
OFFICER
```

---

## 5.2 Agriculture

Responsible for:

- Crop registration
- Farmer's crops
- Crop details
- Quantity
- Expected price
- Crop type
- Harvest date
- Crop status

Crop statuses:

```text
AVAILABLE
RESERVED
SOLD
CANCELLED
```

---

## 5.3 Procurement

Responsible for:

- Procurement centres
- Farmer queues
- Queue token assignment
- Current serving token
- Queue status
- Estimated waiting time

Queue statuses:

```text
WAITING
SERVING
COMPLETED
CANCELLED
```

---

## 5.4 Marketplace

Responsible for:

- Buyer offer creation
- Farmer received offers
- Accept/reject
- Counter offers
- Buyer counter acceptance/rejection

Offer statuses:

```text
PENDING
ACCEPTED
REJECTED
COUNTERED
```

---

## 5.5 Transaction

Responsible for:

- Converting accepted offers into transactions
- Agreed price
- Quantity
- Gross amount
- Transport cost
- Net amount
- Transaction lifecycle
- Buyer transaction history
- Farmer sales history

---

## 5.6 Logistics

Responsible for:

- Transport options
- Vehicle details
- Capacity
- Transport rates
- Availability
- Distance calculation
- Transport cost
- Pickup scheduling
- Delivery status

---

## 5.7 Notification — Planned

Notification functionality is currently paused.

Potential responsibilities:

- Queue updates
- Offer notifications
- Transaction notifications
- Pickup reminders
- Delivery notifications
- Read/unread state
- Real-time notifications

It should not be treated as currently implemented.

---

## 5.8 Recommendation — Planned

Recommendation functionality is currently paused.

Potential responsibilities:

- Crop recommendations
- Price recommendations
- Procurement-centre recommendations
- Buyer/farmer recommendations

It should not be treated as currently implemented.

---

# 6. Layered Architecture Inside Each Module

Each implemented domain follows:

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Entity
    |
    v
PostgreSQL
```

DTOs sit between the API layer and application logic:

```text
HTTP Request
     |
     v
Request DTO
     |
     v
Controller
     |
     v
Service
     |
     v
Repository
     |
     v
Entity / Database
     |
     v
Response DTO
     |
     v
HTTP Response
```

---

# 7. Controller Layer

Controllers are responsible for:

- Defining REST endpoints
- Reading path/query parameters
- Receiving request DTOs
- Triggering validation
- Reading authenticated identity
- Calling services
- Returning response DTOs

Controllers should remain thin.

Business rules belong in services.

---

# 8. Service Layer

The service layer owns business rules.

Examples:

### Queue

```text
Check centre
    ->
Check acceptingQueue
    ->
Assign token
    ->
Calculate queue position
    ->
Persist queue entry
```

### Offer

```text
Check ownership
    ->
Check offer status
    ->
Apply valid state transition
```

### Transaction

```text
Check accepted offer
    ->
Check crop availability
    ->
Check quantity
    ->
Calculate gross amount
    ->
Create transaction
    ->
Reduce crop quantity
```

### Logistics

```text
Check transaction status
    ->
Check transport availability
    ->
Check capacity
    ->
Calculate distance
    ->
Calculate transport cost
    ->
Update net amount
    ->
Change transaction status
```

---

# 9. Repository Layer

Spring Data JPA repositories provide persistence access.

Repositories contain:

- Standard CRUD operations
- Domain-specific queries
- Locking queries where required

Pessimistic locking is used for resources where concurrent updates could create inconsistent state.

Example:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        SELECT c
        FROM Crop c
        WHERE c.id = :id
        """)
Optional<Crop> findByIdForUpdate(@Param("id") Long id);
```

Repositories should not contain complete business workflows.

---

# 10. DTO Architecture

JPA entities are not exposed directly through the REST API.

The flow is:

```text
Request JSON
     |
     v
Request DTO
     |
     v
Service
     |
     v
Entity
     |
     v
Response DTO
     |
     v
Response JSON
```

Benefits:

- API stability
- Controlled data exposure
- Request validation
- Separation of persistence and API models
- Easier frontend integration

---

# 11. Security Architecture

Security uses:

```text
Spring Security
       +
JWT
       +
Role-Based Access Control
```

Authentication flow:

```text
User
 |
 | Login credentials
 v
Auth Controller
 |
 v
Authentication Service
 |
 v
Spring Security
 |
 v
JWT generated
 |
 v
Frontend
```

Subsequent requests:

```text
Frontend
   |
   | Authorization: Bearer <JWT>
   v
JwtAuthenticationFilter
   |
   v
Extract user ID
   |
   v
Load User
   |
   v
Check enabled
   |
   v
Set Authentication
   |
   v
Controller / Service
```

---

# 12. Role-Based Authorization

Method-level authorization is enabled.

Examples:

```java
@PreAuthorize("hasRole('FARMER')")
```

```java
@PreAuthorize("hasRole('BUYER')")
```

```java
@PreAuthorize("hasRole('OFFICER')")
```

Responsibilities:

```text
+---------+--------------------------------------------+
| FARMER  | Crops, queue, received offers,            |
|         | offer decisions, location                  |
+---------+--------------------------------------------+
| BUYER   | Crop discovery, offers, transactions,     |
|         | counter decisions, location                |
+---------+--------------------------------------------+
| OFFICER | Queue management, transport, logistics     |
+---------+--------------------------------------------+
```

The backend remains the final authorization authority. Hiding a button in the frontend is not security.

---

# 13. Identity Model

The authenticated principal is the user ID.

```text
JWT
 |
 +-- userId
       |
       +-- User
       |
       +-- Farmer profile
       |
       +-- Buyer profile
```

Authenticated operations resolve the appropriate profile from the authenticated user.

This prevents the frontend from impersonating another user by changing an ID in a request.

---

# 14. Database Architecture

The application uses one PostgreSQL database.

Conceptual model:

```text
                     +----------+
                     |  users   |
                     +----+-----+
                          |
                 +--------+--------+
                 |                 |
            +----v-----+      +----v----+
            | farmers  |      | buyers  |
            +----+-----+      +----+----+
                 |                 |
            +----v-----+           |
            |  crops   |           |
            +----+-----+           |
                 |                 |
       +---------+---------+       |
       |         |         |       |
       v         v         v       v
     Queue     Offers    ...     Offers
       |         |
       |         +----------------+
       |                          |
       |                     +----v------+
       +-------------------->|Transaction|
                             +----+------+
                                  |
                           +------v-------+
                           |TransportOption|
                           +--------------+
```

---

# 15. Important Relationships

```text
User 1 -------- 1 Farmer
User 1 -------- 1 Buyer

Farmer 1 ------ * Crop
Farmer 1 ------ * QueueEntry

ProcurementCentre 1 ------ * QueueEntry

Crop 1 -------- * Offer
Buyer 1 ------- * Offer

Offer 1 ------- 1 Transaction

Crop 1 -------- * Transaction

TransportOption 1 ------- * Transaction
```

---

# 16. Transaction Boundaries and Concurrency

Critical state-changing operations use database transactions.

Examples:

```java
@Transactional
```

Important operations include:

- Queue token assignment
- Calling next farmer
- Offer acceptance
- Transaction creation
- Transport assignment
- Pickup scheduling
- Delivery state transition

The purpose is to keep related database changes atomic.

---

# 17. Pessimistic Locking

Pessimistic locking is used where concurrent requests could produce invalid state.

Important resources include:

```text
ProcurementCentre
Crop
Offer
Transaction
TransportOption
```

Typical flow:

```text
Request
   |
   v
Lock database row
   |
   v
Read current state
   |
   v
Apply business rule
   |
   v
Save
   |
   v
Commit
```

A concurrent request must wait for the lock before modifying the same critical resource.

---

# 18. Queue Concurrency Design

Queue token generation must be backend-authoritative.

The procurement centre is locked before issuing a token.

```text
Request A ----+
              |
Request B ----+----> Lock Procurement Centre
              |
Request C ----+              |
                             v
                      Read token counter
                             |
                             v
                       Issue next token
                             |
                             v
                       Save queue entry
                             |
                             v
                           Commit
```

This prevents concurrent farmers from receiving the same token.

---

# 19. Queue Calculation

The queue response contains:

```text
farmersAhead
estimatedWaitMinutes
```

The current prototype uses:

```text
farmersAhead = max(0, farmerToken - currentToken)
```

and approximately:

```text
waitMinutes = round(farmersAhead * 8.46)
```

The backend is the source of truth for these values.

---

# 20. Transaction Calculation Architecture

The backend owns financial calculations.

At transaction creation:

```text
agreedPrice
      |
      v
quantity
      |
      v
grossAmount = agreedPrice * quantity
      |
      v
transportCost = 0 initially
      |
      v
netAmount = grossAmount
```

After transport assignment:

```text
distance
   x
ratePerKmPerQuintal
   x
quantity in quintals
   |
   v
transportCost
   |
   v
netAmount = grossAmount - transportCost
```

The frontend should display authoritative values returned by the backend.

---

# 21. Distance Calculation

Distance is calculated by the backend using the Haversine formula.

Inputs:

```text
sourceLatitude
sourceLongitude
destinationLatitude
destinationLongitude
```

Output:

```text
distanceKm
```

Current use case:

```text
Farmer coordinates
       |
       v
Distance Service
       ^
       |
Buyer coordinates
       |
       v
Distance in kilometres
```

---

# 22. External Geocoding

The location update flow uses OpenStreetMap Nominatim.

```text
User enters location
        |
        v
Location Service
        |
        v
Geocoding Service
        |
        v
Nominatim
        |
        v
Latitude + Longitude
        |
        v
Farmer / Buyer profile
```

The public geocoding service should not be treated as an autocomplete API. Repeated unnecessary requests should be avoided.

---

# 23. Entity State Machines

## Offer

```text
             +-----------+
             |  PENDING  |
             +-----+-----+
                   |
          +--------+---------+
          |        |         |
          v        v         v
      ACCEPTED  REJECTED  COUNTERED
                              |
                         +----+----+
                         |         |
                         v         v
                     ACCEPTED   REJECTED
```

## Queue

```text
WAITING
   |
   v
SERVING
   |
   v
COMPLETED

WAITING
   |
   v
CANCELLED
```

## Transaction

```text
OFFER_ACCEPTED
       |
       v
TRANSPORT_ASSIGNED
       |
       v
PICKUP_SCHEDULED
       |
       v
CROP_DELIVERED
       |
       v
COMPLETED
```

The `COMPLETED` state exists, but the final completion endpoint is not yet implemented.

---

# 24. End-to-End Business Flow

```text
                  FARMER
                     |
                     v
              Register Crop
                     |
                     v
            Select Procurement Centre
                     |
                     v
                Join Queue
                     |
                     v
              Queue Processing
                     |
                     |
                     v
                 MARKETPLACE
                     ^
                     |
              Buyer Creates Offer
                     |
                     v
             Farmer Accepts Offer
                     |
                     v
               TRANSACTION
                     |
                     v
             Officer Assigns Transport
                     |
                     v
              Schedule Pickup
                     |
                     v
                Crop Pickup
                     |
                     v
               Mark Delivered
                     |
                     v
                  Complete
```

---

# 25. API Communication

The frontend communicates with the backend using REST and JSON.

```text
React / TanStack Start
        |
        | JSON
        v
Spring MVC Controller
        |
        v
Service
        |
        v
Repository
        |
        v
PostgreSQL
```

A centralized frontend API client should encapsulate HTTP communication instead of scattering raw requests throughout UI components.

---

# 26. Recommended Frontend Integration Structure

```text
frontend/
+-- src/
    +-- lib/
    |   +-- api/
    |       +-- client.ts
    |       +-- auth.ts
    |       +-- crops.ts
    |       +-- procurement.ts
    |       +-- offers.ts
    |       +-- transactions.ts
    |       +-- logistics.ts
    |
    +-- hooks/
    +-- routes/
    +-- components/
    +-- ...
```

The API layer should handle:

- Base URL
- JWT Authorization
- HTTP methods
- JSON serialization
- Error handling
- Module-specific API functions

---

# 27. Configuration

The application contains a dedicated:

```text
config/
```

package.

Infrastructure configuration includes components such as:

- REST client
- External geocoding
- Application/security configuration

Environment-specific values should be supplied through configuration rather than hard-coded in business logic.

---

# 28. Persistence Strategy

Development currently uses Hibernate schema update behavior:

```text
spring.jpa.hibernate.ddl-auto=update
```

The project does not require a separate migration file for every entity/module during development.

A production transition can later be:

```text
Development
    |
    v
ddl-auto=update
    |
    v
Schema stabilized
    |
    v
Create / verify production migrations
    |
    v
Production
    |
    v
ddl-auto=validate
```

---

# 29. Redis and Real-Time Features

Redis is a future infrastructure option.

Potential uses:

```text
Redis
 +-- Caching
 +-- Fast queue-related reads
 +-- Supporting real-time infrastructure
 +-- WebSocket-related state if required
```

Redis is not required for the current core transaction workflow.

PostgreSQL remains the source of truth.

---

# 30. WebSockets — Future

Real-time functionality may later be added for:

- Queue position changes
- Officer queue updates
- Offer notifications
- Transaction status updates

Potential architecture:

```text
Backend
   |
   +-- REST API
   |
   +-- WebSocket
          |
          v
       Frontend
```

This should be added after REST-based frontend integration is stable.

---

# 31. Notification Architecture — Future

The planned notification module can eventually react to business events.

Example:

```text
Offer Accepted
      |
      v
Notification Event
      |
      +-- Database notification
      |
      +-- WebSocket notification
```

Similar events can include:

```text
Queue Updated
Transaction Updated
Pickup Scheduled
Crop Delivered
```

Notifications are currently paused.

---

# 32. Recommendation Architecture — Future

Recommendation can later be implemented as an independent domain.

Potential inputs:

```text
Crop type
Location
Reference prices
Marketplace activity
Procurement centres
Farmer preferences
```

Potential outputs:

```text
Recommended crop
Recommended procurement centre
Suggested price
Potential buyer
```

This is planned, not currently implemented.

---

# 33. Error Handling Architecture

A global exception handler is used through:

```java
@RestControllerAdvice
```

The application maps domain exceptions to consistent HTTP responses.

```text
Service
   |
   +-- ResourceNotFoundException
   +-- BadRequestException
   +-- ConflictException
   +-- UnauthorizedException
   +-- ForbiddenException
             |
             v
GlobalExceptionHandler
             |
             v
ApiErrorResponse
```

This keeps API error responses consistent across modules.

---

# 34. Common Layer

The common layer contains cross-cutting concerns:

```text
common/
+-- exceptions/
|   +-- ApiException
|   +-- BadRequestException
|   +-- ConflictException
|   +-- ForbiddenException
|   +-- ResourceNotFoundException
|   +-- UnauthorizedException
|
+-- response/
|   +-- ApiErrorResponse
|
+-- validation/
```

The common layer should remain small. Business-specific logic belongs inside its domain module.

---

# 35. Architectural Rules

## Rule 1 — Backend is authoritative

The backend owns:

- Authentication
- Authorization
- Ownership
- Queue tokens
- Queue calculations
- Financial calculations
- Distance
- Transport cost
- State transitions

## Rule 2 — Do not trust frontend owner IDs

Authenticated ownership should be derived from JWT whenever possible.

## Rule 3 — Do not expose JPA entities directly

Use DTOs.

## Rule 4 — Business logic belongs in services

Controllers should remain thin.

## Rule 5 — Use transactions for critical state changes

Especially:

```text
Queue
Offers
Transactions
Transport
```

## Rule 6 — Lock shared mutable resources

Use pessimistic locking where concurrent operations can produce incorrect state.

## Rule 7 — PostgreSQL is the source of truth

Caching infrastructure must not become authoritative for core transactional data.

## Rule 8 — Keep modules domain-oriented

Avoid creating a giant shared service that handles unrelated business logic.

## Rule 9 — Do not add complexity prematurely

Notifications, recommendation, Redis, and WebSockets should be introduced when their workflows are ready.

---

# 36. Future Evolution Toward Microservices

The modular monolith provides a path toward future service extraction.

Possible future architecture:

```text
                    API Gateway
                         |
       +-----------------+-----------------+
       |                 |                 |
       v                 v                 v
   Auth Service     Agriculture       Procurement
                         |                 |
                         v                 v
                   Marketplace       Logistics
                         |
                         v
                    Transaction
                         |
                         v
                   Notification
```

This is a future architecture, not the current implementation.

The current application should remain a modular monolith until there is a concrete operational reason to split services.

---

# 37. Current Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Backend Framework | Spring Boot 4.1.1 |
| Web | Spring MVC / REST |
| Security | Spring Security + JWT |
| Persistence | Spring Data JPA |
| ORM | Hibernate |
| Database | PostgreSQL |
| Geocoding | OpenStreetMap Nominatim |
| Frontend | TanStack Start / React |
| Frontend Language | TypeScript |
| Styling | Tailwind CSS |
| UI Components | shadcn |
| Future Cache / Realtime | Redis / WebSockets |

---

# 38. Current vs Future Architecture

| Component | Current | Future |
|---|---|---|
| Backend | Modular monolith | Possible service extraction |
| Database | PostgreSQL | PostgreSQL per service if needed |
| Authentication | JWT | JWT/OAuth evolution if required |
| API | REST | REST + WebSocket |
| Queue | PostgreSQL | Redis optimization if needed |
| Notifications | Not implemented | Planned |
| Recommendation | Not implemented | Planned |
| Redis | Optional/future | Caching/realtime support |
| WebSockets | Not implemented | Planned |
| Deployment | Single backend application | Potentially distributed |

---

# 39. Recommended Development Sequence

```text
1. Backend core modules              DONE
        |
        v
2. API documentation                DONE
        |
        v
3. Frontend API client               NEXT
        |
        v
4. Authentication integration
        |
        v
5. Farmer workflow
        |
        v
6. Buyer workflow
        |
        v
7. Officer workflow
        |
        v
8. End-to-end integration
        |
        v
9. Final backend hardening
        |
        v
10. Notifications / Recommendation
        |
        v
11. WebSockets / Redis optimization
```

---

# 40. Architectural Goal

The goal of the current architecture is not maximum technical complexity.

The goal is:

> **A reliable, understandable, secure, and demonstrable Kisan Suvidha system where the complete farmer → marketplace → transaction → logistics workflow works end-to-end.**

The modular monolith provides enough structure for the hackathon while keeping development, debugging, deployment, and frontend integration manageable.
