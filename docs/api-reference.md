# Kisan Suvidha — API Reference

> **Status:** Current implemented API reference  
> **Backend:** Java 21 + Spring Boot + Spring Data JPA + PostgreSQL  
> **Base URL (local development):** `http://localhost:8081`  
> **API Base Path:** `/api/v1`

This document is the frontend-facing REST API contract for the currently implemented Kisan Suvidha backend.

---

## 1. Authentication

Most endpoints require a JWT.

### Authorization header

```http
Authorization: Bearer <JWT_TOKEN>
```

The backend determines the authenticated user from the JWT. Frontend clients must **not** send `farmerId`, `buyerId`, or `userId` when the backend can derive the identity from authentication.

### Roles

| Role | Meaning |
|---|---|
| `FARMER` | Farmer using crop registration, queues, and marketplace selling |
| `BUYER` | Buyer purchasing crops and creating offers |
| `OFFICER` | Procurement/logistics officer managing queues and transactions |

---

# 2. Standard Error Response

The backend uses the common error response structure:

```json
{
  "success": false,
  "errorCode": "BAD_REQUEST",
  "message": "Human-readable error message"
}
```

Typical HTTP statuses:

| HTTP | Meaning |
|---|---|
| `400` | Invalid request or invalid business state |
| `401` | Authentication required/failed |
| `403` | Authenticated user does not have permission |
| `404` | Requested resource does not exist |
| `409` | Conflict, such as unavailable transport or duplicate state |
| `500` | Unexpected server error |

Frontend should display the `message` when appropriate and use `errorCode` for stable client-side handling.

---

# 3. Auth API

Base path:

```text
/api/v1/auth
```

## 3.1 Register

```http
POST /api/v1/auth/register
```

**Authentication:** Public

### Request

The exact registration DTO should be treated as the source of truth in the backend implementation.

Conceptually, registration creates the application user and the corresponding role-specific profile.

Example:

```json
{
  "email": "farmer@example.com",
  "phone": "9876543210",
  "password": "password123",
  "role": "FARMER"
}
```

### Response

The frontend should use the actual response DTO returned by the current implementation.

---

## 3.2 Login

```http
POST /api/v1/auth/login
```

**Authentication:** Public

### Request

```json
{
  "email": "farmer@example.com",
  "password": "password123"
}
```

The current authentication implementation supports email/phone-based user identity.

### Response

The login response contains the JWT authentication information used for subsequent authenticated requests.

The frontend should store the token according to the application's authentication strategy and send it as a Bearer token.

---

## 3.3 Current User

```http
GET /api/v1/auth/me
```

**Authentication:** Required

Returns information about the currently authenticated user/profile.

### Example request

```http
GET /api/v1/auth/me
Authorization: Bearer <JWT_TOKEN>
```

The backend derives the user ID from the JWT.

---

## 3.4 Update Farmer Location

```http
PUT /api/v1/auth/farmer/me/location
```

**Role:** `FARMER`

### Request

```json
{
  "location": "Patna, Bihar, India"
}
```

The backend geocodes the supplied location and updates the farmer's location/coordinates.

### Important

Do not send a farmer ID. The farmer is identified from the authenticated JWT.

---

## 3.5 Update Buyer Location

```http
PUT /api/v1/auth/buyer/me/location
```

**Role:** `BUYER`

### Request

```json
{
  "location": "Patna, Bihar, India"
}
```

The backend geocodes the supplied location and updates the buyer's location/coordinates.

---

# 4. Agriculture / Crop API

Base path:

```text
/api/v1/crops
```

## 4.1 Register Crop

```http
POST /api/v1/crops
```

**Role:** `FARMER`

### Request

```json
{
  "cropName": "Wheat",
  "cropType": "CEREAL",
  "quantity": 20,
  "unit": "QUINTAL",
  "expectedPrice": 2300,
  "harvestDate": "2026-10-15"
}
```

### Crop types

```text
CEREAL
PULSE
VEGETABLE
FRUIT
OILSEED
SPICE
OTHER
```

### Units

```text
KG
QUINTAL
TON
```

### Initial status

A newly registered crop is normally created with:

```text
AVAILABLE
```

### Important

The frontend must not send `farmerId`. The backend derives the farmer from authentication.

---

## 4.2 Get My Crops

```http
GET /api/v1/crops/my
```

**Role:** `FARMER`

Returns crops belonging to the authenticated farmer.

---

## 4.3 Get Crop By ID

```http
GET /api/v1/crops/{cropId}
```

**Authentication:** Required

Example:

```http
GET /api/v1/crops/15
Authorization: Bearer <JWT_TOKEN>
```

---

# 5. Procurement Centre API

Base path:

```text
/api/v1/procurement-centres
```

## 5.1 Get All Procurement Centres

```http
GET /api/v1/procurement-centres
```

**Authentication:** Required

Returns available procurement centre information.

### Response fields

```json
{
  "id": 1,
  "name": "Patna Central Procurement Centre",
  "state": "Bihar",
  "district": "Patna",
  "location": "Patna, Bihar, India",
  "latitude": 25.5941,
  "longitude": 85.1376,
  "capacity": 1000,
  "currentToken": 10,
  "acceptingQueue": true,
  "createdAt": "2026-09-01T10:00:00",
  "updatedAt": "2026-09-12T12:00:00"
}
```

---

## 5.2 Get Centre By ID

```http
GET /api/v1/procurement-centres/{centreId}
```

**Authentication:** Required

---

## 5.3 Get Centres By State

```http
GET /api/v1/procurement-centres/state/{state}
```

**Authentication:** Required

Example:

```http
GET /api/v1/procurement-centres/state/Bihar
```

---

## 5.4 Get Centres By District

```http
GET /api/v1/procurement-centres/district/{district}
```

**Authentication:** Required

---

## 5.5 Get Centres By State And District

```http
GET /api/v1/procurement-centres/state/{state}/district/{district}
```

**Authentication:** Required

---

# 6. Procurement Queue API

Base path:

```text
/api/v1/queues
```

## 6.1 Join Queue

```http
POST /api/v1/queues/join
```

**Role:** `FARMER`

### Request

```json
{
  "cropId": 15,
  "procurementCentreId": 1
}
```

The backend assigns the queue token.

### Important

The frontend must **not** calculate or submit:

- token number
- farmers ahead
- estimated wait time

These values are backend-authoritative.

### Response

```json
{
  "id": 10,
  "cropId": 15,
  "procurementCentreId": 1,
  "procurementCentreName": "Patna Central Procurement Centre",
  "tokenNumber": 25,
  "currentToken": 20,
  "farmersAhead": 4,
  "estimatedWaitMinutes": 34,
  "status": "WAITING",
  "joinedAt": "2026-09-12T14:00:00",
  "servedAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

---

## 6.2 Get My Queues

```http
GET /api/v1/queues/my
```

**Role:** `FARMER`

Returns the authenticated farmer's active queue entries.

The current implementation returns entries in:

```text
WAITING
SERVING
```

---

## 6.3 Get Centre Queue

```http
GET /api/v1/queues/centre/{centreId}
```

**Role:** `OFFICER`

Returns the queue for the specified procurement centre.

---

## 6.4 Call Next Farmer

```http
POST /api/v1/queues/centre/{centreId}/next
```

**Role:** `OFFICER`

Moves the next waiting farmer to:

```text
SERVING
```

The centre's current token is updated by the backend.

### Concurrency

The procurement centre is locked during this operation so concurrent officer requests cannot issue conflicting queue states.

---

## 6.5 Complete Queue Entry

```http
POST /api/v1/queues/{queueEntryId}/complete
```

**Role:** `OFFICER`

The queue entry must currently be:

```text
SERVING
```

After completion:

```text
COMPLETED
```

---

## 6.6 Cancel Queue Entry

```http
POST /api/v1/queues/{queueEntryId}/cancel
```

**Role:** `FARMER`

A farmer can cancel their own queue entry only while it is:

```text
WAITING
```

The frontend does not send the farmer ID.

---

## Queue Status

```text
WAITING
SERVING
COMPLETED
CANCELLED
```

---

# 7. Marketplace / Offer API

Base path:

```text
/api/v1/offers
```

## 7.1 Create Offer

```http
POST /api/v1/offers
```

**Role:** `BUYER`

### Request

```json
{
  "cropId": 15,
  "quantity": 10,
  "offeredPrice": 2200
}
```

The buyer is derived from authentication.

### Validation

- `cropId` is required.
- Quantity must be greater than `0.001`.
- Offered price must be greater than `0.01`.

---

## 7.2 Get Received Offers

```http
GET /api/v1/offers/received
```

**Role:** `FARMER`

Returns offers received for crops owned by the authenticated farmer.

---

## 7.3 Get My Offers

```http
GET /api/v1/offers/my
```

**Role:** `BUYER`

Returns offers created by the authenticated buyer.

---

## 7.4 Accept Offer

```http
POST /api/v1/offers/{offerId}/accept
```

**Role:** `FARMER`

The authenticated farmer must own the crop associated with the offer.

Changes:

```text
PENDING → ACCEPTED
```

---

## 7.5 Reject Offer

```http
POST /api/v1/offers/{offerId}/reject
```

**Role:** `FARMER`

Changes:

```text
PENDING → REJECTED
```

---

## 7.6 Counter Offer

```http
POST /api/v1/offers/{offerId}/counter
```

**Role:** `FARMER`

### Request

```json
{
  "counterPrice": 2400
}
```

The original `offeredPrice` is preserved.

Status becomes:

```text
COUNTERED
```

---

## 7.7 Accept Counter Offer

```http
POST /api/v1/offers/{offerId}/accept-counter
```

**Role:** `BUYER`

The offer must be:

```text
COUNTERED
```

The transaction price will use the counter price.

---

## 7.8 Reject Counter Offer

```http
POST /api/v1/offers/{offerId}/reject-counter
```

**Role:** `BUYER`

The offer must be:

```text
COUNTERED
```

Status becomes:

```text
REJECTED
```

---

## Offer Status

```text
PENDING
ACCEPTED
REJECTED
COUNTERED
```

---

# 8. Transaction API

Base path:

```text
/api/v1/transactions
```

## 8.1 Create Transaction From Accepted Offer

```http
POST /api/v1/transactions/from-offer/{offerId}
```

**Role:** `BUYER`

The offer must:

1. Belong to the authenticated buyer.
2. Have status `ACCEPTED`.
3. Not already have a transaction.
4. Refer to an `AVAILABLE` crop.
5. Have quantity not greater than the remaining crop quantity.

### Backend calculations

The backend calculates:

```text
agreedPrice
grossAmount
transportCost
netAmount
```

At transaction creation:

```text
transportCost = 0
netAmount = grossAmount
status = OFFER_ACCEPTED
```

The crop quantity is reduced by the transaction quantity.

If no quantity remains, crop status becomes:

```text
SOLD
```

Otherwise it remains:

```text
AVAILABLE
```

---

## 8.2 Get My Purchases

```http
GET /api/v1/transactions/my
```

**Role:** `BUYER`

Returns transactions belonging to the authenticated buyer.

---

## 8.3 Get My Sales

```http
GET /api/v1/transactions/my-sales
```

**Role:** `FARMER`

Returns transactions belonging to the authenticated farmer.

---

## Transaction Response

The current transaction response contains:

```json
{
  "id": 1,
  "offerId": 5,
  "cropId": 15,
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
  "createdAt": "2026-09-12T15:00:00",
  "updatedAt": "2026-09-12T15:30:00",
  "completedAt": null,
  "cancelledAt": null
}
```

---

# 9. Logistics / Transport API

Base path:

```text
/api/v1/transport-options
```

## 9.1 Create Transport Option

```http
POST /api/v1/transport-options
```

**Role:** `OFFICER`

### Request

```json
{
  "vehicleType": "TRUCK",
  "vehicleNumber": "BR01AB1234",
  "capacityQuintals": 50,
  "ratePerKmPerQuintal": 2.50
}
```

### Vehicle types

```text
TRACTOR
MINI_TRUCK
TRUCK
TEMPO
OTHER
```

---

## 9.2 Get Available Transport Options

```http
GET /api/v1/transport-options/available
```

**Roles:** `FARMER`, `BUYER`, `OFFICER`

---

## 9.3 Get Transport Option By ID

```http
GET /api/v1/transport-options/{transportOptionId}
```

**Roles:** `FARMER`, `BUYER`, `OFFICER`

---

# 10. Transaction Logistics Workflow

## 10.1 Assign Transport

```http
POST /api/v1/transactions/{transactionId}/assign-transport
```

**Role:** `OFFICER`

### Request

```json
{
  "transportOptionId": 1
}
```

### Preconditions

Transaction status must be:

```text
OFFER_ACCEPTED
```

The transport option must:

- exist
- be available
- have sufficient capacity

Farmer and buyer coordinates must be available.

### Backend calculation

Distance is calculated from farmer coordinates to buyer coordinates.

```text
transportCost =
    distanceKm
    × ratePerKmPerQuintal
    × quantityInQuintals
```

Then:

```text
netAmount =
    grossAmount - transportCost
```

Transaction status becomes:

```text
TRANSPORT_ASSIGNED
```

The selected transport option becomes unavailable.

---

## 10.2 Schedule Pickup

```http
POST /api/v1/transactions/{transactionId}/schedule-pickup
```

**Role:** `OFFICER`

### Request

```json
{
  "pickupScheduledAt": "2026-09-15T10:30:00"
}
```

The pickup time must be in the future.

### Preconditions

Transaction status:

```text
TRANSPORT_ASSIGNED
```

### Result

```text
PICKUP_SCHEDULED
```

---

## 10.3 Mark Crop Delivered

```http
POST /api/v1/transactions/{transactionId}/mark-delivered
```

**Role:** `OFFICER`

No request body is required.

### Preconditions

Transaction status:

```text
PICKUP_SCHEDULED
```

### Result

```text
CROP_DELIVERED
```

---

# 11. Transaction State Machine

The current implemented transaction lifecycle is:

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

The current implementation has the `COMPLETED` state in the enum, but the completion endpoint is **not yet implemented**.

Cancellation is also represented in the enum for the broader transaction model, but the currently documented implemented API should not assume a cancellation endpoint exists.

---

# 12. Complete Farmer Workflow

```text
Register/Login
      │
      ▼
Update Location
      │
      ▼
Register Crop
      │
      ▼
View Procurement Centres
      │
      ▼
Join Queue
      │
      ▼
Track Queue
      │
      ▼
Receive Buyer Offer
      │
      ├── Reject
      │
      ├── Counter → Buyer Accepts Counter
      │
      └── Accept
               │
               ▼
       Transaction Created
               │
               ▼
       Officer Assigns Transport
               │
               ▼
       Pickup Scheduled
               │
               ▼
         Crop Delivered
```

---

# 13. Complete Buyer Workflow

```text
Register/Login
      │
      ▼
Update Location
      │
      ▼
Browse Available Crops
      │
      ▼
Create Offer
      │
      ▼
Farmer Accepts?
      │
      ├── No → Counter / Reject
      │
      └── Yes
             │
             ▼
      Transaction Created
             │
             ▼
      Track Transaction
             │
             ▼
      Pickup / Delivery
```

---

# 14. Complete Officer Workflow

```text
Login
  │
  ├── Procurement Queue
  │      │
  │      ├── View Centre Queue
  │      ├── Call Next Farmer
  │      └── Complete Queue Entry
  │
  └── Logistics
         │
         ├── Create Transport
         ├── Assign Transport
         ├── Schedule Pickup
         └── Mark Delivered
```

---

# 15. Frontend Integration Rules

## 15.1 Never trust client-side calculations

The frontend may display estimated values, but backend values are authoritative.

Do not send:

```text
farmersAhead
estimatedWaitMinutes
grossAmount
transportCost
netAmount
distanceKm
```

as authoritative input.

---

## 15.2 Never send authenticated owner IDs unnecessarily

For authenticated operations, the backend derives identity from JWT.

For example:

```http
POST /api/v1/crops
```

must not require:

```json
{
  "farmerId": 2
}
```

Similarly, creating an offer should not require:

```json
{
  "buyerId": 1
}
```

---

## 15.3 Handle role-based UI

Frontend routing and UI should respect:

```text
FARMER
BUYER
OFFICER
```

The backend remains the final authorization authority.

Hiding a button in the frontend is not a security mechanism.

---

## 15.4 Handle state-dependent actions

Buttons should be enabled/disabled based on backend state.

Example:

```text
PENDING
 ├── Farmer: Accept / Reject / Counter
 └── Buyer: no counter-response action

COUNTERED
 └── Buyer: Accept Counter / Reject Counter

ACCEPTED
 └── Buyer: Create Transaction

OFFER_ACCEPTED
 └── Officer: Assign Transport

TRANSPORT_ASSIGNED
 └── Officer: Schedule Pickup

PICKUP_SCHEDULED
 └── Officer: Mark Delivered

CROP_DELIVERED
 └── Completion flow: not yet implemented
```

---

# 16. Current API Summary

| Module | Method | Endpoint | Role |
|---|---|---|---|
| Auth | POST | `/api/v1/auth/register` | Public |
| Auth | POST | `/api/v1/auth/login` | Public |
| Auth | GET | `/api/v1/auth/me` | Authenticated |
| Auth | PUT | `/api/v1/auth/farmer/me/location` | Farmer |
| Auth | PUT | `/api/v1/auth/buyer/me/location` | Buyer |
| Crops | POST | `/api/v1/crops` | Farmer |
| Crops | GET | `/api/v1/crops/my` | Farmer |
| Crops | GET | `/api/v1/crops/{cropId}` | Authenticated |
| Procurement | GET | `/api/v1/procurement-centres` | Authenticated |
| Procurement | GET | `/api/v1/procurement-centres/{centreId}` | Authenticated |
| Procurement | GET | `/api/v1/procurement-centres/state/{state}` | Authenticated |
| Procurement | GET | `/api/v1/procurement-centres/district/{district}` | Authenticated |
| Procurement | GET | `/api/v1/procurement-centres/state/{state}/district/{district}` | Authenticated |
| Queue | POST | `/api/v1/queues/join` | Farmer |
| Queue | GET | `/api/v1/queues/my` | Farmer |
| Queue | GET | `/api/v1/queues/centre/{centreId}` | Officer |
| Queue | POST | `/api/v1/queues/centre/{centreId}/next` | Officer |
| Queue | POST | `/api/v1/queues/{queueEntryId}/complete` | Officer |
| Queue | POST | `/api/v1/queues/{queueEntryId}/cancel` | Farmer |
| Offers | POST | `/api/v1/offers` | Buyer |
| Offers | GET | `/api/v1/offers/received` | Farmer |
| Offers | GET | `/api/v1/offers/my` | Buyer |
| Offers | POST | `/api/v1/offers/{offerId}/accept` | Farmer |
| Offers | POST | `/api/v1/offers/{offerId}/reject` | Farmer |
| Offers | POST | `/api/v1/offers/{offerId}/counter` | Farmer |
| Offers | POST | `/api/v1/offers/{offerId}/accept-counter` | Buyer |
| Offers | POST | `/api/v1/offers/{offerId}/reject-counter` | Buyer |
| Transactions | POST | `/api/v1/transactions/from-offer/{offerId}` | Buyer |
| Transactions | GET | `/api/v1/transactions/my` | Buyer |
| Transactions | GET | `/api/v1/transactions/my-sales` | Farmer |
| Transactions | POST | `/api/v1/transactions/{transactionId}/assign-transport` | Officer |
| Transactions | POST | `/api/v1/transactions/{transactionId}/schedule-pickup` | Officer |
| Transactions | POST | `/api/v1/transactions/{transactionId}/mark-delivered` | Officer |
| Transport | POST | `/api/v1/transport-options` | Officer |
| Transport | GET | `/api/v1/transport-options/available` | Farmer/Buyer/Officer |
| Transport | GET | `/api/v1/transport-options/{transportOptionId}` | Farmer/Buyer/Officer |

---

# 17. Planned / Not Yet Implemented APIs

The following areas are intentionally **not part of the current implemented API contract**:

## Notifications

Planned functionality:

```text
Notification entity
Notification listing
Read/unread state
Real-time delivery
```

Status:

```text
PLANNED
```

## Recommendation

Planned functionality:

```text
Crop recommendations
Price recommendations
Procurement-centre recommendations
Buyer/farmer recommendations
```

Status:

```text
PLANNED
```

## Transaction Completion

`COMPLETED` exists as a transaction status, but the final completion endpoint has not yet been implemented.

---

# 18. Frontend Development Checklist

Before replacing mock data with API calls:

- [ ] Configure backend base URL.
- [ ] Create a centralized API client.
- [ ] Add JWT Authorization header handling.
- [ ] Implement login.
- [ ] Implement registration.
- [ ] Implement `/auth/me`.
- [ ] Implement role-based route protection.
- [ ] Replace farmer mock crop data.
- [ ] Connect crop registration.
- [ ] Connect procurement centre listing.
- [ ] Connect queue join/status.
- [ ] Connect buyer crop browsing.
- [ ] Connect offer creation.
- [ ] Connect farmer offer actions.
- [ ] Connect buyer counter-offer actions.
- [ ] Connect transaction creation.
- [ ] Connect transaction tracking.
- [ ] Connect officer queue management.
- [ ] Connect transport management.
- [ ] Connect pickup scheduling.
- [ ] Connect delivery status.
- [ ] Remove obsolete mock API calls after verification.

---

# 19. Source-of-Truth Principle

The frontend is a presentation/client layer.

The Spring Boot backend is responsible for:

- Authentication
- Authorization
- Ownership checks
- Queue token assignment
- Queue state transitions
- Offer state transitions
- Transaction creation
- Crop quantity updates
- Transport availability
- Distance calculation
- Transport cost calculation
- Net amount calculation
- Transaction state transitions

Therefore:

```text
Frontend
   │
   │ request
   ▼
Spring Boot API
   │
   ├── validate
   ├── authorize
   ├── calculate
   ├── persist
   └── return authoritative state
   │
   ▼
Frontend
   │
   └── render returned state
```

This is the API contract the frontend should integrate against.
