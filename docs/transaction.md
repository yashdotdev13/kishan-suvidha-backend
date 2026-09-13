# Transaction Management

## 1. Overview

The Transaction module manages the sale lifecycle after a buyer and farmer have reached an accepted offer.

An accepted marketplace offer represents an agreed commercial deal. The Transaction module turns that accepted offer into an actual sale workflow and then manages logistics and delivery stages until completion or cancellation.

The current transaction lifecycle is:

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

A transaction may also move to:

```text
CANCELLED
```

when the corresponding cancellation workflow is implemented/used.

The module is organized as:

```text
transaction/
├── controller/
├── dtos/
├── entity/
├── enums/
├── repository/
└── service/
```

---

## 2. Responsibilities

The Transaction module is responsible for:

1. Creating a transaction from an accepted offer.
2. Validating buyer ownership of the accepted offer.
3. Preventing the same offer from creating multiple transactions.
4. Re-validating crop availability and quantity.
5. Calculating the gross transaction amount.
6. Determining the agreed price.
7. Tracking farmer and buyer relationships.
8. Tracking transaction quantity.
9. Tracking transport assignment.
10. Tracking calculated distance.
11. Calculating transport cost.
12. Calculating the resulting net amount.
13. Scheduling pickup.
14. Marking the crop as delivered.
15. Managing transaction lifecycle state.
16. Providing buyer and farmer transaction history.

The module works closely with:

```text
Marketplace
Agriculture
Logistics
Auth
```

---

# 3. Package Structure

```text
transaction/
├── controller/
│   └── TransactionController
│
├── dtos/
│   ├── AssignTransportRequest
│   ├── SchedulePickupRequest
│   └── TransactionResponse
│
├── entity/
│   └── Transaction
│
├── enums/
│   └── TransactionStatus
│
├── repository/
│   └── TransactionRepository
│
└── service/
    └── TransactionService
```

The standard flow is:

```text
Controller
    ↓
TransactionService
    ↓
TransactionRepository
    ↓
PostgreSQL
```

The service also interacts with repositories from related modules when creating or updating a transaction.

---

# 4. Transaction Domain Model

A transaction connects the accepted offer with the actual sale:

```text
Offer
  │
  ▼
Transaction
  │
  ├── Crop
  ├── Farmer
  ├── Buyer
  └── TransportOption
```

Conceptually:

```text
Farmer
  │
  └── Crop
       │
       └── Offer
            │
            ▼
        Transaction
            │
            ├── Buyer
            └── Logistics
```

---

# 5. Transaction Entity

The transaction contains the following major fields:

```text
Transaction
├── id
├── offer
├── crop
├── farmer
├── buyer
├── quantity
├── agreedPrice
├── grossAmount
├── transportCost
├── netAmount
├── transportOption
├── distanceKm
├── status
├── pickupScheduledAt
├── createdAt
├── updatedAt
├── completedAt
└── cancelledAt
```

---

## 5.1 ID

```text
id
```

Database-generated primary key.

---

## 5.2 Offer

```text
offer
```

The accepted marketplace offer that created the transaction.

The relationship is one-to-one:

```text
Offer
  │
  └── Transaction
```

The database enforces uniqueness on the offer reference so that one offer cannot create multiple transactions.

---

## 5.3 Crop

```text
crop
```

The crop being sold.

The transaction retains a direct reference to the crop so the sale can be associated with the agricultural inventory.

---

## 5.4 Farmer

```text
farmer
```

The farmer selling the crop.

This is derived from the crop/offer relationship when the transaction is created.

---

## 5.5 Buyer

```text
buyer
```

The buyer purchasing the crop.

The buyer is derived from the accepted offer.

---

## 5.6 Quantity

```text
quantity
```

The quantity being sold in this transaction.

The current implementation uses:

```java
BigDecimal
```

for precise quantity representation.

The transaction quantity is determined from the accepted offer and must not exceed the crop's currently available quantity.

---

## 5.7 Agreed Price

```text
agreedPrice
```

The price agreed between buyer and farmer.

The backend determines this value from the accepted offer.

The rule is:

```text
counterPrice exists
        ↓
agreedPrice = counterPrice

otherwise
        ↓
agreedPrice = offeredPrice
```

---

## 5.8 Gross Amount

```text
grossAmount
```

The total value before transport cost.

Formula:

```text
grossAmount = agreedPrice × quantity
```

Example:

```text
agreedPrice = ₹2300 / quintal
quantity    = 20 quintals

grossAmount = 2300 × 20
            = ₹46,000
```

---

## 5.9 Transport Cost

```text
transportCost
```

The logistics cost associated with transporting the crop.

At initial transaction creation:

```text
transportCost = 0
```

Once transport is assigned, the backend calculates the actual transport cost.

---

## 5.10 Net Amount

```text
netAmount
```

The transaction amount after transport cost.

Formula:

```text
netAmount = grossAmount - transportCost
```

Example:

```text
grossAmount   = ₹46,000
transportCost = ₹4,667

netAmount     = ₹41,333
```

---

## 5.11 Transport Option

```text
transportOption
```

References the logistics/transport option assigned to the transaction.

It may be `null` when the transaction is first created.

---

## 5.12 Distance

```text
distanceKm
```

The calculated distance between the farmer and buyer locations.

It is initially unset and populated during transport assignment.

---

## 5.13 Status

```text
status
```

Represents the current transaction lifecycle state.

---

## 5.14 Pickup Scheduled At

```text
pickupScheduledAt
```

Stores the scheduled pickup date and time.

It is populated when an officer schedules pickup.

---

## 5.15 Lifecycle Timestamps

The transaction tracks:

```text
createdAt
updatedAt
completedAt
cancelledAt
pickupScheduledAt
```

These timestamps allow the frontend and operational workflows to understand transaction history.

---

# 6. Transaction Status

The module defines:

```java
public enum TransactionStatus {
    OFFER_ACCEPTED,
    TRANSPORT_ASSIGNED,
    PICKUP_SCHEDULED,
    CROP_DELIVERED,
    COMPLETED,
    CANCELLED
}
```

---

## 6.1 OFFER_ACCEPTED

The marketplace offer has been accepted and a transaction has been created.

No transport has yet been assigned.

---

## 6.2 TRANSPORT_ASSIGNED

A transport option has been assigned.

At this point the backend has also calculated:

```text
distanceKm
transportCost
netAmount
```

---

## 6.3 PICKUP_SCHEDULED

A transport option has been assigned and an officer has scheduled the pickup.

The transaction contains:

```text
pickupScheduledAt
```

---

## 6.4 CROP_DELIVERED

The pickup/delivery workflow has been completed to the point where the crop is marked as delivered.

---

## 6.5 COMPLETED

The final transaction state.

This is the next lifecycle stage after:

```text
CROP_DELIVERED
```

The final completion operation is not yet implemented in the current workflow.

---

## 6.6 CANCELLED

Represents a cancelled transaction.

The current documentation reserves this state for cancellation workflows.

---

# 7. Transaction State Machine

The intended lifecycle is:

```text
┌─────────────────┐
│ OFFER_ACCEPTED  │
└────────┬────────┘
         │
         ▼
┌──────────────────────┐
│ TRANSPORT_ASSIGNED   │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ PICKUP_SCHEDULED     │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ CROP_DELIVERED       │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ COMPLETED            │
└──────────────────────┘
```

A cancellation path may terminate the transaction:

```text
Active Transaction
       │
       ▼
  CANCELLED
```

The currently implemented workflow has reached:

```text
OFFER_ACCEPTED
        ↓
TRANSPORT_ASSIGNED
        ↓
PICKUP_SCHEDULED
        ↓
CROP_DELIVERED
```

`COMPLETED` remains the next implementation step.

---

# 8. Transaction Repository

The repository is:

```text
TransactionRepository
```

It supports:

```java
findByOfferId(Long offerId)

findByFarmerIdOrderByCreatedAtDesc(Long farmerId)

findByBuyerIdOrderByCreatedAtDesc(Long buyerId)

findByStatusOrderByCreatedAtDesc(TransactionStatus status)

existsByOfferId(Long offerId)
```

These methods support:

- Finding a transaction from an offer
- Farmer sales history
- Buyer transaction history
- Status-based operational views
- Duplicate transaction prevention

---

# 9. Transaction Pessimistic Lock

The repository also provides:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        SELECT t
        FROM Transaction t
        WHERE t.id = :transactionId
        """)
Optional<Transaction> findByIdForUpdate(
        @Param("transactionId") Long transactionId
);
```

This method is used when modifying transaction state.

It prevents concurrent requests from applying conflicting lifecycle transitions.

Conceptually:

```text
Request
   ↓
Lock transaction row
   ↓
Read current status
   ↓
Validate transition
   ↓
Update transaction
   ↓
Commit
```

---

# 10. Creating a Transaction From an Offer

The transaction creation endpoint is:

```http
POST /api/v1/transactions/from-offer/{offerId}
```

Authorization:

```text
BUYER
```

This operation converts an accepted marketplace offer into a transaction.

---

# 11. Transaction Creation Workflow

The backend performs the following steps:

```text
Buyer
  │
  ▼
Authenticated User
  │
  ▼
Load Buyer
  │
  ▼
Lock Offer
  │
  ▼
Verify Buyer Owns Offer
  │
  ▼
Verify Offer = ACCEPTED
  │
  ▼
Verify Transaction Does Not Exist
  │
  ▼
Lock Crop
  │
  ▼
Verify Crop = AVAILABLE
  │
  ▼
Verify Offer Quantity <= Crop Quantity
  │
  ▼
Determine Agreed Price
  │
  ▼
Calculate Gross Amount
  │
  ▼
Create Transaction
  │
  ▼
Reduce Crop Quantity
  │
  ▼
Update Crop Status if Required
  │
  ▼
Commit
```

The entire operation is transactional.

---

# 12. Accepted Offer Requirement

A transaction cannot be created from an arbitrary offer.

The offer must have:

```text
status = ACCEPTED
```

If the offer is:

```text
PENDING
COUNTERED
REJECTED
```

the transaction creation operation must reject the request.

This keeps marketplace negotiation separate from actual sale creation.

---

# 13. Buyer Ownership

The authenticated buyer must own the accepted offer.

For example:

```text
Buyer A
  │
  └── Offer 10
```

Buyer B must not be able to call:

```text
POST /api/v1/transactions/from-offer/10
```

The backend verifies ownership using the authenticated principal.

The frontend is never trusted to declare the buyer identity.

---

# 14. Duplicate Transaction Prevention

An offer can create at most one transaction.

The repository supports:

```java
existsByOfferId(Long offerId)
```

and the transaction entity also has a unique one-to-one relationship with the offer.

The service checks that a transaction does not already exist before creating one.

This protects against duplicate transaction creation from repeated requests.

---

# 15. Crop Availability Check

The transaction workflow locks the crop and checks:

```text
crop.status == AVAILABLE
```

before creating the sale.

This is important because an offer may have been accepted earlier, while the crop's state may have changed before the transaction was created.

The transaction creation workflow therefore re-validates the current database state.

---

# 16. Crop Quantity Check

The transaction quantity must not exceed the available crop quantity.

Example:

```text
Crop quantity = 20 quintals
Offer quantity = 25 quintals
```

The transaction must be rejected.

Valid:

```text
Crop quantity = 20 quintals
Offer quantity = 15 quintals
```

After the transaction:

```text
Remaining crop quantity = 5 quintals
```

---

# 17. Crop Quantity Update

After a successful transaction:

```text
remainingQuantity =
    crop.quantity - transaction.quantity
```

The result determines the crop state.

### Remaining quantity greater than zero

```text
Crop status = AVAILABLE
```

### Remaining quantity equal to zero

```text
Crop status = SOLD
```

This makes the Agriculture module's crop quantity consistent with the transaction.

---

# 18. Agreed Price Calculation

The transaction service determines the final agreed price from the offer.

The logic is:

```text
counterPrice != null
        ?
counterPrice
        :
offeredPrice
```

Example without counter:

```text
offeredPrice = ₹2200
counterPrice = null

agreedPrice = ₹2200
```

Example with counter:

```text
offeredPrice = ₹2200
counterPrice = ₹2400

agreedPrice = ₹2400
```

---

# 19. Gross Amount Calculation

The gross amount is:

```text
grossAmount = agreedPrice × quantity
```

Example:

```text
quantity = 20 quintals
agreedPrice = ₹2300/quintal

grossAmount = ₹46,000
```

The calculation uses `BigDecimal`.

---

# 20. Initial Financial State

Immediately after transaction creation:

```text
grossAmount   = agreedPrice × quantity
transportCost = 0
netAmount     = grossAmount
```

Example:

```text
grossAmount   = ₹46,000
transportCost = ₹0
netAmount     = ₹46,000
```

Transport cost is populated later when an officer assigns transport.

---

# 21. Assigning Transport

Transport assignment is an officer operation.

Endpoint:

```http
POST /api/v1/transactions/{transactionId}/assign-transport
```

Authorization:

```text
OFFICER
```

The request identifies the transport option.

The endpoint uses:

```java
@PreAuthorize("hasRole('OFFICER')")
```

---

# 22. Transport Assignment Workflow

The backend performs:

```text
Lock Transaction
      │
      ▼
Verify status = OFFER_ACCEPTED
      │
      ▼
Lock TransportOption
      │
      ▼
Verify transport is available
      │
      ▼
Verify capacity >= transaction quantity
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
Mark Transport Unavailable
      │
      ▼
Set status = TRANSPORT_ASSIGNED
      │
      ▼
Save
```

---

# 23. Transport Capacity

The assigned transport option must have enough capacity for the transaction.

Example:

```text
Transaction quantity = 20 quintals
Transport capacity   = 15 quintals
```

Assignment must be rejected.

Valid:

```text
Transaction quantity = 20 quintals
Transport capacity   = 25 quintals
```

---

# 24. Distance Calculation

Distance is calculated using the farmer and buyer coordinates.

The Transaction module delegates this to:

```text
DistanceService
```

The implementation is:

```text
HaversineDistanceService
```

The service calculates distance using latitude and longitude.

Conceptually:

```text
Farmer Coordinates
       │
       ▼
DistanceService
       │
       ▼
Buyer Coordinates
       │
       ▼
Distance in KM
```

---

# 25. Transport Cost Calculation

The current logistics pricing model is:

```text
transportCost =
    distanceKm
    × ratePerKmPerQuintal
    × quantityInQuintals
```

Example:

```text
distance = 93.34 km
rate = ₹2.50 / km / quintal
quantity = 20 quintals
```

Therefore:

```text
transportCost
= 93.34 × 2.50 × 20
= ₹4,667
```

The backend rounds the result to two decimal places using `HALF_UP`.

---

# 26. Net Amount Calculation

After transport assignment:

```text
netAmount = grossAmount - transportCost
```

Example:

```text
grossAmount   = ₹46,000
transportCost = ₹4,667

netAmount = ₹41,333
```

The backend performs this calculation.

The frontend should display the returned backend values rather than calculate financial totals independently.

---

# 27. Transport Availability

Once a transport option is successfully assigned:

```text
transportOption.available = false
```

This prevents the same transport option from being simultaneously assigned to another transaction.

The transport row is locked during assignment.

---

# 28. Schedule Pickup

After transport is assigned, an officer schedules pickup.

Endpoint:

```http
POST /api/v1/transactions/{transactionId}/schedule-pickup
```

Authorization:

```text
OFFICER
```

Request:

```json
{
  "pickupScheduledAt": "2026-09-15T10:30:00"
}
```

The backend validates that the pickup time is in the future.

---

# 29. Pickup Scheduling Workflow

The service:

```text
Lock Transaction
      │
      ▼
Verify status = TRANSPORT_ASSIGNED
      │
      ▼
Verify pickup time is in the future
      │
      ▼
Set pickupScheduledAt
      │
      ▼
Set status = PICKUP_SCHEDULED
      │
      ▼
Save
```

This creates the next transaction lifecycle stage.

---

# 30. Mark Crop Delivered

After the scheduled pickup/delivery process, an officer can mark the transaction as delivered.

Endpoint:

```http
POST /api/v1/transactions/{transactionId}/mark-delivered
```

Authorization:

```text
OFFICER
```

No request body is required.

The transaction must currently be:

```text
PICKUP_SCHEDULED
```

The service changes the status to:

```text
CROP_DELIVERED
```

---

# 31. Delivery Workflow

```text
PICKUP_SCHEDULED
       │
       │ Officer confirms delivery
       ▼
CROP_DELIVERED
```

The current implementation does not yet move this state automatically to `COMPLETED`.

---

# 32. Completion

The final intended state is:

```text
COMPLETED
```

The current transaction lifecycle has not yet implemented the final completion endpoint.

Therefore, the current documented implementation stops at:

```text
CROP_DELIVERED
```

The next lifecycle operation will be:

```text
CROP_DELIVERED
       ↓
COMPLETED
```

---

# 33. Transaction Controller

The Transaction controller exposes the transaction workflow.

Current operations include:

```text
Create transaction from accepted offer
View buyer transactions
View farmer sales
Assign transport
Schedule pickup
Mark delivered
```

Base path:

```text
/api/v1/transactions
```

---

# 34. Transaction API Summary

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/v1/transactions/from-offer/{offerId}` | BUYER | Create transaction from accepted offer |
| GET | `/api/v1/transactions/my` | BUYER | View buyer transactions |
| GET | `/api/v1/transactions/my-sales` | FARMER | View farmer sales |
| POST | `/api/v1/transactions/{transactionId}/assign-transport` | OFFICER | Assign transport |
| POST | `/api/v1/transactions/{transactionId}/schedule-pickup` | OFFICER | Schedule pickup |
| POST | `/api/v1/transactions/{transactionId}/mark-delivered` | OFFICER | Mark crop delivered |

The final completion endpoint is not yet implemented.

---

# 35. Transaction Response

The current response DTO is:

```java
public record TransactionResponse(
    Long id,
    Long offerId,
    Long cropId,
    String cropName,
    Long farmerId,
    Long buyerId,
    Long transportOptionId,
    String vehicleNumber,
    BigDecimal quantity,
    BigDecimal agreedPrice,
    BigDecimal grossAmount,
    BigDecimal distanceKm,
    BigDecimal transportCost,
    BigDecimal netAmount,
    TransactionStatus status,
    LocalDateTime pickupScheduledAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime completedAt,
    LocalDateTime cancelledAt
) {}
```

The response is designed to give the frontend the transaction information needed for dashboards and lifecycle screens.

---

# 36. Example Initial Transaction Response

Conceptually:

```json
{
  "id": 1,
  "offerId": 1,
  "cropId": 1,
  "cropName": "Wheat",
  "farmerId": 2,
  "buyerId": 1,
  "transportOptionId": null,
  "vehicleNumber": null,
  "quantity": 20,
  "agreedPrice": 2300,
  "grossAmount": 46000,
  "distanceKm": null,
  "transportCost": 0,
  "netAmount": 46000,
  "status": "OFFER_ACCEPTED",
  "pickupScheduledAt": null,
  "createdAt": "2026-09-13T10:00:00",
  "updatedAt": "2026-09-13T10:00:00",
  "completedAt": null,
  "cancelledAt": null
}
```

---

# 37. Example After Transport Assignment

Conceptually:

```json
{
  "id": 1,
  "offerId": 1,
  "cropId": 1,
  "cropName": "Wheat",
  "farmerId": 2,
  "buyerId": 1,
  "transportOptionId": 1,
  "vehicleNumber": "BR01AB1234",
  "quantity": 20,
  "agreedPrice": 2300,
  "grossAmount": 46000,
  "distanceKm": 93.34,
  "transportCost": 4667,
  "netAmount": 41333,
  "status": "TRANSPORT_ASSIGNED",
  "pickupScheduledAt": null,
  "createdAt": "2026-09-13T10:00:00",
  "updatedAt": "2026-09-13T10:20:00",
  "completedAt": null,
  "cancelledAt": null
}
```

---

# 38. Example Pickup Scheduling

Request:

```http
POST /api/v1/transactions/1/schedule-pickup
Authorization: Bearer <OFFICER_JWT>
Content-Type: application/json
```

Body:

```json
{
  "pickupScheduledAt": "2026-09-15T10:30:00"
}
```

Result:

```text
status = PICKUP_SCHEDULED
```

and:

```text
pickupScheduledAt = 2026-09-15T10:30:00
```

---

# 39. Example Mark Delivered

Request:

```http
POST /api/v1/transactions/1/mark-delivered
Authorization: Bearer <OFFICER_JWT>
```

No request body is required.

Result:

```text
status = CROP_DELIVERED
```

---

# 40. Authentication & Authorization

Transaction operations use role-based authorization.

### Buyer

```java
@PreAuthorize("hasRole('BUYER')")
```

Used for:

```text
Create transaction from accepted offer
View own transactions
```

### Farmer

```java
@PreAuthorize("hasRole('FARMER')")
```

Used for:

```text
View own sales
```

### Officer

```java
@PreAuthorize("hasRole('OFFICER')")
```

Used for:

```text
Assign transport
Schedule pickup
Mark delivered
```

The backend derives the current user from the JWT.

---

# 41. Why Transport Operations Are Officer-Only

Transport assignment, pickup scheduling, and delivery confirmation are operational workflows.

The current design therefore places these operations under:

```text
OFFICER
```

authorization.

The frontend may present these actions only to officers, but backend authorization remains the actual security boundary.

---

# 42. Financial Calculation Authority

The frontend must never be treated as authoritative for:

```text
agreedPrice
grossAmount
transportCost
netAmount
distanceKm
```

These values are calculated or validated by the backend.

For example, the frontend should not send:

```json
{
  "grossAmount": 46000,
  "transportCost": 4667,
  "netAmount": 41333
}
```

to determine the transaction totals.

Instead, the backend calculates these values and returns them in `TransactionResponse`.

---

# 43. Concurrency Considerations

Transaction creation and lifecycle updates are concurrency-sensitive.

Important locks include:

```text
Offer lock
Crop lock
Transaction lock
TransportOption lock
```

Conceptually:

```text
Transaction Creation
        │
        ├── Lock Offer
        │
        └── Lock Crop
```

Transport assignment:

```text
Transaction
     │
     ├── Lock Transaction
     │
     └── Lock TransportOption
```

This prevents:

- Duplicate transaction creation
- Overselling crop quantity
- Double transport assignment
- Conflicting transaction status updates

---

# 44. Transactional Boundaries

Important operations are wrapped in transactions.

For example:

```java
@Transactional
```

is used for operations that must update multiple related records atomically.

Transaction creation may update:

```text
Offer
Crop
Transaction
```

Transport assignment may update:

```text
Transaction
TransportOption
```

These changes should succeed or fail together.

---

# 45. Error Cases

Common transaction errors include:

| Situation | HTTP Status |
|---|---:|
| Offer not found | 404 |
| Transaction not found | 404 |
| Crop not found | 404 |
| Transport option not found | 404 |
| Offer not accepted | 400 |
| Crop not available | 400 |
| Offer quantity exceeds crop quantity | 400 |
| Invalid transaction state | 400 |
| Pickup time is not in the future | 400 |
| Transport capacity insufficient | 400 |
| Transport unavailable | 409 |
| Transaction already exists for offer | 409 |
| Buyer does not own offer | 403 |
| Unauthenticated request | 401 |
| Insufficient role | 403 |

These errors are converted into the application's standard `ApiErrorResponse` through the global exception handler.

---

# 46. Frontend Integration

The frontend should present transactions according to user role.

## Buyer dashboard

The buyer can view:

```text
My Transactions
```

using:

```http
GET /api/v1/transactions/my
```

Important fields to display:

```text
Crop
Quantity
Agreed price
Gross amount
Transport cost
Net amount
Transaction status
Pickup schedule
```

---

## Farmer dashboard

The farmer can view:

```text
My Sales
```

using:

```http
GET /api/v1/transactions/my-sales
```

The response provides the sale information associated with the farmer.

---

## Officer dashboard

The officer needs operational transaction actions:

```text
View transaction
       ↓
Assign transport
       ↓
Schedule pickup
       ↓
Mark delivered
       ↓
Complete transaction
```

The final completion action will be added when the `COMPLETED` workflow is implemented.

---

# 47. Frontend Transaction State

The frontend should display the backend transaction status:

```text
OFFER_ACCEPTED
TRANSPORT_ASSIGNED
PICKUP_SCHEDULED
CROP_DELIVERED
COMPLETED
CANCELLED
```

It should not locally invent or mutate lifecycle states.

A status badge, timeline, or progress component can be built directly from the backend state.

---

# 48. Transaction Timeline UI

A suitable frontend representation is:

```text
✓ Offer Accepted
      │
      ▼
✓ Transport Assigned
      │
      ▼
✓ Pickup Scheduled
      │
      ▼
✓ Crop Delivered
      │
      ▼
○ Completed
```

The UI should derive the active stage from:

```text
transaction.status
```

and use:

```text
pickupScheduledAt
```

for the scheduled pickup information.

---

# 49. End-to-End Marketplace-to-Transaction Flow

The complete business workflow is:

```text
Farmer Registers Crop
        │
        ▼
Crop AVAILABLE
        │
        ▼
Buyer Creates Offer
        │
        ▼
Offer PENDING
        │
        ├───────────────┐
        │               │
        ▼               ▼
   Farmer Accepts    Farmer Counters
        │               │
        ▼               ▼
    ACCEPTED        COUNTERED
                        │
                        ▼
                Buyer Accepts Counter
                        │
                        ▼
                    ACCEPTED
                        │
                        ▼
             Create Transaction
                        │
                        ▼
                OFFER_ACCEPTED
                        │
                        ▼
              Assign Transport
                        │
                        ▼
             TRANSPORT_ASSIGNED
                        │
                        ▼
              Schedule Pickup
                        │
                        ▼
              PICKUP_SCHEDULED
                        │
                        ▼
               Mark Delivered
                        │
                        ▼
                CROP_DELIVERED
                        │
                        ▼
                   COMPLETED
```

---

# 50. Example Financial Flow

Consider:

```text
Crop quantity = 20 quintals
Buyer offer = ₹2200/quintal
Farmer counter = ₹2300/quintal
```

After the buyer accepts:

```text
agreedPrice = ₹2300
```

Transaction creation:

```text
grossAmount
= 2300 × 20
= ₹46,000
```

Transport assignment:

```text
distance = 93.34 km
rate = ₹2.50/km/quintal
quantity = 20 quintals
```

Transport cost:

```text
93.34 × 2.50 × 20
= ₹4,667
```

Net amount:

```text
₹46,000 - ₹4,667
= ₹41,333
```

The backend owns all of these calculations.

---

# 51. Module Interaction

The Transaction module is the point where marketplace negotiation becomes an operational sale.

```text
                  Auth
                   │
          ┌────────┴────────┐
          ▼                 ▼
       Farmer              Buyer
          │                 │
          ▼                 ▼
        Crop ◄──────────── Offer
          │                 │
          └────────┬────────┘
                   ▼
              Transaction
                   │
                   ▼
               Logistics
                   │
          ┌────────┼────────┐
          ▼        ▼        ▼
      Transport  Distance  Pickup
                   │
                   ▼
                Delivery
                   │
                   ▼
               Completed
```

---

# 52. Data Ownership

The Transaction module owns transaction-specific information:

```text
agreedPrice
grossAmount
transportCost
netAmount
distanceKm
transportOption
pickupScheduledAt
transaction status
transaction timestamps
```

The Agriculture module owns:

```text
crop
quantity
crop status
```

The Marketplace module owns:

```text
offer
offeredPrice
counterPrice
offer status
```

The Logistics module owns:

```text
transport option
vehicle
capacity
rate
availability
```

This separation keeps the modular monolith maintainable.

---

# 53. Current Implementation Status

Implemented:

- Transaction entity
- Transaction status enum
- Offer-to-transaction relationship
- Crop relationship
- Farmer relationship
- Buyer relationship
- Transaction repository
- Transaction pessimistic locking
- Transaction response DTO
- Transaction creation from accepted offer
- Buyer ownership validation
- Accepted-offer validation
- Duplicate transaction prevention
- Crop availability validation
- Crop quantity validation
- Crop quantity update
- Crop SOLD state when quantity reaches zero
- Agreed price calculation
- Gross amount calculation
- Transport assignment
- Transport capacity validation
- Farmer/buyer coordinate validation
- Haversine distance calculation
- Transport cost calculation
- Net amount calculation
- Transport availability update
- Pickup scheduling
- Future pickup-time validation
- Crop delivery status transition
- Buyer transaction listing
- Farmer sales listing
- JWT/RBAC authorization

Current lifecycle progress:

```text
OFFER_ACCEPTED       ✓
TRANSPORT_ASSIGNED   ✓
PICKUP_SCHEDULED     ✓
CROP_DELIVERED       ✓
COMPLETED            TODO
```

---

# 54. Remaining Transaction Work

The main remaining lifecycle operation is:

```text
CROP_DELIVERED
       ↓
COMPLETED
```

A future completion operation should define the exact business condition under which the transaction is considered fully completed.

Potential considerations include:

- Delivery confirmation
- Buyer confirmation
- Final settlement
- Completion timestamp
- Releasing transport availability

These should be implemented only after the intended business rule is finalized.

---

# 55. Important Design Principles

### Backend-authoritative financial calculations

The backend calculates prices, totals, distance, transport cost, and net amount.

### Database-authoritative inventory

The crop quantity and availability are validated against PostgreSQL.

### Transactional consistency

Multi-entity updates are performed within transaction boundaries.

### Concurrency protection

Pessimistic locks protect offers, crops, transactions, and transport options where required.

### Explicit lifecycle

Transaction status is represented using an enum and controlled by backend state transitions.

### Role-based operational control

Buyers create transactions, while officers manage transport and delivery operations.

### DTO-based API

JPA entities are not directly exposed through REST endpoints.

### Separation of domain responsibilities

Marketplace handles negotiation.

Agriculture handles crop inventory.

Logistics handles transport.

Transaction handles the sale lifecycle.

---

# 56. Summary

The Transaction module is the bridge between an accepted marketplace agreement and the physical completion of a crop sale.

The core flow is:

```text
Accepted Offer
      │
      ▼
Create Transaction
      │
      ▼
OFFER_ACCEPTED
      │
      ▼
Assign Transport
      │
      ▼
TRANSPORT_ASSIGNED
      │
      ▼
Schedule Pickup
      │
      ▼
PICKUP_SCHEDULED
      │
      ▼
Mark Delivered
      │
      ▼
CROP_DELIVERED
      │
      ▼
COMPLETED
```

During this workflow the backend maintains authoritative:

```text
quantity
agreed price
gross amount
transport cost
net amount
distance
transport assignment
pickup schedule
transaction status
```

The current implementation is complete through `CROP_DELIVERED`, with the final `COMPLETED` transition remaining to be implemented.
