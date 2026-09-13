# Agriculture & Crop Management

## 1. Overview

The Agriculture module manages the crops registered by farmers in Kisan Suvidha.

Its main responsibility is to provide a backend-authoritative representation of the crop a farmer intends to sell or take through the procurement workflow.

The module currently supports:

- Crop registration
- Viewing a farmer's own crops
- Viewing an individual crop
- Crop type classification
- Quantity and unit management
- Expected price
- Harvest date
- Crop availability status
- Secure farmer ownership through JWT authentication

The module is organized as:

```text
agriculture/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

---

## 2. Responsibilities

The Agriculture module is responsible for:

1. Creating crop records for authenticated farmers.
2. Associating every crop with its owning farmer.
3. Validating crop information.
4. Maintaining crop quantity and unit.
5. Maintaining the crop lifecycle status.
6. Providing crop information to downstream modules such as:
    - Procurement
    - Marketplace
    - Transactions
7. Preventing the frontend from choosing the farmer identity for a crop.

The module is **not** responsible for:

- Creating marketplace offers
- Assigning transport
- Calculating logistics cost
- Managing procurement queues
- Completing transactions

Those responsibilities belong to their respective modules.

---

## 3. Package Structure

```text
agriculture/
├── controller/
│   └── CropController
│
├── dto/
│   ├── CreateCropRequest
│   └── CropResponse
│
├── entity/
│   └── Crop
│
├── repository/
│   └── CropRepository
│
└── service/
    └── CropService
```

The module follows the application's standard layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

JPA entities are not returned directly from the controller. API responses use DTOs.

---

## 4. Crop Entity

The central entity is `Crop`.

Conceptually:

```text
Crop
├── id
├── farmer
├── cropName
├── cropType
├── quantity
├── unit
├── expectedPrice
├── harvestDate
├── status
├── createdAt
└── updatedAt
```

### Farmer relationship

Each crop belongs to one farmer.

Conceptually:

```text
Farmer
   │
   └──< Crop
```

The crop stores the farmer relationship through a JPA many-to-one association.

The farmer is determined from the authenticated JWT context when creating the crop.

The frontend therefore does not need to provide a `farmerId` in the create request.

---

## 5. Crop Fields

### ID

```text
id
```

Database-generated primary key.

### Farmer

```text
farmer
```

The farmer who owns the crop.

### Crop name

```text
cropName
```

Human-readable crop name.

Examples:

```text
Wheat
Rice
Potato
Tomato
Mustard
```

### Crop type

```text
cropType
```

Classifies the crop using `CropType`.

### Quantity

```text
quantity
```

The amount of crop currently represented by the record.

The backend uses `BigDecimal` for agricultural quantities to support precise values.

### Unit

```text
unit
```

The unit in which the crop quantity is represented.

### Expected price

```text
expectedPrice
```

The farmer's expected price for the crop.

The backend represents monetary values using `BigDecimal`.

### Harvest date

```text
harvestDate
```

The date associated with harvesting the crop.

### Status

```text
status
```

Represents the current availability/lifecycle state of the crop.

### Timestamps

```text
createdAt
updatedAt
```

Used to track creation and modification times.

---

## 6. Crop Type

The module defines:

```java
public enum CropType {
    CEREAL,
    PULSE,
    VEGETABLE,
    FRUIT,
    OILSEED,
    SPICE,
    OTHER
}
```

The purpose of `CropType` is to provide a controlled classification instead of storing arbitrary category strings.

Current categories:

| Value | Meaning |
|---|---|
| CEREAL | Cereal crops |
| PULSE | Pulse crops |
| VEGETABLE | Vegetables |
| FRUIT | Fruits |
| OILSEED | Oilseed crops |
| SPICE | Spices |
| OTHER | Crops outside the listed categories |

---

## 7. Crop Unit

The module defines:

```java
public enum CropUnit {
    KG,
    QUINTAL,
    TON
}
```

Supported units:

| Unit | Description |
|---|---|
| KG | Kilogram |
| QUINTAL | Quintal |
| TON | Metric ton |

The unit is stored explicitly with the crop quantity.

This is important because:

```text
20 KG
```

and:

```text
20 QUINTAL
```

represent completely different quantities.

---

## 8. Crop Status

The module defines:

```java
public enum CropStatus {
    AVAILABLE,
    RESERVED,
    SOLD,
    CANCELLED
}
```

### AVAILABLE

The crop is available for the relevant selling/procurement workflow.

### RESERVED

The crop has been reserved for a downstream workflow.

### SOLD

The crop quantity has been fully sold.

### CANCELLED

The crop record is no longer active because it has been cancelled.

The status is controlled by the backend and should not be treated as a frontend-only state.

---

## 9. Create Crop Request

Crop creation uses a DTO with the following fields:

```java
public record CreateCropRequest(
    @NotBlank String cropName,
    @NotNull CropType cropType,
    @NotNull @DecimalMin("0.001") BigDecimal quantity,
    @NotNull CropUnit unit,
    @NotNull @DecimalMin("0.01") BigDecimal expectedPrice,
    @NotNull LocalDate harvestDate
) {}
```

The important design decision is that the request does **not** contain:

```text
farmerId
```

The farmer is obtained from the authenticated JWT.

This prevents a farmer from attempting to create a crop under another farmer's account.

---

## 10. Crop Response

The API returns a `CropResponse` DTO rather than exposing the JPA entity directly.

The response represents information such as:

```text
id
farmerId
cropName
cropType
quantity
unit
expectedPrice
harvestDate
status
createdAt
updatedAt
```

The exact response structure should remain aligned with the current DTO implementation.

---

## 11. Repository Layer

The module uses Spring Data JPA.

The main repository is:

```text
CropRepository
```

Normal read operations can use standard Spring Data methods.

The repository also contains a pessimistic-locking method for operations that must safely modify crop state or quantity:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        select c
        from Crop c
        where c.id = :id
        """)
Optional<Crop> findByIdForUpdate(@Param("id") Long id);
```

This method is important for concurrency-sensitive operations.

---

## 12. Why Crop Locking Is Required

Crop quantity can be affected by concurrent business operations.

For example:

```text
Farmer has 20 quintals
        │
        ├── Buyer A attempts to purchase 15
        │
        └── Buyer B attempts to purchase 10
```

Without proper locking, both operations could read the same initial quantity and incorrectly proceed.

The backend therefore uses a pessimistic write lock when a downstream operation needs to modify the crop.

Conceptually:

```text
Transaction
    ↓
findByIdForUpdate()
    ↓
Lock crop row
    ↓
Verify current quantity/status
    ↓
Perform calculation
    ↓
Update crop
    ↓
Commit
    ↓
Release lock
```

This keeps PostgreSQL as the source of truth.

---

## 13. Controller

The main controller is:

```text
CropController
```

Base path:

```text
/api/v1/crops
```

### Create crop

```http
POST /api/v1/crops
```

Authorization:

```text
FARMER
```

The farmer is derived from the authenticated principal.

### Get my crops

```http
GET /api/v1/crops/my
```

Authorization:

```text
FARMER
```

Returns crops belonging to the currently authenticated farmer.

### Get crop by ID

```http
GET /api/v1/crops/{cropId}
```

Returns the requested crop.

The service layer is responsible for loading the crop and handling the not-found case.

---

## 14. API Summary

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/v1/crops` | FARMER | Register a crop |
| GET | `/api/v1/crops/my` | FARMER | Get authenticated farmer's crops |
| GET | `/api/v1/crops/{cropId}` | Authenticated | Get a crop |

---

## 15. Create Crop Example

Request:

```http
POST /api/v1/crops
Authorization: Bearer <JWT>
Content-Type: application/json
```

Example body:

```json
{
  "cropName": "Wheat",
  "cropType": "CEREAL",
  "quantity": 20,
  "unit": "QUINTAL",
  "expectedPrice": 2300,
  "harvestDate": "2026-09-20"
}
```

Notice that the request does not contain:

```json
{
  "farmerId": 2
}
```

The authenticated farmer is determined from the JWT.

---

## 16. Example Response

A successful response conceptually looks like:

```json
{
  "id": 1,
  "farmerId": 2,
  "cropName": "Wheat",
  "cropType": "CEREAL",
  "quantity": 20,
  "unit": "QUINTAL",
  "expectedPrice": 2300,
  "harvestDate": "2026-09-20",
  "status": "AVAILABLE",
  "createdAt": "2026-09-13T10:00:00",
  "updatedAt": "2026-09-13T10:00:00"
}
```

The exact timestamp values depend on when the record is created.

---

## 17. Validation

The create request validates important fields before the service performs business logic.

Examples:

```text
cropName
    → must not be blank

cropType
    → must be provided

quantity
    → must be positive

unit
    → must be provided

expectedPrice
    → must be positive

harvestDate
    → must be provided
```

Bean Validation is used for request-level validation.

Business-level validation remains in the service layer.

---

## 18. Business Rules

The Agriculture module follows these rules:

### Rule 1 — Farmer ownership is backend-controlled

A crop is associated with the authenticated farmer.

The frontend must not be trusted to specify the owner.

### Rule 2 — Quantity must be positive

A crop cannot be registered with zero or negative quantity.

### Rule 3 — Expected price must be positive

A crop cannot be registered with a non-positive expected price.

### Rule 4 — Crop status is backend-controlled

The frontend should not arbitrarily change lifecycle states.

### Rule 5 — Financial values use `BigDecimal`

Prices are represented with `BigDecimal` rather than floating-point types.

### Rule 6 — Crop quantity changes must be concurrency-safe

Operations that reserve, sell, or otherwise modify crop quantity should use the repository's pessimistic locking method where required.

---

## 19. Relationship With Marketplace

The marketplace module uses crops as the item being offered for sale.

The conceptual relationship is:

```text
Farmer
  │
  └── Crop
       │
       └── Offer
            │
            └── Buyer
```

A buyer creates an offer against a crop.

The offer references the crop rather than duplicating the crop's identity.

---

## 20. Relationship With Transactions

Once an accepted offer becomes a transaction, the transaction references the crop.

Conceptually:

```text
Crop
  │
  └── Offer
       │
       └── Transaction
```

The transaction creation workflow verifies that the crop is still available and that the requested offer quantity does not exceed the available crop quantity.

The crop is locked during this operation to prevent concurrent overselling.

After a successful transaction:

```text
remaining quantity > 0
        ↓
crop remains AVAILABLE

remaining quantity = 0
        ↓
crop becomes SOLD
```

This makes crop inventory part of the backend-authoritative transaction workflow.

---

## 21. Relationship With Procurement

A farmer can also take a registered crop through the procurement-centre queue.

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

The queue entry references the crop being taken to the procurement centre.

This allows the procurement module to manage queue operations without duplicating crop information.

---

## 22. Security

All crop operations are protected by Spring Security according to their required role.

Farmer-specific operations use:

```java
@PreAuthorize("hasRole('FARMER')")
```

The backend derives the authenticated user from:

```java
Authentication authentication
```

and the JWT filter places the user ID into the authenticated principal.

Conceptually:

```java
Long userId = (Long) authentication.getPrincipal();
```

The service then resolves the corresponding farmer profile.

---

## 23. Error Cases

Common agriculture-related errors include:

### Crop not found

```text
404 NOT_FOUND
```

Returned when the requested crop does not exist.

### Invalid crop data

```text
400 BAD_REQUEST
```

Examples:

```text
quantity <= 0
expectedPrice <= 0
missing crop name
missing crop type
missing unit
missing harvest date
```

### Unauthorized request

```text
401 UNAUTHORIZED
```

Returned when authentication is missing or invalid.

### Forbidden request

```text
403 FORBIDDEN
```

Returned when an authenticated user does not have the required role.

### Concurrent state conflict

A downstream operation may reject the request if the crop is no longer available or its quantity has changed before the operation obtains the lock.

---

## 24. Complete Crop Workflow

The current crop workflow is:

```text
Farmer Login
     │
     ▼
JWT Issued
     │
     ▼
POST /api/v1/crops
     │
     ▼
Authenticate Farmer
     │
     ▼
Validate Crop Request
     │
     ▼
Create Crop
     │
     ▼
Status = AVAILABLE
     │
     ├───────────────────┐
     ▼                   ▼
Procurement           Marketplace
     │                   │
QueueEntry              Offer
                         │
                         ▼
                    Transaction
                         │
                         ▼
                 Crop Quantity Updated
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
       Quantity > 0          Quantity = 0
              │                     │
              ▼                     ▼
         AVAILABLE                SOLD
```

---

## 25. Frontend Integration

The TanStack Start/React frontend should interact with the Agriculture module through the REST APIs.

### Register crop

```text
POST /api/v1/crops
```

Send:

```json
{
  "cropName": "Wheat",
  "cropType": "CEREAL",
  "quantity": 20,
  "unit": "QUINTAL",
  "expectedPrice": 2300,
  "harvestDate": "2026-09-20"
}
```

Include:

```http
Authorization: Bearer <JWT>
```

### Load farmer crops

```text
GET /api/v1/crops/my
```

The frontend should not append the farmer ID.

### Load a crop

```text
GET /api/v1/crops/{cropId}
```

Use the returned backend data as the source of truth.

---

## 26. Frontend State vs Backend State

The frontend can maintain temporary UI state such as:

```text
selected crop
loading state
form values
error state
```

But it should not become the authoritative source for:

```text
crop ownership
crop status
remaining quantity
transaction quantity
sold state
```

Those values are determined by the backend and PostgreSQL.

---

## 27. Data Flow With Other Modules

The Agriculture module is a foundational domain module.

```text
                 ┌──────────────┐
                 │    Farmer    │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │     Crop     │
                 └──────┬───────┘
                        │
             ┌──────────┼──────────┐
             │          │          │
             ▼          ▼          ▼
        Procurement  Marketplace  Transaction
             │          │          │
             ▼          ▼          ▼
           Queue       Offer     Sale lifecycle
```

This keeps crop information centralized instead of duplicating it across modules.

---

## 28. Current Status

The Agriculture/Crop module is implemented.

Implemented:

- Crop entity
- Farmer-to-crop relationship
- Crop type enum
- Crop unit enum
- Crop status enum
- Create crop request
- Crop response DTO
- Crop repository
- Pessimistic crop locking
- Crop service
- Crop controller
- Farmer-only crop registration
- Farmer's crop listing
- Individual crop lookup
- Bean validation
- JWT-based farmer identification
- Integration with marketplace and transaction workflows

The module currently provides the crop foundation required by the Procurement, Marketplace, and Transaction modules.
