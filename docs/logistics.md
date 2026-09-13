# Logistics & Transport Management

## 1. Overview

The Logistics module manages transport options and the logistics calculations required to move a farmer's crop to the buyer.

Its current responsibilities are:

- Managing transport options
- Managing vehicle type, vehicle number, capacity and pricing
- Providing available transport options
- Protecting transport assignment with database locking
- Validating transport capacity
- Calculating distance between farmer and buyer
- Calculating transport cost
- Updating transaction financial values after transport assignment
- Tracking transport availability

The module is organized as:

```text
logistics/
├── controller/
├── dtos/
├── entity/
├── enums/
└── repository/
    └── service/
```

The logistics workflow is closely integrated with the Transaction module.

---

# 2. Responsibilities

The Logistics module is responsible for:

1. Storing transport/vehicle information.
2. Maintaining transport availability.
3. Defining supported vehicle types.
4. Storing vehicle capacity.
5. Storing transport pricing.
6. Providing available transport options.
7. Calculating geographic distance.
8. Validating transport capacity before assignment.
9. Calculating transport cost.
10. Supporting transaction transport assignment.

The module is **not** responsible for:

- User authentication
- Farmer registration
- Crop registration
- Marketplace offers
- Queue management
- Negotiation
- Transaction lifecycle ownership

The Transaction module owns the transaction lifecycle, while Logistics supplies transport information and logistics calculations.

---

# 3. Package Structure

The logistics module is organized around:

```text
logistics/
├── controller/
│   └── TransportOptionController
│
├── dtos/
│   ├── CreateTransportOptionRequest
│   └── TransportOptionResponse
│
├── entity/
│   └── TransportOption
│
├── enums/
│   └── VehicleType
│
├── repository/
│   └── TransportOptionRepository
│
└── service/
    └── DistanceService
        └── HaversineDistanceService
```

The Transaction module uses the logistics services/repositories when transport is assigned.

---

# 4. Transport Option

## 4.1 TransportOption Entity

The central logistics entity is:

```text
TransportOption
├── id
├── vehicleType
├── vehicleNumber
├── capacityQuintals
├── ratePerKmPerQuintal
├── available
├── createdAt
└── updatedAt
```

---

## 4.2 ID

```text
id
```

Database-generated primary key.

---

## 4.3 Vehicle Type

```text
vehicleType
```

Represents the type of vehicle.

The current enum is:

```java
public enum VehicleType {
    TRACTOR,
    MINI_TRUCK,
    TRUCK,
    TEMPO,
    OTHER
}
```

---

## 4.4 Vehicle Number

```text
vehicleNumber
```

Stores the vehicle registration number.

Example:

```text
BR01AB1234
```

This value identifies the physical vehicle assigned to a transaction.

---

## 4.5 Capacity

```text
capacityQuintals
```

Represents the maximum crop quantity the transport option can carry, expressed in quintals.

The backend uses:

```java
BigDecimal
```

for precise quantity representation.

Example:

```text
capacityQuintals = 25
```

means the vehicle can carry up to 25 quintals.

---

## 4.6 Rate

```text
ratePerKmPerQuintal
```

Represents the transport rate.

The pricing unit is:

```text
currency / km / quintal
```

Example:

```text
₹2.50 / km / quintal
```

The backend uses `BigDecimal` for monetary calculations.

---

## 4.7 Availability

```text
available
```

Indicates whether the transport option can currently be assigned.

Typical states:

```text
true
false
```

When a vehicle is assigned to a transaction:

```text
available = false
```

This prevents simultaneous assignment to another transaction.

---

## 4.8 Timestamps

The entity maintains:

```text
createdAt
updatedAt
```

These timestamps are managed by entity lifecycle callbacks.

---

# 5. Vehicle Type

The supported vehicle types are:

```java
public enum VehicleType {
    TRACTOR,
    MINI_TRUCK,
    TRUCK,
    TEMPO,
    OTHER
}
```

| Type | Description |
|---|---|
| TRACTOR | Tractor-based transport |
| MINI_TRUCK | Small truck |
| TRUCK | Larger truck |
| TEMPO | Small commercial transport |
| OTHER | Other vehicle types |

The enum prevents arbitrary vehicle-type strings from being stored throughout the application.

---

# 6. Transport Repository

The repository is:

```text
TransportOptionRepository
```

It supports:

```java
List<TransportOption> findByAvailableTrue();

List<TransportOption> findByVehicleTypeAndAvailableTrue(
    VehicleType vehicleType
);
```

These methods support transport discovery.

---

# 7. Transport Pessimistic Lock

The repository also provides:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        select t
        from TransportOption t
        where t.id = :transportOptionId
        """)
Optional<TransportOption> findByIdForUpdate(
        @Param("transportOptionId") Long transportOptionId
);
```

This is used when a transport option is being assigned.

The lock is important because two transactions could otherwise attempt to assign the same vehicle concurrently.

---

# 8. Why Transport Locking Is Required

Consider:

```text
Transaction A ──┐
                ├── Assign Vehicle #1
Transaction B ──┘
```

If both requests read:

```text
available = true
```

before either request updates the row, both could potentially assign the same vehicle.

The backend therefore uses a pessimistic write lock.

Conceptually:

```text
Request A
   ↓
Lock Transport
   ↓
Check available
   ↓
Assign
   ↓
available = false
   ↓
Commit
   ↓
Release lock

Request B
   ↓
Wait for lock
   ↓
Read available = false
   ↓
Reject assignment
```

This makes transport availability database-authoritative.

---

# 9. Transport DTOs

## 9.1 CreateTransportOptionRequest

The create request contains:

```java
public record CreateTransportOptionRequest(
    @NotNull VehicleType vehicleType,
    @NotBlank String vehicleNumber,
    @NotNull @DecimalMin("0.001") BigDecimal capacityQuintals,
    @NotNull @DecimalMin("0.01") BigDecimal ratePerKmPerQuintal
) {}
```

Fields:

```text
vehicleType
vehicleNumber
capacityQuintals
ratePerKmPerQuintal
```

Availability is not supplied by the client.

A newly created transport option is available by default.

---

## 9.2 TransportOptionResponse

The response is:

```java
public record TransportOptionResponse(
    Long id,
    VehicleType vehicleType,
    String vehicleNumber,
    BigDecimal capacityQuintals,
    BigDecimal ratePerKmPerQuintal,
    Boolean available,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

This DTO is used for transport discovery and officer management.

---

# 10. Transport Controller

The main controller is:

```text
TransportOptionController
```

Base path:

```text
/api/v1/transport-options
```

---

# 11. Create Transport Option

Endpoint:

```http
POST /api/v1/transport-options
```

Authorization:

```text
OFFICER
```

This operation allows an officer to add a transport option to the logistics system.

Example request:

```json
{
  "vehicleType": "TRUCK",
  "vehicleNumber": "BR01AB1234",
  "capacityQuintals": 25,
  "ratePerKmPerQuintal": 2.50
}
```

The backend creates the transport option with:

```text
available = true
```

---

# 12. Get Available Transport Options

Endpoint:

```http
GET /api/v1/transport-options/available
```

Authorization:

```text
FARMER
BUYER
OFFICER
```

This endpoint returns transport options that are currently available.

It is primarily useful for operational transport selection.

---

# 13. Get Transport Option By ID

Endpoint:

```http
GET /api/v1/transport-options/{id}
```

Authorization:

```text
FARMER
BUYER
OFFICER
```

Returns a specific transport option.

---

# 14. Logistics API Summary

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/v1/transport-options` | OFFICER | Create transport option |
| GET | `/api/v1/transport-options/available` | FARMER/BUYER/OFFICER | List available transport |
| GET | `/api/v1/transport-options/{id}` | FARMER/BUYER/OFFICER | Get transport option |

Transport assignment itself is exposed through the Transaction module because assigning transport changes transaction state.

---

# 15. Distance Service

Distance calculation is abstracted behind:

```java
public interface DistanceService {
    BigDecimal calculateDistanceKm(
        Double sourceLatitude,
        Double sourceLongitude,
        Double destinationLatitude,
        Double destinationLongitude
    );
}
```

The abstraction prevents the Transaction module from depending directly on the mathematical implementation.

---

# 16. Haversine Distance

The current implementation is:

```text
HaversineDistanceService
```

It calculates the great-circle distance between two geographic coordinates.

Conceptually:

```text
Source
(latitude, longitude)
       │
       ▼
HaversineDistanceService
       │
       ▼
Destination
(latitude, longitude)
       │
       ▼
Distance in kilometres
```

The implementation uses an Earth radius of approximately:

```text
6371 km
```

The result is returned using:

```text
BigDecimal
scale = 2
rounding = HALF_UP
```

---

# 17. Coordinate Validation

Distance calculation requires valid coordinates.

The service validates that the required coordinate values are available before calculating the distance.

If required coordinates are missing, the transaction cannot safely calculate logistics cost.

The Transaction module therefore requires:

```text
Farmer latitude
Farmer longitude
Buyer latitude
Buyer longitude
```

before transport assignment can complete.

---

# 18. Why Distance Is Calculated by the Backend

The frontend may know approximate locations, but it must not be trusted to determine financial values.

For example, the frontend should not send:

```json
{
  "distanceKm": 93.34
}
```

and expect the backend to accept that value.

Instead:

```text
Farmer coordinates
        +
Buyer coordinates
        ↓
Backend DistanceService
        ↓
distanceKm
```

The backend then uses the calculated distance in transport pricing.

---

# 19. Transport Assignment

Transport assignment is part of the Transaction workflow.

Endpoint:

```http
POST /api/v1/transactions/{transactionId}/assign-transport
```

Authorization:

```text
OFFICER
```

The request identifies the transport option to assign.

---

# 20. Assignment Preconditions

Before assigning transport, the backend verifies:

### Transaction state

The transaction must currently be:

```text
OFFER_ACCEPTED
```

Transport cannot be assigned after the transaction has already moved beyond this stage.

### Transport existence

The selected transport option must exist.

### Transport availability

The transport option must be:

```text
available = true
```

### Transport capacity

The transport capacity must be greater than or equal to the transaction quantity.

### Farmer coordinates

The farmer must have valid latitude and longitude.

### Buyer coordinates

The buyer must have valid latitude and longitude.

---

# 21. Transport Assignment Workflow

The complete workflow is:

```text
Officer
   │
   │ Assign transport
   ▼
TransactionService
   │
   ▼
Lock Transaction
   │
   ▼
Verify OFFER_ACCEPTED
   │
   ▼
Lock TransportOption
   │
   ▼
Verify available
   │
   ▼
Verify capacity
   │
   ▼
Load Farmer Coordinates
   │
   ▼
Load Buyer Coordinates
   │
   ▼
Calculate Distance
   │
   ▼
Calculate Transport Cost
   │
   ▼
Calculate Net Amount
   │
   ▼
Assign Transport
   │
   ▼
Set available = false
   │
   ▼
Set status = TRANSPORT_ASSIGNED
   │
   ▼
Save
```

---

# 22. Transport Cost Formula

The current transport pricing model is:

```text
transportCost =
    distanceKm
    × ratePerKmPerQuintal
    × quantityInQuintals
```

The quantity used by the formula is expressed in quintals.

Example:

```text
distanceKm = 93.34
rate = ₹2.50 / km / quintal
quantity = 20 quintals
```

Calculation:

```text
93.34 × 2.50 × 20
= ₹4,667
```

The backend rounds the result to two decimal places using `HALF_UP`.

---

# 23. Financial Integration With Transaction

After transport assignment:

```text
grossAmount
        ↓
transportCost
        ↓
netAmount
```

Formula:

```text
netAmount = grossAmount - transportCost
```

Example:

```text
grossAmount   = ₹46,000
transportCost = ₹4,667

netAmount = ₹41,333
```

The Logistics module supplies the transport-related values, while the Transaction entity stores the resulting financial state.

---

# 24. Transaction Fields Updated During Assignment

When transport is successfully assigned, the transaction is updated with:

```text
transportOption
distanceKm
transportCost
netAmount
status
updatedAt
```

The transaction status becomes:

```text
TRANSPORT_ASSIGNED
```

---

# 25. Transport Availability Lifecycle

A transport option starts as:

```text
available = true
```

After assignment:

```text
available = false
```

Conceptually:

```text
AVAILABLE
    │
    │ Assigned to transaction
    ▼
UNAVAILABLE
```

The current implementation does not yet document a complete transport-release workflow after transaction completion.

That release behavior should be explicitly defined when the final transaction completion workflow is implemented.

---

# 26. Capacity Validation Example

Suppose:

```text
Transaction quantity = 20 quintals
```

Transport A:

```text
capacity = 15 quintals
```

Result:

```text
Assignment rejected
```

Transport B:

```text
capacity = 25 quintals
```

Result:

```text
Assignment allowed
```

The capacity check is performed by the backend.

---

# 27. Rate Validation

Transport creation validates:

```text
ratePerKmPerQuintal > 0
```

This prevents invalid pricing such as:

```text
₹0
negative rate
```

The rate stored on the transport option becomes the basis for calculating the transport cost during assignment.

---

# 28. Capacity Validation

Transport creation validates:

```text
capacityQuintals > 0
```

A transport option must have a positive carrying capacity.

The transaction assignment workflow then compares:

```text
transport capacity
vs
transaction quantity
```

---

# 29. Authentication & Authorization

Logistics uses the application's JWT security model.

### Officer

Officers can create transport options:

```java
@PreAuthorize("hasRole('OFFICER')")
```

### Authenticated users

Farmer, buyer, and officer roles can view available transport options where permitted:

```java
@PreAuthorize(
    "hasAnyRole('FARMER', 'BUYER', 'OFFICER')"
)
```

### Transport assignment

Transport assignment is controlled by the Transaction controller and is:

```text
OFFICER-only
```

The frontend must not be trusted to assign a transport option directly.

---

# 30. Concurrency Considerations

The main concurrency risk is double assignment.

Example:

```text
Transport #1
available = true

        │
   ┌────┴────┐
   ▼         ▼
Tx A       Tx B
```

Without locking:

```text
Tx A reads true
Tx B reads true

Tx A assigns
Tx B assigns
```

Both could believe the vehicle is available.

With pessimistic locking:

```text
Tx A locks transport
      │
      ▼
Tx A assigns transport
      │
      ▼
available = false
      │
      ▼
commit

Tx B obtains lock
      │
      ▼
reads available = false
      │
      ▼
assignment rejected
```

This is why:

```java
findByIdForUpdate(...)
```

is used during assignment.

---

# 31. Transactional Boundary

Transport assignment is a transactional operation.

Conceptually:

```java
@Transactional
public TransactionResponse assignTransport(...) {
    ...
}
```

The following changes need to remain consistent:

```text
Transaction
+
TransportOption
```

If the operation fails, the assignment should not leave one record updated while the other remains inconsistent.

---

# 32. Error Cases

Common logistics errors include:

| Situation | HTTP Status |
|---|---:|
| Transport option not found | 404 |
| Transaction not found | 404 |
| Invalid transaction state | 400 |
| Invalid transport capacity | 400 |
| Missing farmer coordinates | 400 |
| Missing buyer coordinates | 400 |
| Invalid coordinates | 400 |
| Invalid transport rate | 400 |
| Invalid capacity | 400 |
| Transport unavailable | 409 |
| Unauthenticated request | 401 |
| Insufficient role | 403 |

The global exception handler converts application exceptions into the standard API error response.

---

# 33. Example Create Transport Request

```http
POST /api/v1/transport-options
Authorization: Bearer <OFFICER_JWT>
Content-Type: application/json
```

Body:

```json
{
  "vehicleType": "TRUCK",
  "vehicleNumber": "BR01AB1234",
  "capacityQuintals": 25,
  "ratePerKmPerQuintal": 2.50
}
```

The resulting transport option is initially:

```text
available = true
```

---

# 34. Example Transport Response

```json
{
  "id": 1,
  "vehicleType": "TRUCK",
  "vehicleNumber": "BR01AB1234",
  "capacityQuintals": 25,
  "ratePerKmPerQuintal": 2.50,
  "available": true,
  "createdAt": "2026-09-13T10:00:00",
  "updatedAt": "2026-09-13T10:00:00"
}
```

---

# 35. Example Transport Assignment

Suppose:

```text
Transaction quantity = 20 quintals

Transport:
vehicleNumber = BR01AB1234
capacity = 25 quintals
rate = ₹2.50/km/quintal
```

Farmer coordinates and buyer coordinates produce:

```text
distance = 93.34 km
```

Then:

```text
transportCost
= 93.34 × 2.50 × 20
= ₹4,667
```

If:

```text
grossAmount = ₹46,000
```

then:

```text
netAmount = ₹46,000 - ₹4,667
          = ₹41,333
```

The transaction becomes:

```text
TRANSPORT_ASSIGNED
```

---

# 36. Frontend Integration

The frontend can use logistics APIs for transport discovery and officer operations.

## Officer transport management

An officer can create a transport option:

```text
POST /api/v1/transport-options
```

The officer can then see available vehicles:

```text
GET /api/v1/transport-options/available
```

---

## Officer transaction workflow

The officer sees a transaction in:

```text
OFFER_ACCEPTED
```

and selects an available transport.

The frontend calls:

```text
POST /api/v1/transactions/{transactionId}/assign-transport
```

After success, the backend returns:

```text
TRANSPORT_ASSIGNED
```

with:

```text
vehicleNumber
distanceKm
transportCost
netAmount
```

---

# 37. Frontend Must Not Calculate Logistics Cost

The frontend may display:

```text
Distance: 93.34 km
Transport cost: ₹4,667
Net amount: ₹41,333
```

but should not be the source of truth for these values.

Do not implement business authority like:

```text
distance = frontend calculation
transportCost = frontend calculation
netAmount = frontend calculation
```

Instead:

```text
Farmer coordinates
       +
Buyer coordinates
       +
Transport rate
       +
Transaction quantity
       ↓
Backend
       ↓
Distance
       ↓
Transport Cost
       ↓
Net Amount
```

---

# 38. Logistics and Authentication

Farmer and buyer locations originate from the Authentication/Profile module.

The relationship is:

```text
Farmer Profile
    │
    └── latitude / longitude
              │
              ▼
         Logistics
              ▲
              │
    ┌─────────┴─────────┐
    │                   │
Buyer Profile      Transport
latitude/longitude   Option
```

The Logistics calculation therefore depends on profile coordinates being available.

---

# 39. Logistics and Transaction

The Transaction module owns the workflow:

```text
OFFER_ACCEPTED
        ↓
TRANSPORT_ASSIGNED
        ↓
PICKUP_SCHEDULED
        ↓
CROP_DELIVERED
        ↓
COMPLETED
```

Logistics provides the transport-specific information required for the second stage.

Conceptually:

```text
Transaction
     │
     │ assign transport
     ▼
Logistics
     │
     ├── TransportOption
     ├── DistanceService
     └── Cost calculation
     │
     ▼
Transaction updated
```

---

# 40. Data Ownership

The Logistics module owns:

```text
vehicleType
vehicleNumber
capacityQuintals
ratePerKmPerQuintal
available
transport timestamps
distance calculation logic
```

The Transaction module owns:

```text
transaction status
transaction quantity
agreed price
gross amount
transport reference
distance value
transport cost
net amount
pickup schedule
```

The Farmer/Buyer profile modules own:

```text
latitude
longitude
location
```

This separation prevents unnecessary duplication of domain responsibilities.

---

# 41. Complete Logistics Workflow

```text
Officer Creates Vehicle
        │
        ▼
TransportOption
available = true
        │
        ▼
Transaction reaches
OFFER_ACCEPTED
        │
        ▼
Officer selects transport
        │
        ▼
Lock Transaction
        │
        ▼
Lock TransportOption
        │
        ▼
Validate availability
        │
        ▼
Validate capacity
        │
        ▼
Load Farmer Coordinates
        │
        ▼
Load Buyer Coordinates
        │
        ▼
Calculate Haversine Distance
        │
        ▼
Calculate Transport Cost
        │
        ▼
Calculate Net Amount
        │
        ▼
Assign Transport
        │
        ▼
Transport available = false
        │
        ▼
TRANSPORT_ASSIGNED
```

---

# 42. Current Implementation Status

Implemented:

### Transport options

- `TransportOption` entity
- `VehicleType` enum
- Vehicle number
- Capacity in quintals
- Rate per kilometre per quintal
- Availability tracking
- Created/updated timestamps
- Transport repository
- Available transport queries
- Vehicle-type filtering
- Pessimistic transport locking
- Create transport DTO
- Transport response DTO
- Officer-only transport creation
- Available transport listing
- Transport lookup

### Distance

- `DistanceService`
- `HaversineDistanceService`
- Coordinate validation
- Haversine calculation
- 6371 km Earth radius
- `BigDecimal` distance result
- Two-decimal rounding

### Transaction integration

- Transport assignment
- Transaction state validation
- Transport availability validation
- Transport capacity validation
- Farmer coordinate validation
- Buyer coordinate validation
- Distance calculation
- Transport cost calculation
- Net amount calculation
- Transport availability update
- `TRANSPORT_ASSIGNED` transition

---

# 43. Current Logistics Flow

The current implemented flow is:

```text
Transport Available
        │
        ▼
Transaction OFFER_ACCEPTED
        │
        ▼
Officer Assigns Transport
        │
        ▼
Distance Calculated
        │
        ▼
Transport Cost Calculated
        │
        ▼
Net Amount Calculated
        │
        ▼
Transport Marked Unavailable
        │
        ▼
TRANSPORT_ASSIGNED
```

The next transaction stage is handled by the Transaction module:

```text
TRANSPORT_ASSIGNED
        ↓
PICKUP_SCHEDULED
```

---

# 44. Future Improvements

The current implementation provides the logistics foundation required for the hackathon workflow.

Potential future improvements include:

- Real routing distance instead of straight-line Haversine distance
- Estimated travel time
- Transport driver details
- Driver contact information
- Vehicle status history
- Transport release after transaction completion
- Multiple transport providers
- Dynamic transport pricing
- Route optimization
- Live vehicle tracking
- Delivery proof
- Location-aware transport recommendations

These are future enhancements and are not part of the current implemented workflow.

---

# 45. Important Design Principles

### Backend-authoritative calculations

Distance and transport cost are calculated by the backend.

### Database-authoritative availability

Transport availability is stored in PostgreSQL and protected with locking.

### Capacity validation

A transport cannot be assigned when its capacity is insufficient.

### Officer-controlled assignment

Transport assignment is an operational officer action.

### Transactional consistency

Transaction and transport changes are handled within a transaction boundary.

### BigDecimal for financial calculations

Transport pricing and costs use `BigDecimal`.

### Service abstraction

Distance calculation is exposed through `DistanceService`, allowing the implementation to evolve without changing transaction logic.

### Separation of concerns

Logistics manages transport.

Transaction manages the sale lifecycle.

Authentication manages profile coordinates.

---

# 46. Summary

The Logistics module provides the transport and distance-calculation layer for Kisan Suvidha.

The current flow is:

```text
Transport Option Created
        │
        ▼
Available Transport
        │
        ▼
Accepted Transaction
        │
        ▼
Officer Assigns Vehicle
        │
        ▼
Validate Capacity
        │
        ▼
Calculate Distance
        │
        ▼
Calculate Transport Cost
        │
        ▼
Calculate Net Amount
        │
        ▼
Mark Transport Unavailable
        │
        ▼
TRANSPORT_ASSIGNED
        │
        ▼
Transaction continues
to pickup scheduling
```

The module ensures that logistics-related data and financial calculations remain backend-authoritative and concurrency-safe.
