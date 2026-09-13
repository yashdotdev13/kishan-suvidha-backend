# Procurement & Queue Management

## 1. Overview

The Procurement module manages government procurement centres and the farmer queue associated with those centres.

Its main responsibilities are:

- Managing procurement-centre information
- Providing procurement-centre discovery APIs
- Maintaining centre location and capacity information
- Geocoding procurement-centre locations
- Allowing farmers to join a procurement queue
- Assigning backend-authoritative queue tokens
- Showing a farmer's queue position
- Calculating the number of farmers ahead
- Estimating waiting time
- Allowing officers to call the next farmer
- Allowing officers to complete a queue entry
- Allowing farmers to cancel their own waiting queue entry
- Protecting concurrent token assignment with database locking

The module is organized as:

```text
procurement/
├── controller/
├── dto/
├── entity/
├── enums/
├── repository/
└── service/
```

---

## 2. Responsibilities

The Procurement module contains two closely related areas:

```text
Procurement Centre
        +
Queue Management
```

### Procurement Centre responsibilities

- Store procurement-centre details.
- Store state and district.
- Store human-readable location.
- Store latitude and longitude.
- Store centre capacity.
- Track the current serving token.
- Track the last issued token.
- Track whether the centre is accepting new queue entries.
- Provide centre discovery APIs.

### Queue responsibilities

- Associate a farmer and crop with a procurement centre.
- Assign a unique queue token within the centre's active queue.
- Maintain queue status.
- Determine farmers ahead.
- Estimate waiting time.
- Move entries through waiting/serving/completed/cancelled states.
- Ensure queue operations are concurrency-safe.

---

## 3. Package Structure

```text
procurement/
├── controller/
│   ├── ProcurementCentreController
│   └── QueueController
│
├── dto/
│   ├── ProcurementCentreResponse
│   ├── JoinQueueRequest
│   └── QueueEntryResponse
│
├── entity/
│   ├── ProcurementCentre
│   └── QueueEntry
│
├── enums/
│   └── QueueStatus
│
├── repository/
│   ├── ProcurementCentreRepository
│   └── QueueEntryRepository
│
└── service/
    ├── ProcurementCentreService
    └── QueueService
```

The module follows the standard application architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

DTOs are used at the API boundary rather than exposing JPA entities directly.

---

# 4. Procurement Centre

## 4.1 Procurement Centre Entity

The central centre entity contains:

```text
ProcurementCentre
├── id
├── name
├── state
├── district
├── location
├── latitude
├── longitude
├── capacity
├── currentToken
├── acceptingQueue
├── lastIssuedToken
├── createdAt
└── updatedAt
```

### ID

Database-generated primary key.

### Name

Human-readable procurement-centre name.

Example:

```text
Patna Central Procurement Centre
```

### State

State in which the centre is located.

### District

District in which the centre is located.

### Location

Human-readable address/location.

### Latitude and longitude

Geographic coordinates of the centre.

These are stored as `Double`.

### Capacity

Represents the configured capacity of the procurement centre.

### Current token

Represents the token currently being served.

### Accepting queue

Controls whether the centre currently accepts new queue entries.

### Last issued token

Tracks the latest token issued by the centre.

This value is used by the backend when generating the next queue token.

### Timestamps

The entity maintains:

```text
createdAt
updatedAt
```

---

## 4.2 Procurement Centre Indexes

The centre table contains indexes for common geographic filtering operations.

Indexes include:

```text
idx_procurement_centre_district
idx_procurement_centre_state
```

These support queries such as:

```text
Find centres in Bihar
Find centres in Patna district
```

---

# 5. Procurement Centre Repository

The repository is:

```text
ProcurementCentreRepository
```

It supports filtering by:

```java
findByState(String state)

findByDistrict(String district)

findByStateAndDistrict(String state, String district)

findByAcceptingQueueTrue()
```

It also provides a pessimistic-locking method:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        select c
        from ProcurementCentre c
        where c.id = :id
        """)
Optional<ProcurementCentre> findByIdForUpdate(
        @Param("id") Long id
);
```

The locking method is critical for queue token generation and other centre-level concurrent operations.

---

# 6. Why Procurement Centre Locking Is Required

Queue token assignment is a concurrency-sensitive operation.

Consider two farmers joining the same centre at almost exactly the same time:

```text
Farmer A ──┐
           ├── Join Queue
Farmer B ──┘
```

If both requests read:

```text
lastIssuedToken = 10
```

without locking, both could attempt to issue:

```text
token = 11
```

That would create duplicate queue tokens.

The backend therefore locks the procurement-centre row before issuing a new token.

Conceptually:

```text
Request A
    ↓
Lock Centre
    ↓
Read lastIssuedToken
    ↓
Increment
    ↓
Save token
    ↓
Commit
    ↓
Release lock

Request B
    ↓
Wait for centre lock
    ↓
Read updated lastIssuedToken
    ↓
Increment
    ↓
Save next token
```

This makes token assignment backend-authoritative and concurrency-safe.

---

# 7. Procurement Centre API

Base path:

```text
/api/v1/procurement-centres
```

The module provides centre discovery operations.

## 7.1 Get All Centres

```http
GET /api/v1/procurement-centres
```

Returns the available procurement-centre records.

---

## 7.2 Get Centre By ID

```http
GET /api/v1/procurement-centres/{centreId}
```

Returns a specific procurement centre.

---

## 7.3 Get Centres By State

```http
GET /api/v1/procurement-centres/state/{state}
```

Returns centres matching the requested state.

---

## 7.4 Get Centres By District

```http
GET /api/v1/procurement-centres/district/{district}
```

Returns centres matching the requested district.

---

## 7.5 Centre Response

The centre API uses:

```java
public record ProcurementCentreResponse(
    Long id,
    String name,
    String state,
    String district,
    String location,
    Double latitude,
    Double longitude,
    Integer capacity,
    Integer currentToken,
    Boolean acceptingQueue,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

This provides the frontend with the information required to display and select a procurement centre.

---

# 8. Geocoding

Procurement centres use geographic coordinates to support logistics and location-aware workflows.

The backend contains a geocoding abstraction:

```java
public interface GeocodingService {
    Coordinates geocode(String address);
}
```

The implementation uses OpenStreetMap Nominatim.

Conceptually:

```text
Human-readable location
        ↓
GeocodingService
        ↓
NominatimGeocodingService
        ↓
Nominatim
        ↓
Latitude + Longitude
```

The Nominatim client uses the configured REST client and supplies an application user-agent.

---

## 8.1 Why Geocoding Is Needed

A procurement centre may be stored as:

```text
Patna Central Procurement Centre,
Patna, Bihar, India
```

but downstream distance calculations require coordinates:

```text
latitude
longitude
```

The coordinates can later be used for:

- Distance calculations
- Transport planning
- Location-aware frontend experiences

---

## 8.2 Geocoding Responsibility

The procurement module does not implement geospatial calculations itself.

Its responsibility is to maintain reliable centre coordinates.

Distance calculation belongs to the Logistics module.

---

# 9. Queue Management

Queue management is implemented using:

```text
QueueEntry
```

A queue entry connects:

```text
Farmer
   +
Crop
   +
ProcurementCentre
```

Conceptually:

```text
Farmer
   │
   └── Crop
        │
        └── QueueEntry
              │
              └── ProcurementCentre
```

---

# 10. Queue Entry Entity

The queue entry contains:

```text
QueueEntry
├── id
├── farmer
├── crop
├── procurementCentre
├── tokenNumber
├── status
├── joinedAt
├── servedAt
├── completedAt
└── cancelledAt
```

### ID

Database-generated primary key.

### Farmer

The farmer who joined the queue.

### Crop

The crop associated with the procurement visit.

### Procurement centre

The centre at which the farmer joined the queue.

### Token number

Backend-generated queue token.

### Status

Current queue state.

### Timestamps

The timestamps record important queue lifecycle events.

---

# 11. Queue Status

The queue module defines:

```java
public enum QueueStatus {
    WAITING,
    SERVING,
    COMPLETED,
    CANCELLED
}
```

### WAITING

The farmer has joined the queue but has not yet been called.

### SERVING

The officer has called this farmer and the procurement process is currently being handled.

### COMPLETED

The farmer's queue visit has been completed.

### CANCELLED

The farmer cancelled the queue entry before being served.

---

# 12. Queue Repository

The repository is:

```text
QueueEntryRepository
```

It supports operations such as:

```java
findByProcurementCentreIdAndStatusOrderByTokenNumberAsc(...)

findByFarmerIdAndStatus(...)

findByFarmerIdAndStatusIn(...)

findByFarmerIdAndCropIdAndStatus(...)

existsByFarmerIdAndCropIdAndStatus(...)

findFirstByProcurementCentreIdAndStatus(...)

findFirstByProcurementCentreIdAndStatusOrderByTokenNumberAsc(...)
```

These methods support:

- Centre queue display
- Farmer queue lookup
- Duplicate queue prevention
- Finding the next waiting farmer

---

# 13. Joining the Queue

The farmer joins a queue using:

```http
POST /api/v1/queues/join
```

Authorization:

```text
FARMER
```

The farmer identity is derived from the JWT.

The request identifies the crop and procurement centre needed for the queue entry.

The backend performs the queue operation rather than trusting the frontend to calculate a token.

---

# 14. Queue Join Workflow

The queue join workflow is conceptually:

```text
Farmer
  │
  │ POST /api/v1/queues/join
  ▼
Authenticate Farmer
  │
  ▼
Load Farmer
  │
  ▼
Load Crop
  │
  ▼
Validate Crop Ownership
  │
  ▼
Lock Procurement Centre
  │
  ▼
Validate Centre Queue Status
  │
  ▼
Generate Next Token
  │
  ▼
Create QueueEntry
  │
  ▼
Save QueueEntry
  │
  ▼
Return QueueEntryResponse
```

The important property is that the token is generated by the backend.

---

# 15. Queue Token Generation

The centre maintains:

```text
lastIssuedToken
```

When a farmer joins:

```text
lastIssuedToken = lastIssuedToken + 1
```

The resulting number becomes the farmer's token.

For example:

```text
Initial:
lastIssuedToken = 20

Farmer A joins:
token = 21

Farmer B joins:
token = 22

Farmer C joins:
token = 23
```

The operation is protected by the pessimistic lock on the procurement-centre row.

---

# 16. Current Token

The procurement centre also maintains:

```text
currentToken
```

This represents the token currently being served.

For example:

```text
currentToken = 18
```

and a farmer has:

```text
tokenNumber = 23
```

Then the farmer is five tokens ahead in the simple queue calculation:

```text
23 - 18 = 5
```

The actual queue response uses the backend calculation described below.

---

# 17. Farmers Ahead

The current queue calculation is:

```text
farmersAhead = max(0, farmerToken - currentToken)
```

This prevents negative values.

Example:

```text
farmerToken = 25
currentToken = 20

farmersAhead = max(0, 25 - 20)
              = 5
```

The value is calculated by the backend and returned to the frontend.

The frontend should display the backend value rather than calculate its own queue position.

---

# 18. Estimated Waiting Time

The current prototype uses an estimated processing time of approximately:

```text
8.46 minutes per farmer
```

The calculation is:

```text
waitMinutes = round(farmersAhead × 8.46)
```

Example:

```text
farmersAhead = 5

waitMinutes = round(5 × 8.46)
            = round(42.3)
            = 42 minutes
```

This is an estimate, not a guaranteed appointment time.

The calculation is intended to provide the farmer with an approximate waiting duration.

---

# 19. Queue Response

The queue response contains:

```text
id
cropId
procurementCentreId
procurementCentreName
tokenNumber
currentToken
farmersAhead
estimatedWaitMinutes
status
joinedAt
servedAt
completedAt
cancelledAt
```

This allows the frontend to display:

- Queue token
- Current token
- Number of farmers ahead
- Estimated waiting time
- Current queue status
- Queue lifecycle timestamps

---

# 20. Farmer's Queues

Farmers can view their active queue entries through:

```http
GET /api/v1/queues/my
```

Authorization:

```text
FARMER
```

The current implementation returns active entries in:

```text
WAITING
SERVING
```

states.

Completed and cancelled historical entries are not returned by this active queue endpoint.

---

# 21. Officer Centre Queue

Officers can retrieve the queue of a procurement centre:

```http
GET /api/v1/queues/centre/{centreId}
```

Authorization:

```text
OFFICER
```

This endpoint is intended for the officer dashboard.

The response is ordered by queue token for the centre/status query used by the repository.

---

# 22. Calling the Next Farmer

An officer calls the next farmer using:

```http
POST /api/v1/queues/centre/{centreId}/next
```

Authorization:

```text
OFFICER
```

The service:

1. Locks the procurement-centre row.
2. Verifies that the centre accepts queue operations.
3. Verifies that another farmer is not already in `SERVING`.
4. Finds the lowest-token `WAITING` entry.
5. Changes the entry to `SERVING`.
6. Records `servedAt`.
7. Updates the centre's `currentToken`.
8. Saves the queue entry and centre.
9. Returns the updated queue response.

Conceptually:

```text
WAITING
   │
   │ Officer calls next
   ▼
SERVING
```

---

# 23. One Farmer Serving At A Time

The current queue workflow prevents multiple queue entries at the same centre from being in `SERVING` simultaneously.

Before calling the next farmer, the service checks whether a serving entry already exists.

Conceptually:

```text
Centre
  │
  ├── WAITING token 21
  ├── SERVING token 20
  └── WAITING token 22
```

The officer cannot call token 21 until token 20 is completed.

This provides a simple single-server queue model for the prototype.

---

# 24. Completing a Queue Entry

An officer completes a serving queue entry through:

```http
POST /api/v1/queues/{queueEntryId}/complete
```

Authorization:

```text
OFFICER
```

The entry must currently be:

```text
SERVING
```

The backend then changes it to:

```text
COMPLETED
```

and records:

```text
completedAt
```

Lifecycle:

```text
WAITING
   ↓
SERVING
   ↓
COMPLETED
```

---

# 25. Cancelling a Queue Entry

A farmer can cancel their own waiting queue entry:

```http
POST /api/v1/queues/{queueEntryId}/cancel
```

Authorization:

```text
FARMER
```

The service verifies:

1. The queue entry exists.
2. The authenticated farmer owns the entry.
3. The entry is currently `WAITING`.

A serving entry cannot be cancelled through this endpoint.

Lifecycle:

```text
WAITING
   ↓
CANCELLED
```

The backend records:

```text
cancelledAt
```

---

# 26. Queue Controller Summary

The main controller is:

```text
QueueController
```

Base path:

```text
/api/v1/queues
```

Endpoints:

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/v1/queues/join` | FARMER | Join procurement queue |
| GET | `/api/v1/queues/my` | FARMER | Get active farmer queues |
| GET | `/api/v1/queues/centre/{centreId}` | OFFICER | View centre queue |
| POST | `/api/v1/queues/centre/{centreId}/next` | OFFICER | Call next farmer |
| POST | `/api/v1/queues/{queueEntryId}/complete` | OFFICER | Complete serving entry |
| POST | `/api/v1/queues/{queueEntryId}/cancel` | FARMER | Cancel waiting entry |

---

# 27. Example Queue Join Request

Example:

```http
POST /api/v1/queues/join
Authorization: Bearer <JWT>
Content-Type: application/json
```

The request identifies the crop and procurement centre.

Conceptually:

```json
{
  "cropId": 1,
  "procurementCentreId": 1
}
```

The exact DTO field names should remain aligned with the current `JoinQueueRequest`.

The frontend does not provide:

```text
tokenNumber
currentToken
farmersAhead
estimatedWaitMinutes
```

Those values are calculated or assigned by the backend.

---

# 28. Example Queue Response

Conceptually:

```json
{
  "id": 10,
  "cropId": 1,
  "procurementCentreId": 1,
  "procurementCentreName": "Patna Central Procurement Centre",
  "tokenNumber": 23,
  "currentToken": 18,
  "farmersAhead": 5,
  "estimatedWaitMinutes": 42,
  "status": "WAITING",
  "joinedAt": "2026-09-13T10:00:00",
  "servedAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

The timestamp values are illustrative.

---

# 29. Validation

Queue operations perform business validation in the service layer.

Important validations include:

### Centre exists

The selected procurement centre must exist.

### Centre accepts queue

A farmer cannot join when:

```text
acceptingQueue = false
```

### Crop exists

The selected crop must exist.

### Farmer owns crop

A farmer cannot put another farmer's crop into their queue.

### Duplicate active queue

The backend checks for an existing active queue entry for the same farmer/crop where required.

### Serving state

An officer cannot call another farmer if the centre already has a `SERVING` entry.

### Cancellation state

Only `WAITING` entries can be cancelled by the farmer.

### Completion state

Only `SERVING` entries can be completed.

---

# 30. Authentication & Authorization

The Procurement module uses JWT authentication and role-based authorization.

### Farmer operations

```java
@PreAuthorize("hasRole('FARMER')")
```

Used for:

```text
Join queue
View own queues
Cancel own waiting queue
```

### Officer operations

```java
@PreAuthorize("hasRole('OFFICER')")
```

Used for:

```text
View centre queue
Call next farmer
Complete queue entry
```

The backend obtains the current user from the authenticated principal.

The frontend must not be trusted to provide another user's identity.

---

# 31. Concurrency Model

Queue management is one of the most concurrency-sensitive modules in Kisan Suvidha.

The main concurrency concern is token generation.

The backend protects centre-level updates using:

```text
PESSIMISTIC_WRITE
```

The queue join operation is transactional.

Conceptually:

```text
@Transactional
      │
      ▼
Lock ProcurementCentre
      │
      ▼
Read lastIssuedToken
      │
      ▼
Generate next token
      │
      ▼
Create QueueEntry
      │
      ▼
Save changes
      │
      ▼
Commit
```

This ensures that concurrent requests cannot both allocate the same next token.

---

# 32. Database as Source of Truth

The frontend may display:

```text
Token 23
5 farmers ahead
42 minutes estimated
```

but these values must originate from the backend.

The frontend should never be considered authoritative for:

```text
token assignment
current token
queue status
farmers ahead
estimated wait time
```

The backend and PostgreSQL database are the source of truth.

---

# 33. Queue State Machine

The current queue lifecycle is:

```text
                 ┌───────────┐
                 │  WAITING  │
                 └─────┬─────┘
                       │
              Officer calls next
                       │
                       ▼
                 ┌───────────┐
                 │  SERVING  │
                 └─────┬─────┘
                       │
                 Officer completes
                       │
                       ▼
                 ┌───────────┐
                 │ COMPLETED │
                 └───────────┘

WAITING
   │
   │ Farmer cancels
   ▼
CANCELLED
```

Valid transitions in the current implementation:

```text
WAITING → SERVING
WAITING → CANCELLED
SERVING → COMPLETED
```

---

# 34. Procurement Centre and Queue Relationship

The database relationships are conceptually:

```text
ProcurementCentre
      │
      └──< QueueEntry >── Farmer
                 │
                 └────── Crop
```

One procurement centre can have many queue entries.

Each queue entry belongs to one farmer, one crop, and one procurement centre.

---

# 35. Example Complete Procurement Workflow

A typical farmer workflow is:

```text
Farmer Login
     │
     ▼
Browse Procurement Centres
     │
     ▼
Select Centre
     │
     ▼
Select Registered Crop
     │
     ▼
Join Queue
     │
     ▼
Backend Assigns Token
     │
     ▼
WAITING
     │
     │ Officer calls next
     ▼
SERVING
     │
     │ Procurement work completed
     ▼
COMPLETED
```

If the farmer leaves before being served:

```text
WAITING
   │
   │ Cancel
   ▼
CANCELLED
```

---

# 36. Frontend Integration

The frontend should use the procurement APIs to build:

- Procurement-centre listing
- Centre selection
- Queue joining
- Queue status screen
- Token display
- Waiting-time display
- Officer queue dashboard

A farmer queue screen can display:

```text
Procurement Centre:
Patna Central Procurement Centre

Your Token:
23

Current Token:
18

Farmers Ahead:
5

Estimated Wait:
42 minutes

Status:
WAITING
```

These values should come directly from the backend response.

---

# 37. Recommended Frontend Flow

```text
GET /api/v1/procurement-centres
        │
        ▼
Display centres
        │
        ▼
Farmer selects centre
        │
        ▼
Farmer selects crop
        │
        ▼
POST /api/v1/queues/join
        │
        ▼
Receive QueueEntryResponse
        │
        ▼
Display token + wait estimate
        │
        ▼
GET /api/v1/queues/my
        │
        ▼
Refresh queue state
```

When WebSockets are integrated later, queue state can be updated in real time instead of relying only on polling.

---

# 38. Error Handling

Typical procurement errors include:

| Situation | HTTP Status |
|---|---:|
| Centre not found | 404 |
| Crop not found | 404 |
| Queue entry not found | 404 |
| Centre not accepting queue | 400 |
| Invalid queue state | 400 |
| Farmer does not own crop | 403 |
| Duplicate active queue | 409 |
| Unauthenticated request | 401 |
| Insufficient role | 403 |

The module uses the application's global exception handling mechanism.

---

# 39. Current Implementation Status

Implemented:

### Procurement centres

- Procurement centre entity
- State and district information
- Location
- Latitude/longitude
- Capacity
- Current token
- Last issued token
- Queue acceptance flag
- Centre repository
- Pessimistic centre locking
- Centre response DTO
- Centre listing
- Centre lookup
- State filtering
- District filtering
- State + district filtering
- Accepting-queue filtering

### Geocoding

- `GeocodingService`
- `NominatimGeocodingService`
- Nominatim integration
- Centre coordinate support

### Queue

- Queue entry entity
- Queue status enum
- Queue repository
- Queue token assignment
- Pessimistic locking
- Farmer queue joining
- Farmer active queue lookup
- Officer centre queue lookup
- Officer next-farmer workflow
- Queue completion
- Farmer cancellation
- Farmers-ahead calculation
- Estimated waiting-time calculation

---

# 40. Current Queue Calculation

The current prototype uses:

```text
farmersAhead = max(0, farmerToken - currentToken)

waitMinutes = round(farmersAhead × 8.46)
```

Example:

```text
Farmer token = 30
Current token = 24

farmersAhead = max(0, 30 - 24)
              = 6

waitMinutes = round(6 × 8.46)
            = round(50.76)
            = 51 minutes
```

This is an estimated wait and can later be replaced with a more sophisticated calculation based on:

- Historical service time
- Centre workload
- Crop quantity
- Number of active counters
- Real-time queue throughput

The current implementation intentionally keeps the calculation simple.

---

# 41. Important Design Principles

The Procurement module follows these principles:

### Backend-authoritative tokens

The frontend never generates queue tokens.

### Database-backed concurrency control

Centre-level locking prevents duplicate token assignment.

### JWT-derived ownership

The backend derives the farmer identity from authentication.

### Explicit queue states

The queue uses an enum instead of arbitrary strings.

### Transactional queue updates

Operations that change queue state and centre token information are performed transactionally.

### DTO-based APIs

JPA entities are not directly exposed through REST responses.

### Separation of concerns

Procurement manages queues and centres.

Logistics manages transport and distance calculations.

Marketplace manages offers.

Transactions manage sales lifecycle.

---

# 42. Module Interaction

The Procurement module interacts with several other modules:

```text
                    ┌───────────────┐
                    │     Auth      │
                    └───────┬───────┘
                            │
                            ▼
                         Farmer
                            │
                            ▼
                    ┌───────────────┐
                    │  Agriculture  │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │  Procurement  │
                    │     Queue     │
                    └───────┬───────┘
                            │
                            ▼
                   Procurement Centre
                            │
                            ▼
                       Logistics
```

The queue uses the farmer and crop information created by other modules while keeping queue-specific state inside the Procurement module.

---

# 43. Future Improvements

The current implementation is intentionally simple for the hackathon prototype.

Possible future improvements include:

- Real-time queue updates through WebSockets
- Dynamic waiting-time estimation
- Multiple service counters per procurement centre
- Queue priority rules
- Better queue analytics
- Historical service-time calculation
- Centre operating hours
- Centre-specific crop acceptance rules
- More sophisticated location-based centre discovery
- Redis-backed queue caching where appropriate

These are future enhancements and are not part of the current implemented queue behavior.

---

# 44. Summary

The Procurement module provides the bridge between a farmer's registered crop and the physical procurement-centre workflow.

The core flow is:

```text
Farmer
   │
   ▼
Registered Crop
   │
   ▼
Select Procurement Centre
   │
   ▼
Join Queue
   │
   ▼
Backend Token Assignment
   │
   ▼
WAITING
   │
   ▼
Officer Calls Farmer
   │
   ▼
SERVING
   │
   ▼
Officer Completes
   │
   ▼
COMPLETED
```

The backend remains authoritative for queue tokens, queue state, farmer ownership, current token, farmers ahead, and estimated waiting time.

This provides a reliable foundation for the frontend procurement experience and later real-time queue updates.
