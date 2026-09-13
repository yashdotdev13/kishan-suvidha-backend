# Authentication & Authorization

## 1. Overview

The Kisan Suvidha backend uses Spring Security with JWT-based authentication and role-based access control (RBAC).

The authentication module is responsible for:

- User registration
- User login
- JWT generation and validation
- Authentication of protected API requests
- Role-based authorization
- Current-user (`/me`) access
- Farmer, buyer, and officer profiles
- Farmer and buyer location updates
- Geocoding of profile locations into latitude/longitude coordinates

The backend follows a layered architecture:

```text
auth/
├── controller/
├── dto/
├── entity/
├── repository/
├── security/
└── service/
```

---

## 2. User Roles

The system currently defines three roles:

```text
FARMER
BUYER
OFFICER
```

Roles are used throughout the application to control access to protected operations.

Examples:

| Role | Main Responsibilities |
|---|---|
| FARMER | Manage crops, join procurement queues, manage received offers |
| BUYER | Create offers, manage own offers, create transactions |
| OFFICER | Manage operational workflows such as queues and transport |

Authorization is enforced using Spring Security method-level authorization, for example:

```java
@PreAuthorize("hasRole('FARMER')")
```

and:

```java
@PreAuthorize("hasRole('OFFICER')")
```

---

## 3. User Entity

The central authentication entity is `User`.

Conceptually, a user contains:

```text
User
├── id
├── email
├── phone
├── password
├── role
├── enabled
├── createdAt
└── updatedAt
```

Important constraints:

- Email is unique.
- Phone is unique.
- Password is stored as a password hash rather than plain text.
- Role determines the user's authorization level.
- `enabled` controls whether the account can authenticate.

The role is represented by the enum:

```java
public enum Role {
    FARMER,
    BUYER,
    OFFICER
}
```

---

## 4. Profile Entities

The authentication module separates the authentication identity (`User`) from role-specific profile information.

### Farmer

```text
User
  │
  └── Farmer
```

The farmer profile contains information such as:

```text
id
user
name
mobile
state
district
village
location
latitude
longitude
createdAt
updatedAt
```

### Buyer

```text
User
  │
  └── Buyer
```

The buyer profile contains:

```text
id
user
name
companyName
location
latitude
longitude
createdAt
updatedAt
```

The relationship is one-to-one:

```java
@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "user_id", nullable = false, unique = true)
private User user;
```

This keeps authentication data separate from domain-specific profile data.

---

## 5. JWT Authentication Flow

The authentication flow is:

```text
Client
  │
  │ POST /api/v1/auth/login
  ▼
AuthController
  │
  ▼
Authentication Service
  │
  ▼
UserRepository
  │
  ▼
Password Verification
  │
  ▼
JwtService
  │
  ▼
JWT Access Token
  │
  ▼
Client
```

For protected requests:

```text
Client
  │
  │ Authorization: Bearer <JWT>
  ▼
JwtAuthenticationFilter
  │
  ├── Extract JWT
  ├── Validate JWT
  ├── Extract user ID
  ├── Load User
  ├── Check enabled status
  └── Set Authentication
          │
          ▼
      Controller
          │
          ▼
   @PreAuthorize(...)
```

The JWT filter loads the user from `UserRepository` and places the authenticated user ID in the Spring Security context.

The authenticated principal is currently the user's `Long` ID.

---

## 6. Security Configuration

The application uses:

```text
Spring Security
JWT
Method-level authorization
```

Method security is enabled using:

```java
@EnableMethodSecurity
```

Public endpoints include authentication endpoints such as:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
```

The actuator health endpoint is also permitted.

Other application endpoints require authentication unless explicitly configured otherwise.

---

## 7. Registration

Registration is exposed through:

```http
POST /api/v1/auth/register
```

The client supplies the registration information required by the current authentication DTO.

The backend is responsible for:

1. Validating the request.
2. Checking email uniqueness.
3. Checking phone uniqueness.
4. Creating the `User`.
5. Assigning the requested role.
6. Creating the corresponding role-specific profile.
7. Persisting the data.

The farmer, buyer, and officer roles determine which profile is created.

The backend must remain authoritative for identity and role information.

---

## 8. Login

Login is exposed through:

```http
POST /api/v1/auth/login
```

The client provides its login credentials.

The backend:

1. Finds the user.
2. Verifies the password.
3. Checks that the account is enabled.
4. Generates a JWT.
5. Returns the authentication response containing the access token.

The client must send the JWT on subsequent protected requests:

```http
Authorization: Bearer <access-token>
```

---

## 9. Current User

The authentication module provides:

```http
GET /api/v1/auth/me
```

The authenticated user is determined from the JWT rather than from a user ID supplied by the frontend.

This is important because clients should not be able to impersonate another user simply by changing a request parameter.

---

## 10. Farmer Location

Farmers can update their location through:

```http
PUT /api/v1/auth/farmer/me/location
```

Request:

```json
{
  "location": "Patna, Bihar, India"
}
```

The backend uses the configured geocoding service to convert the supplied location into coordinates.

The resulting profile data contains:

```text
location
latitude
longitude
```

The farmer's location is later used by logistics calculations.

---

## 11. Buyer Location

Buyers can update their location through:

```http
PUT /api/v1/auth/buyer/me/location
```

Request:

```json
{
  "location": "Gaya, Bihar, India"
}
```

The backend geocodes the supplied location and stores:

```text
location
latitude
longitude
```

Buyer coordinates are later used by the logistics module for distance calculation.

---

## 12. Geocoding

Location updates reuse the backend's existing geocoding abstraction:

```text
GeocodingService
        │
        ▼
NominatimGeocodingService
        │
        ▼
OpenStreetMap Nominatim
```

The service converts a human-readable location into geographic coordinates.

The authentication module does not directly implement the geocoding algorithm; it delegates this responsibility to the procurement/geocoding service.

---

## 13. Repository Layer

The authentication module uses Spring Data JPA repositories.

The `UserRepository` provides operations such as:

```java
findByEmail(...)
findByPhone(...)
existsByEmail(...)
existsByPhone(...)
```

These methods are used for authentication and registration validation.

Role-specific repositories provide access to:

```text
Farmer
Buyer
Officer
```

profile records.

---

## 14. Authorization Rules

Authorization is enforced at the API layer using role checks.

Examples:

### Farmer-only

```java
@PreAuthorize("hasRole('FARMER')")
```

Used for farmer-specific operations such as crop management.

### Buyer-only

```java
@PreAuthorize("hasRole('BUYER')")
```

Used for buyer-specific operations such as creating offers.

### Officer-only

```java
@PreAuthorize("hasRole('OFFICER')")
```

Used for operational management such as transport assignment.

### Multiple roles

Where an operation is intentionally shared:

```java
@PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'OFFICER')")
```

The exact authorization rule should always match the business operation.

---

## 15. Error Handling

Authentication and authorization errors are handled by the application's global exception handling.

Important error types include:

```text
UnauthorizedException
ForbiddenException
BadRequestException
ConflictException
ResourceNotFoundException
```

Typical HTTP semantics:

| Situation | HTTP Status |
|---|---:|
| Missing/invalid authentication | 401 |
| Authenticated but insufficient permissions | 403 |
| Invalid request | 400 |
| Resource conflict | 409 |
| Resource not found | 404 |

The API returns a consistent error response through `GlobalExceptionHandler`.

---

## 16. Security Principles

The authentication module follows these principles:

### 16.1 Do not trust user IDs from the frontend

For user-owned resources, the backend derives the authenticated user from the JWT.

Instead of:

```text
POST /crops
{
  "farmerId": 123
}
```

the backend uses the authenticated principal.

### 16.2 Do not expose passwords

Passwords must never be returned in API responses.

### 16.3 Enforce authorization on the backend

Frontend role checks are only for user experience.

Actual authorization is enforced by Spring Security.

### 16.4 Keep JWT handling centralized

JWT parsing and authentication are handled by:

```text
JwtService
JwtAuthenticationFilter
SecurityConfig
```

rather than being duplicated inside every controller.

---

## 17. Frontend Integration

The frontend should maintain the JWT after login and send it with protected requests.

Typical flow:

```text
Login
  ↓
Receive JWT
  ↓
Store access token
  ↓
API request
  ↓
Authorization: Bearer <JWT>
  ↓
Spring Security
  ↓
Controller
```

The frontend should not send:

```text
farmerId
buyerId
userId
```

to identify the current authenticated user when the backend can derive that information from the JWT.

For example:

```http
GET /api/v1/crops/my
Authorization: Bearer <JWT>
```

The backend determines which farmer owns the request.

---

## 18. Authentication API Summary

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/auth/me` | Authenticated |
| PUT | `/api/v1/auth/farmer/me/location` | FARMER |
| PUT | `/api/v1/auth/buyer/me/location` | BUYER |

---

## 19. Authentication Module Flow

Complete authentication flow:

```text
                  ┌──────────────┐
                  │    Client    │
                  └──────┬───────┘
                         │
                 Register / Login
                         │
                         ▼
                ┌─────────────────┐
                │  AuthController │
                └────────┬────────┘
                         │
                         ▼
                ┌─────────────────┐
                │ Auth Service     │
                └────────┬────────┘
                         │
                         ▼
                ┌─────────────────┐
                │ UserRepository  │
                └────────┬────────┘
                         │
                         ▼
                  User + Profile
                         │
                         ▼
                    JwtService
                         │
                         ▼
                       JWT
                         │
                         ▼
                      Client
                         │
                 Bearer JWT
                         │
                         ▼
             JwtAuthenticationFilter
                         │
                         ▼
                 SecurityContext
                         │
                         ▼
                Protected Controller
                         │
                         ▼
                   @PreAuthorize
```

---

## 20. Current Status

Authentication and authorization are implemented and form the security foundation for the remaining modules.

Implemented:

- User registration
- User login
- JWT authentication
- JWT request filtering
- Role-based authorization
- Farmer profile
- Buyer profile
- Officer role
- Current-user endpoint
- Farmer location update
- Buyer location update
- Location geocoding
- Global authentication/authorization error handling

The authentication module is used by the agriculture, procurement, marketplace, transaction, and logistics modules.
