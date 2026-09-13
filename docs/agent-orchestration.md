# Kisan Suvidha --- Agentic AI Orchestration & Integration

## 1. Overview

Kisan Suvidha uses Gemini through Spring AI to provide an agentic
agricultural assistant.

The AI layer is a **tool-using orchestration layer**, not a second
business-logic layer. Gemini interprets the farmer's request, decides
which registered tools are required, invokes those tools, receives real
backend data, and reasons over the results.

Core principle:

> Business services remain the source of truth. AI tools delegate to
> business services and must not invent or duplicate business data or
> calculations.

------------------------------------------------------------------------

## 2. Current Architecture

``` text
                         FARMER
                           |
                           v
                  POST /api/v1/ai/chat
                           |
                           v
                  +----------------+
                  |  AiController  |
                  +-------+--------+
                          |
                          v
                  +----------------+
                  | KisanAiService |
                  +-------+--------+
                          |
                          v
                  +----------------+
                  |   ChatClient   |
                  |   Spring AI    |
                  +-------+--------+
                          |
                          v
                     Gemini Model
                          |
                  Dynamic tool calling
                          |
        +-----------------+------------------+
        |                 |                  |
        v                 v                  v
     CropTools      MarketplaceTools   ReferencePriceTools
        |                 |                  |
        v                 v                  v
   CropService        OfferService     ReferencePriceService

        +-----------------+------------------+
        |                 |                  |
        v                 v                  v
  ProcurementTools   QueueTools       LogisticsTools
        |                 |                  |
        v                 v                  v
ProcurementCentre   QueueService      TransportCostService
Service
```

The AI does not access repositories directly.

------------------------------------------------------------------------

## 3. Technology Stack

-   Java 21
-   Spring Boot 4.1.1
-   Spring AI 2.0.1
-   Google Gemini
-   Spring Data JPA / Hibernate
-   PostgreSQL
-   Spring Security
-   JWT authentication
-   Lombok
-   Haversine distance calculation for transport estimates

------------------------------------------------------------------------

## 4. Gemini Configuration

The Gemini API key is supplied through an environment variable and must
never be committed.

``` text
GEMINI_API_KEY=<secret>
```

Spring configuration:

``` properties
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.model=gemini-3.6-flash
```

Maven:

``` xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>2.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

``` xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
</dependency>
```

------------------------------------------------------------------------

## 5. AI Package Structure

``` text
ai/
├── controller/
│   └── AiController.java
├── dto/
│   ├── AiChatRequest.java
│   └── AiChatResponse.java
├── service/
│   └── KisanAiService.java
├── tools/
│   ├── CropTools.java
│   ├── ProcurementTools.java
│   ├── QueueTools.java
│   ├── MarketplaceTools.java
│   ├── LogisticsTools.java
│   └── ReferencePriceTools.java
└── config/
    └── AiConfig.java
```

------------------------------------------------------------------------

## 6. Authentication and ToolContext

The AI endpoint remains authenticated.

The authenticated user ID is obtained from Spring Security:

``` java
Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();

Long userId = (Long) authentication.getPrincipal();
```

It is passed to Spring AI using `ToolContext`:

``` java
return chatClient.prompt()
        .user(message)
        .toolContext(Map.of("userId", userId))
        .call()
        .content();
```

Tools that access personal farmer data retrieve the ID from
`ToolContext`.

The frontend and model must never be trusted to supply an arbitrary
farmer ID.

------------------------------------------------------------------------

## 7. Tool Architecture

AI tools are thin adapters around existing domain services:

``` text
AI Tool
   |
   v
Business Service
   |
   v
Repository
   |
   v
PostgreSQL
```

Avoid direct AI-tool-to-repository access and avoid duplicating business
calculations in the AI layer.

------------------------------------------------------------------------

## 8. Current AI Tools

### CropTools

Retrieves crops belonging to the authenticated farmer through
`CropService`.

The AI must not invent crop names, quantities, status, expected prices,
or harvest dates.

### MarketplaceTools

Retrieves marketplace offers received by the authenticated farmer
through `OfferService`.

Returned data includes offer ID, crop, buyer, quantity, price, counter
price, status, and timestamps.

The advisor does not accept, reject, or counter offers.

### ProcurementTools

Retrieves procurement-centre information through
`ProcurementCentreService`.

### QueueTools

Retrieves the authenticated farmer's active queue information through
`QueueService`.

Queue positions and waiting times are calculated by the business service
and must not be independently invented or recalculated by the AI.

### LogisticsTools

Provides:

-   available transport options
-   procurement-centre transport-cost estimation
-   marketplace-buyer transport-cost estimation

### ReferencePriceTools

Retrieves the active reference price for a crop and state through
`ReferencePriceService`.

Reference prices are treated as reference data and must not
automatically be described as official MSP without supporting data.

------------------------------------------------------------------------

## 9. Transport Cost Estimation

The backend calculates transport cost using the existing distance
service.

Conceptually:

``` text
distanceKm
    *
ratePerKmPerQuintal
    *
quantityQuintals
    =
estimatedTransportCost
```

The AI uses returned values rather than inventing distance or rates.

### Procurement route

``` text
Farmer -> Procurement Centre
```

### Marketplace route

``` text
Farmer -> Buyer
```

Both use the same underlying distance/cost calculation approach.

------------------------------------------------------------------------

## 10. Generic Transport Response

The response was generalized so one DTO can represent both route types:

``` java
public record TransportCostEstimateResponse(
        TransportRouteType routeType,
        Long destinationId,
        Long transportOptionId,
        BigDecimal quantityQuintals,
        BigDecimal distanceKm,
        BigDecimal ratePerKmPerQuintal,
        BigDecimal estimatedTransportCost
) {
}
```

Route types:

``` java
public enum TransportRouteType {
    PROCUREMENT_CENTRE,
    MARKETPLACE_BUYER
}
```

This avoids using a procurement-centre field with `null` for marketplace
calculations.

------------------------------------------------------------------------

## 11. Reference Price Module

Reference pricing was implemented under the procurement domain:

``` text
procurement/
├── controller/
│   └── ReferencePriceController.java
├── dtos/
│   ├── CreateReferencePriceRequest.java
│   └── ReferencePriceResponse.java
├── entity/
│   └── ReferencePrice.java
├── repository/
│   └── ReferencePriceRepository.java
└── service/
    └── ReferencePriceService.java
```

AI adapter:

``` text
ai/
└── tools/
    └── ReferencePriceTools.java
```

Current reference-price model:

``` text
ReferencePrice
├── id
├── cropName
├── cropType
├── pricePerQuintal
├── state
├── effectiveFrom
├── effectiveTo
├── createdAt
└── updatedAt
```

An active price follows:

``` text
effectiveFrom <= today
AND
(
    effectiveTo >= today
    OR
    effectiveTo IS NULL
)
```

------------------------------------------------------------------------

## 12. Selling Advisor

The current agent can handle decision-oriented requests such as:

``` text
I have wheat to sell. Check my available crops and marketplace
offers. For my wheat, compare the best marketplace offer with the
current reference price and consider transport costs to both the
buyer and the procurement centre. Also consider my procurement queue
if relevant. Based only on the available data, tell me which option
appears financially better and explain why.
```

The intended orchestration is:

``` text
                         Farmer Question
                                |
                                v
                         Gemini Agent
                                |
              +-----------------+-----------------+
              |                 |                 |
              v                 v                 v
          My Crops          My Offers       Reference Price
              |                 |                 |
              +-----------------+-----------------+
                                |
                    +-----------+-----------+
                    |                       |
                    v                       v
          Procurement Transport      Marketplace Transport
                    |                       |
                    +-----------+-----------+
                                |
                                v
                         Queue Information
                                |
                                v
                         Agent Reasoning
                                |
                                v
                       Selling Recommendation
```

Gemini decides which tools are necessary for each request; it does not
have to call every tool every time.

------------------------------------------------------------------------

## 13. Financial Comparison

For a marketplace offer:

``` text
marketplaceGross
    =
offerPricePerQuintal × offerQuantity
```

If transport data is available:

``` text
marketplaceNet
    =
marketplaceGross - marketplaceTransportCost
```

For procurement:

``` text
procurementGross
    =
referencePricePerQuintal × quantity
```

If transport data is available:

``` text
procurementNet
    =
procurementGross - procurementTransportCost
```

The AI should only make a net comparison when the required data exists.

Missing information must be stated explicitly instead of guessed.

------------------------------------------------------------------------

## 14. Read-Only Agent Boundary

The current advisor is intentionally read-only.

It must not:

``` text
- Accept an offer
- Reject an offer
- Counter an offer
- Create a transaction
- Assign transport
- Schedule pickup
- Mark crop delivered
```

The agent can recommend an action, but the farmer must explicitly
perform the action through normal application APIs.

``` text
AI Agent
   |
   | READ
   v
Business Services
   |
   v
Recommendation
   |
   v
Farmer decision
   |
   | explicit action
   v
Normal application API
```

------------------------------------------------------------------------

## 15. Security Rules

1.  AI endpoints remain authenticated.
2.  Never accept `userId` from the AI request body for personal-data
    tools.
3.  Obtain the authenticated user ID from Spring Security.
4.  Pass it through `ToolContext`.
5.  Verify ownership inside business services/tools where appropriate.
6.  Never expose another farmer's crops or offers.
7.  Never expose Gemini API secrets to the model.
8.  Never commit `GEMINI_API_KEY`.
9.  Keep state-changing operations outside the read-only advisor tool
    set.

------------------------------------------------------------------------

## 16. Error-Handling Lesson

During development, a transport estimate failed because the selected
transport option was unavailable.

The correct business rule was preserved:

``` text
Transport option is not currently available
```

The problem was not solved by weakening availability validation.

A provider/tool-result parsing issue can occur when an expected business
exception becomes plain text and the model provider expects a structured
tool result.

The architectural principle is:

> Preserve domain validation. If necessary, represent expected AI-facing
> tool errors as structured serializable results rather than weakening
> business rules.

------------------------------------------------------------------------

## 17. Agentic vs Traditional Chatbot

Traditional chatbot:

``` text
User
 |
 v
LLM
 |
 v
Text response
```

Kisan Suvidha:

``` text
User
 |
 v
LLM Agent
 |
 +--> crops
 +--> offers
 +--> reference price
 +--> procurement
 +--> transport
 +--> queue
 |
 v
Reason over real application state
 |
 v
Recommendation
```

The model therefore acts as an orchestrator over real application
capabilities.

------------------------------------------------------------------------

## 18. Validation Status

The following capabilities have been successfully tested:

``` text
Basic Gemini chat                                  PASS
Authenticated AI endpoint                         PASS
Crop retrieval                                    PASS
Procurement-centre retrieval                      PASS
Farmer queue retrieval                            PASS
Marketplace offer retrieval                       PASS
Available transport retrieval                     PASS
Procurement transport-cost estimation             PASS
Reference-price retrieval                          PASS
Marketplace transport-cost estimation             PASS
Generic transport-cost response                   PASS
Multi-tool selling-advisor orchestration          PASS
```

------------------------------------------------------------------------

## 19. Current Milestone

> **Gemini-powered agentic orchestration over authenticated, read-only
> Kisan Suvidha business capabilities is working end-to-end.**

The AI layer can retrieve real application state through tools and use
that information to produce a selling recommendation.

------------------------------------------------------------------------

## 20. Next Engineering Milestone

The next recommended improvement is to harden the advisor output into a
structured recommendation that the frontend can render.

Potential fields:

``` text
decision
confidence
crop
quantity
marketplaceOfferPrice
referencePrice
marketplaceTransportCost
procurementTransportCost
marketplaceNet
procurementNet
reason
```

This can eventually become a dedicated recommendation card in the Kisan
Suvidha frontend.

Future enhancements can include:

-   specialized logistics or procurement agents
-   verified agricultural RAG
-   government scheme information
-   structured recommendations
-   controlled conversational memory
-   additional read-only intelligence tools
