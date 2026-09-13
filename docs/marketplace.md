# Marketplace & Offer Management

## 1. Overview

The Marketplace module connects farmers who have registered crops with buyers who want to purchase those crops.

Its core responsibility is to manage the lifecycle of buyer offers against farmer crops.

The module currently supports:

- Buyer offer creation
- Farmer offer discovery
- Buyer offer discovery
- Offer acceptance
- Offer rejection
- Farmer counter-offers
- Buyer acceptance of counter-offers
- Buyer rejection of counter-offers
- Offer status management
- Buyer/farmer ownership validation
- Concurrency-safe offer updates
- Integration with transaction creation

The module is organized as:

```text
marketplace/
├── controller/
├── dto/
├── entity/
├── enums/
├── repository/
└── service/
```

---

## 2. Responsibilities

The Marketplace module is responsible for:

1. Allowing buyers to make offers against available farmer crops.
2. Storing the offered price and requested quantity.
3. Allowing farmers to view offers received for their crops.
4. Allowing buyers to view their own offers.
5. Allowing farmers to accept, reject, or counter an offer.
6. Allowing buyers to accept or reject a counter-offer.
7. Maintaining the offer state machine.
8. Preventing unauthorized users from modifying offers.
9. Providing concurrency-safe offer state updates.
10. Providing the accepted offer that can later become a transaction.

The module is **not** responsible for:

- Authentication/JWT generation
- Crop registration
- Procurement queues
- Transport assignment
- Distance calculation
- Pickup scheduling
- Final transaction lifecycle

Those responsibilities belong to other modules.

---

# 3. Package Structure

```text
marketplace/
├── controller/
│   └── OfferController
│
├── dto/
│   ├── CreateOfferRequest
│   ├── CounterOfferRequest
│   └── OfferResponse
│
├── entity/
│   └── Offer
│
├── enums/
│   └── OfferStatus
│
├── repository/
│   └── OfferRepository
│
└── service/
    └── OfferService
```

The module follows the standard layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

JPA entities are kept inside the persistence layer and REST APIs use DTOs.

---

# 4. Marketplace Domain Model

The main relationship is:

```text
Farmer
  │
  └── Crop
       │
       └── Offer
            │
            └── Buyer
```

A farmer owns the crop.

A buyer creates an offer against that crop.

The offer stores the commercial proposal without duplicating the complete crop or buyer data.

---

# 5. Offer Entity

The central entity is:

```text
Offer
├── id
├── crop
├── buyer
├── offeredPrice
├── quantity
├── status
├── counterPrice
├── createdAt
└── updatedAt
```

---

## 5.1 ID

```text
id
```

Database-generated primary key.

---

## 5.2 Crop

```text
crop
```

The crop for which the buyer is making the offer.

The relationship is many-to-one:

```text
Crop
  │
  └──< Offer
```

Multiple buyers may make offers against the same crop.

---

## 5.3 Buyer

```text
buyer
```

The buyer who created the offer.

The authenticated buyer is determined from the JWT when creating an offer.

The frontend does not need to supply a buyer ID to identify the current buyer.

---

## 5.4 Offered Price

```text
offeredPrice
```

The price proposed by the buyer.

The value is represented using:

```java
BigDecimal
```

rather than floating-point arithmetic.

---

## 5.5 Quantity

```text
quantity
```

The quantity the buyer wants to purchase.

The current implementation represents offer quantity using `BigDecimal`.

The service verifies the requested quantity against the crop's available quantity when the offer is converted into a transaction.

---

## 5.6 Status

```text
status
```

Represents the current state of the offer.

The status is controlled by the backend.

---

## 5.7 Counter Price

```text
counterPrice
```

Stores the farmer's counter-offered price.

The original `offeredPrice` is preserved.

This is important because the system needs to distinguish:

```text
Buyer's original offer
```

from:

```text
Farmer's counter-offer
```

---

## 5.8 Timestamps

The offer maintains:

```text
createdAt
updatedAt
```

These are automatically maintained by the entity lifecycle callbacks.

---

# 6. Offer Status

The module defines:

```java
public enum OfferStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COUNTERED
}
```

The statuses represent the negotiation lifecycle.

### PENDING

The buyer has submitted an offer and the farmer has not yet finalized it.

### ACCEPTED

The offer has been accepted.

An accepted offer can subsequently be converted into a transaction by the buyer.

### REJECTED

The offer has been rejected.

A rejected offer cannot proceed into the transaction workflow.

### COUNTERED

The farmer has responded with a different price.

The buyer can then either accept or reject the counter-offer.

---

# 7. Offer State Machine

The current offer lifecycle is:

```text
                 ┌───────────┐
                 │  PENDING  │
                 └─────┬─────┘
                       │
             ┌─────────┼─────────┐
             │         │         │
             ▼         ▼         ▼
         ACCEPTED   REJECTED   COUNTERED
             │                   │
             │             ┌─────┴─────┐
             │             │           │
             │             ▼           ▼
             │         ACCEPTED     REJECTED
             │
             ▼
        Transaction
```

The important negotiation path is:

```text
PENDING
   ↓
COUNTERED
   ↓
ACCEPTED
```

or:

```text
PENDING
   ↓
COUNTERED
   ↓
REJECTED
```

---

# 8. Create Offer Request

The buyer creates an offer using:

```java
public record CreateOfferRequest(
    @NotNull Long cropId,
    @NotNull @DecimalMin("0.001") BigDecimal quantity,
    @NotNull @DecimalMin("0.01") BigDecimal offeredPrice
) {}
```

The request contains:

```text
cropId
quantity
offeredPrice
```

It does not contain:

```text
buyerId
```

The buyer is derived from the authenticated JWT.

---

# 9. Create Offer API

Endpoint:

```http
POST /api/v1/offers
```

Authorization:

```text
BUYER
```

The service:

1. Gets the authenticated buyer.
2. Loads the requested crop.
3. Validates the crop.
4. Creates the offer.
5. Associates the offer with the buyer.
6. Associates the offer with the crop.
7. Initializes the status as `PENDING`.
8. Saves the offer.
9. Returns the offer response.

---

# 10. Create Offer Example

Request:

```http
POST /api/v1/offers
Authorization: Bearer <JWT>
Content-Type: application/json
```

Example:

```json
{
  "cropId": 1,
  "quantity": 20,
  "offeredPrice": 2200
}
```

The buyer's identity is obtained from the JWT.

The frontend should not send:

```json
{
  "buyerId": 1
}
```

---

# 11. Farmer Received Offers

Farmers can view offers received for their crops through:

```http
GET /api/v1/offers/received
```

Authorization:

```text
FARMER
```

The service identifies the farmer from the authenticated user and retrieves offers associated with crops owned by that farmer.

This is the primary API for the farmer marketplace dashboard.

---

# 12. Buyer Own Offers

Buyers can view their own offers through:

```http
GET /api/v1/offers/my
```

Authorization:

```text
BUYER
```

The backend derives the buyer from the JWT.

The response can be used to display:

```text
Crop
Offered quantity
Original price
Counter price
Offer status
Created time
Updated time
```

---

# 13. Offer Response

The marketplace uses an `OfferResponse` DTO.

Current response shape:

```java
public record OfferResponse(
    Long id,
    Long cropId,
    String cropName,
    Long buyerId,
    BigDecimal quantity,
    BigDecimal offeredPrice,
    BigDecimal counterPrice,
    OfferStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

The response gives the frontend enough information to display the offer without exposing the JPA entity.

---

# 14. Accepting an Offer

The farmer can accept a pending offer using:

```http
POST /api/v1/offers/{offerId}/accept
```

Authorization:

```text
FARMER
```

The service verifies:

1. The offer exists.
2. The authenticated farmer owns the crop associated with the offer.
3. The offer is in an acceptable state.
4. The offer is updated to:

```text
ACCEPTED
```

The accepted offer can then be used by the buyer to create a transaction.

---

# 15. Rejecting an Offer

The farmer can reject an offer using:

```http
POST /api/v1/offers/{offerId}/reject
```

Authorization:

```text
FARMER
```

The backend verifies farmer ownership before changing the state.

The offer becomes:

```text
REJECTED
```

A rejected offer cannot be used as an accepted commercial agreement.

---

# 16. Counter Offer

A farmer can negotiate instead of immediately accepting or rejecting.

Endpoint:

```http
POST /api/v1/offers/{offerId}/counter
```

Authorization:

```text
FARMER
```

Request:

```json
{
  "counterPrice": 2400
}
```

The backend:

1. Loads the offer.
2. Verifies that the authenticated farmer owns the associated crop.
3. Validates the counter price.
4. Preserves the original `offeredPrice`.
5. Stores the farmer's price in `counterPrice`.
6. Changes the status to:

```text
COUNTERED
```

---

# 17. Counter Offer Example

Suppose a buyer proposes:

```text
offeredPrice = ₹2200
```

The farmer wants:

```text
counterPrice = ₹2400
```

The offer becomes:

```text
offeredPrice = 2200
counterPrice = 2400
status = COUNTERED
```

The original buyer proposal is not overwritten.

This provides a clear negotiation history at the offer level.

---

# 18. Accept Counter Offer

The buyer can accept a counter-offer using:

```http
POST /api/v1/offers/{offerId}/accept-counter
```

Authorization:

```text
BUYER
```

The backend verifies:

1. The offer exists.
2. The authenticated buyer owns the offer.
3. The offer status is `COUNTERED`.
4. A counter price exists.

The status then becomes:

```text
ACCEPTED
```

The agreed price used later for transaction creation is the counter price.

---

# 19. Reject Counter Offer

The buyer can reject a counter-offer using:

```http
POST /api/v1/offers/{offerId}/reject-counter
```

Authorization:

```text
BUYER
```

The backend verifies:

1. The offer exists.
2. The authenticated buyer owns the offer.
3. The offer is currently `COUNTERED`.

The status becomes:

```text
REJECTED
```

---

# 20. Offer API Summary

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/v1/offers` | BUYER | Create offer |
| GET | `/api/v1/offers/received` | FARMER | View received offers |
| GET | `/api/v1/offers/my` | BUYER | View own offers |
| POST | `/api/v1/offers/{offerId}/accept` | FARMER | Accept offer |
| POST | `/api/v1/offers/{offerId}/reject` | FARMER | Reject offer |
| POST | `/api/v1/offers/{offerId}/counter` | FARMER | Counter offer |
| POST | `/api/v1/offers/{offerId}/accept-counter` | BUYER | Accept counter |
| POST | `/api/v1/offers/{offerId}/reject-counter` | BUYER | Reject counter |

---

# 21. Offer Repository

The repository is:

```text
OfferRepository
```

It supports the following queries:

```java
findByCropIdOrderByCreatedAtDesc(...)

findByBuyerIdOrderByCreatedAtDesc(...)

findByCropFarmerIdOrderByCreatedAtDesc(...)

findByCropFarmerIdAndStatusOrderByCreatedAtDesc(...)

existsByCropIdAndBuyerIdAndStatus(...)
```

These queries support:

- Farmer marketplace views
- Buyer offer history
- Crop-specific offer discovery
- Status filtering
- Duplicate active-offer prevention

---

# 22. Pessimistic Offer Lock

The repository provides:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        select o
        from Offer o
        where o.id = :id
        """)
Optional<Offer> findByIdForUpdate(@Param("id") Long id);
```

This is used when an offer state needs to be modified in a concurrency-sensitive workflow.

For example, two requests should not simultaneously change the same offer from:

```text
PENDING
```

into conflicting states.

The lock makes the database row authoritative during the state transition.

---

# 23. Concurrency Considerations

Offer state changes are transactional business operations.

Consider:

```text
Farmer
   │
   ├── Accept
   │
   └── Reject
```

If both requests arrive at approximately the same time, the backend must prevent an inconsistent final state.

The service can:

```text
BEGIN TRANSACTION
       ↓
findByIdForUpdate()
       ↓
Check current status
       ↓
Apply valid transition
       ↓
Save
       ↓
COMMIT
```

The database lock ensures that concurrent modifications are serialized.

---

# 24. Ownership Validation

Marketplace authorization has two dimensions:

```text
Role authorization
        +
Resource ownership
```

For example, having:

```text
ROLE_FARMER
```

does not automatically mean a farmer can modify every offer.

The farmer must also own the crop associated with that offer.

Similarly, a buyer can only accept/reject a counter-offer belonging to that buyer.

Conceptually:

```text
JWT
 │
 ▼
Authenticated User
 │
 ▼
Required Role?
 │
 ▼
Resource Ownership?
 │
 ▼
Business Operation
```

---

# 25. Business Rules

The Marketplace module follows these rules.

### Rule 1 — Buyer creates the offer

Only buyers can create offers.

### Rule 2 — Buyer identity is backend-controlled

The buyer ID comes from authentication.

### Rule 3 — Farmer ownership is verified

A farmer can only act on offers associated with crops owned by that farmer.

### Rule 4 — Buyer ownership is verified

A buyer can only act on their own offers.

### Rule 5 — Original offer price is preserved

When a farmer counters, `offeredPrice` remains the buyer's original price.

`counterPrice` stores the farmer's new price.

### Rule 6 — Counter acceptance uses counter price

When the buyer accepts a counter-offer, the counter price becomes the agreed price used by the transaction workflow.

### Rule 7 — Offer state is backend-controlled

The frontend cannot directly set:

```text
ACCEPTED
REJECTED
COUNTERED
```

It must call the corresponding API operation.

### Rule 8 — Accepted offers can proceed to transaction

An accepted offer provides the basis for creating a transaction.

---

# 26. Duplicate Offer Consideration

The repository contains:

```java
existsByCropIdAndBuyerIdAndStatus(...)
```

This supports checking whether the buyer already has an offer for the same crop in a particular state.

The purpose is to prevent inappropriate duplicate active offers where the business rules require uniqueness.

The exact allowed duplicate behavior remains controlled by the service-layer business rules.

---

# 27. Validation

The create-offer request validates:

```text
cropId
quantity
offeredPrice
```

Examples:

```text
cropId
    → required

quantity
    → must be greater than 0.001

offeredPrice
    → must be greater than 0.01
```

Counter-offer requests similarly validate the counter price.

Request validation is handled using Bean Validation.

Business validation remains in the service layer.

---

# 28. Crop Availability

The Marketplace module operates on crops managed by the Agriculture module.

When an offer is created, the crop is associated with the offer.

When the accepted offer is converted into a transaction, the transaction service re-checks the crop's current state and quantity.

This distinction is important.

An offer does not permanently reserve the crop simply because the offer exists.

The transaction workflow performs the authoritative availability check before completing the sale.

---

# 29. Offer vs Transaction

An offer represents:

```text
Negotiation / commercial proposal
```

A transaction represents:

```text
Accepted commercial agreement + actual sale workflow
```

Conceptually:

```text
Buyer
  │
  │ Offer
  ▼
Farmer
  │
  ├── Reject
  ├── Counter
  │     │
  │     └── Buyer accepts/rejects
  │
  └── Accept
        │
        ▼
     ACCEPTED
        │
        ▼
   Transaction
```

This separation keeps negotiation logic independent from logistics and delivery processing.

---

# 30. Transaction Creation From Accepted Offer

After an offer becomes:

```text
ACCEPTED
```

the buyer can create a transaction through the Transaction module.

The transaction creation endpoint is:

```http
POST /api/v1/transactions/from-offer/{offerId}
```

The Marketplace module provides the accepted offer.

The Transaction module then:

1. Locks the accepted offer.
2. Verifies buyer ownership.
3. Verifies the offer is `ACCEPTED`.
4. Checks that no transaction already exists.
5. Locks the crop.
6. Verifies crop availability.
7. Verifies offer quantity against crop quantity.
8. Determines the agreed price.
9. Creates the transaction.

The Marketplace module therefore ends its core responsibility at the accepted-offer boundary.

---

# 31. Agreed Price Logic

When a transaction is created from an accepted offer:

```text
counterPrice exists
        ↓
use counterPrice

otherwise
        ↓
use offeredPrice
```

Conceptually:

```java
BigDecimal agreedPrice =
        offer.getCounterPrice() != null
                ? offer.getCounterPrice()
                : offer.getOfferedPrice();
```

This ensures that a successfully negotiated counter-offer is reflected in the transaction price.

---

# 32. Example Negotiation

A complete negotiation could look like:

```text
Buyer
  │
  │ ₹2200 / quintal
  ▼
Offer PENDING
  │
  │ Farmer counters
  ▼
Offer COUNTERED
  │
  │ ₹2400 / quintal
  ▼
Buyer accepts
  │
  ▼
Offer ACCEPTED
  │
  ▼
Transaction created
  │
  ▼
Agreed price = ₹2400
```

If the buyer rejects:

```text
PENDING
   ↓
COUNTERED
   ↓
REJECTED
```

No transaction should be created from the rejected offer.

---

# 33. Security

Marketplace endpoints use Spring Security method-level authorization.

Examples:

### Buyer

```java
@PreAuthorize("hasRole('BUYER')")
```

Used for:

```text
Create offer
View own offers
Accept counter
Reject counter
```

### Farmer

```java
@PreAuthorize("hasRole('FARMER')")
```

Used for:

```text
View received offers
Accept offer
Reject offer
Counter offer
```

Role checks are not sufficient by themselves.

The service also performs resource ownership validation.

---

# 34. Error Cases

Common marketplace errors include:

| Situation | HTTP Status |
|---|---:|
| Offer not found | 404 |
| Crop not found | 404 |
| Buyer not found | 404 |
| Invalid offer request | 400 |
| Invalid counter price | 400 |
| Invalid offer state transition | 400 |
| Farmer does not own crop | 403 |
| Buyer does not own offer | 403 |
| Duplicate/conflicting offer | 409 |
| Unauthenticated request | 401 |

The application-level `GlobalExceptionHandler` converts these exceptions into the standard API error response.

---

# 35. Example Offer Response

A typical response can look like:

```json
{
  "id": 1,
  "cropId": 1,
  "cropName": "Wheat",
  "buyerId": 1,
  "quantity": 20,
  "offeredPrice": 2200,
  "counterPrice": null,
  "status": "PENDING",
  "createdAt": "2026-09-13T10:00:00",
  "updatedAt": "2026-09-13T10:00:00"
}
```

After a counter:

```json
{
  "id": 1,
  "cropId": 1,
  "cropName": "Wheat",
  "buyerId": 1,
  "quantity": 20,
  "offeredPrice": 2200,
  "counterPrice": 2400,
  "status": "COUNTERED",
  "createdAt": "2026-09-13T10:00:00",
  "updatedAt": "2026-09-13T10:10:00"
}
```

After the buyer accepts the counter:

```json
{
  "id": 1,
  "cropId": 1,
  "cropName": "Wheat",
  "buyerId": 1,
  "quantity": 20,
  "offeredPrice": 2200,
  "counterPrice": 2400,
  "status": "ACCEPTED",
  "createdAt": "2026-09-13T10:00:00",
  "updatedAt": "2026-09-13T10:15:00"
}
```

---

# 36. Frontend Integration

The frontend marketplace experience can be divided into two dashboards.

## Farmer marketplace

The farmer should be able to:

```text
View received offers
       ↓
Inspect buyer offer
       ↓
Accept
Reject
or Counter
```

For a counter:

```text
Enter counter price
       ↓
POST /offers/{id}/counter
       ↓
Display COUNTERED
```

---

## Buyer marketplace

The buyer should be able to:

```text
View available crops
       ↓
Select crop
       ↓
Create offer
       ↓
View offer status
       ↓
If COUNTERED:
    Accept Counter
    or
    Reject Counter
```

After acceptance:

```text
ACCEPTED
   ↓
Create Transaction
```

---

# 37. Frontend Should Not Manage Offer State Directly

The frontend can display state:

```text
PENDING
COUNTERED
ACCEPTED
REJECTED
```

but it should not directly modify the status.

For example, the frontend should not send:

```json
{
  "status": "ACCEPTED"
}
```

Instead it should call:

```http
POST /api/v1/offers/{offerId}/accept
```

or:

```http
POST /api/v1/offers/{offerId}/accept-counter
```

The backend validates and performs the state transition.

---

# 38. Frontend Marketplace Flow

### Buyer

```text
Browse Crop
    │
    ▼
Create Offer
    │
    ▼
PENDING
    │
    ├───────────────┐
    │               │
    ▼               ▼
ACCEPTED         COUNTERED
    │               │
    ▼          ┌────┴────┐
Transaction     ▼         ▼
             Accept    Reject
                │         │
                ▼         ▼
            ACCEPTED   REJECTED
                │
                ▼
           Transaction
```

### Farmer

```text
Received Offer
      │
      ├───────────────┬───────────────┐
      ▼               ▼               ▼
   Accept           Reject          Counter
      │               │               │
      ▼               ▼               ▼
  ACCEPTED        REJECTED        COUNTERED
                                      │
                                      ▼
                              Buyer responds
```

---

# 39. Module Interaction

The Marketplace module interacts with:

```text
Auth
  │
  ▼
Buyer / Farmer identity

Agriculture
  │
  ▼
Crop

Marketplace
  │
  ▼
Offer

Transaction
  │
  ▼
Accepted sale
```

Conceptually:

```text
             ┌──────────────┐
             │     Auth     │
             └──────┬───────┘
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
       Farmer                Buyer
          │                   │
          ▼                   │
        Crop ◄────────────────┘
          │       Offer
          ▼
        Offer
          │
          ▼
     ACCEPTED
          │
          ▼
    Transaction
```

---

# 40. Important Design Principles

### Backend-authoritative negotiation

The backend owns offer status and price transitions.

### JWT-derived identity

The current farmer or buyer is derived from authentication.

### Resource ownership

Role checks are combined with ownership checks.

### Immutable original offer price

The buyer's original proposal remains stored in `offeredPrice`.

### Explicit counter price

The farmer's negotiated price is stored separately in `counterPrice`.

### Transaction boundary

An accepted offer is not the same thing as a completed sale.

The Transaction module owns the actual sale lifecycle.

### Concurrency safety

Offer updates use database locking where state transitions require it.

### DTO-based API

REST APIs expose DTOs rather than JPA entities.

---

# 41. Current Implementation Status

Implemented:

- Offer entity
- Offer status enum
- Buyer-to-offer relationship
- Crop-to-offer relationship
- Create offer request
- Counter offer request
- Offer response DTO
- Offer repository
- Offer pessimistic locking
- Buyer offer creation
- Farmer received-offer listing
- Buyer own-offer listing
- Farmer accept
- Farmer reject
- Farmer counter
- Buyer accept counter
- Buyer reject counter
- Ownership validation
- JWT-based buyer/farmer identification
- Integration with transaction creation

The Marketplace module is therefore complete enough to support the buyer-farmer negotiation workflow and hand an accepted offer to the Transaction module.

---

# 42. Summary

The Marketplace module provides the negotiation layer of Kisan Suvidha.

The complete flow is:

```text
Farmer registers Crop
        │
        ▼
Crop becomes AVAILABLE
        │
        ▼
Buyer creates Offer
        │
        ▼
PENDING
        │
   ┌────┼───────────────┐
   │    │               │
   ▼    ▼               ▼
Accept Reject         Counter
   │    │               │
   ▼    ▼               ▼
ACCEPTED REJECTED    COUNTERED
                       │
                  ┌────┴────┐
                  ▼         ▼
               Accept     Reject
                  │         │
                  ▼         ▼
              ACCEPTED   REJECTED
                  │
                  ▼
             Transaction
```

The Marketplace module is responsible for the offer and negotiation lifecycle, while the Transaction module takes ownership once an offer has been accepted and a sale is created.
