# Manufacturing Impact Analysis Engine

Manufacturing Impact Analysis Engine (MIAE) is a Spring Boot + Neo4j MVP that builds a manufacturing knowledge graph from ERP-style events and exposes deterministic impact analysis APIs for revisions, components, and suppliers. The Manufacturing Impact Analysis Platform provides instant visibility into upstream and downstream dependencies using a graph-based model and a natural language copilot.

Detailed requirements, architecture, and design documentation for the Manufacturing Impact Analysis Engine (MIAE) can be found within this repository.

## Problem
Today, impact analysis is often a manual process involving multiple teams and systems. Manufacturing organizations struggle to understand the downstream impact of engineering changes, since there are multiple disconnected business systems like ERP, PLM, Inventory, Supplier etc. Without timely impact visibility, production planning slows, procurement cannot respond proactively, customer commitments become uncertain, and overall business risk increases. If a component becomes unavailable or a revision changes, teams need to quickly understand affected products, work orders, suppliers, inventory, and customer orders.

## Stack

- Java 21
- Spring Boot 3.x
- Spring Data Neo4j
- Neo4j 5.x
- Maven
- OpenAPI / Swagger UI
- Docker Compose
- JUnit 5 / Mockito

## Run Locally

The app defaults match the local Neo4j instance provided for this workspace:

- URI: `neo4j://127.0.0.1:7687`
- Username: `neo4j`
- Password: `password`

```bash
mvn test
mvn spring-boot:run
```

Open:

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

All `/api/**` requests require authentication. The default developer configuration uses:

```text
X-API-Key: dev-api-key
```

Override configuration with environment variables:

```bash
NEO4J_URI=neo4j://127.0.0.1:7687
NEO4J_USERNAME=neo4j
NEO4J_PASSWORD=password
MIAE_SECURITY_AUTHENTICATION_TYPE=DEVELOPER_API_KEY
MIAE_SECURITY_API_KEY_HEADER_NAME=X-API-Key
MIAE_SECURITY_API_KEY_VALUE=dev-api-key
MIAE_SAMPLE_DATA_ENABLED=false
```

## Run With Docker Compose

```bash
mvn -DskipTests package
docker compose up --build
```

## JWT and JWKS authentication

Authentication is selected only through configuration; no API or business-code changes are required. API-key mode remains the default for local development. In both JWT modes, MIAE validates the signature and expiry, plus issuer and audience when configured. Invalid or expired credentials return HTTP 401.

To configure a mode directly in `src/main/resources/application.yml`, replace the `miae.security` block with one of the following. Environment variables shown below can still override these values in a container or deployment.

Developer API key (default):

```yaml
miae:
  security:
    authentication-type: DEVELOPER_API_KEY
    api-key:
      header-name: X-API-Key
      value: dev-api-key
```

JWT with an RSA public key. `location` supports either `file:` or `classpath:` resources:

```yaml
miae:
  security:
    authentication-type: JWT_PUBLIC_KEY
    public-key:
      location: file:/opt/miae/security/public-key.pem
    issuer: ERPNext       # optional
    audience: miae-api    # optional
    clock-skew: 60s       # optional; defaults to 60s
```

JWT with a JWKS endpoint. The decoder resolves signing keys by `kid` and refreshes its JWKS cache when a new key is encountered:

```yaml
miae:
  security:
    authentication-type: JWKS
    jwks:
      uri: https://erp.company.com/.well-known/jwks.json
    issuer: ERPNext       # optional
    audience: miae-api    # optional
    clock-skew: 60s       # optional; defaults to 60s
```

For a PEM-encoded RSA public key available to the application:

```bash
MIAE_SECURITY_AUTHENTICATION_TYPE=JWT_PUBLIC_KEY
MIAE_SECURITY_PUBLIC_KEY_LOCATION=file:/opt/miae/security/public-key.pem
MIAE_SECURITY_ISSUER=ERPVendor
MIAE_SECURITY_AUDIENCE=miae-api
MIAE_SECURITY_CLOCK_SKEW=60s
```

For an ERP or identity provider JWKS endpoint (including key rotation):

```bash
MIAE_SECURITY_AUTHENTICATION_TYPE=JWKS
MIAE_SECURITY_JWKS_URI=https://erp.company.com/.well-known/jwks.json
MIAE_SECURITY_ISSUER=ERPVendor
MIAE_SECURITY_AUDIENCE=miae-api
MIAE_SECURITY_CLOCK_SKEW=60s
```

JWT modes expect `Authorization: Bearer <JWT>`. The Copilot page provides an authentication selector for either API key or Bearer token; API key remains its default.

Compose starts:

- `impact-engine` on port `8080`
- `neo4j` browser on port `7474`
- `neo4j` Bolt on port `7687`

## Sample Data Loader

Set `MIAE_SAMPLE_DATA_ENABLED=true` to load a small graph at startup:

- Product `P100`
- Revisions `P100-REV-A`, `P100-REV-B`
- Components `PCB-A`, `PCB-B`, `SCREW`
- Supplier `SUP-ABC`
- Inventory, purchase order, work order, sales order, and customer records

## Implemented APIs

Ingestion:

- `POST /api/v1/products`
- `POST /api/v1/revisions`
- `POST /api/v1/boms`
- `POST /api/v1/suppliers`
- `POST /api/v1/inventory`
- `POST /api/v1/purchase-orders`
- `POST /api/v1/work-orders`
- `POST /api/v1/sales-orders`

Impact analysis:

- `POST /api/v1/impact-analysis`
- `POST /api/v1/chat`

Supported impact entity types:

- `REVISION`
- `COMPONENT`
- `SUPPLIER`

## Project Layout

```text
src/main/java/com/miae
  api            REST controllers and DTOs
  service        Projection and application services
  copilot        GenAI assistant for natural query and responses
  graph          Neo4j nodes and repository queries
  analysis       Impact strategies and traversal logic
  validation     Enums and validation types
  config         Neo4j schema, OpenAPI, API key, sample data
  exception      Global error handling
```

## Documentation

- Sample requests: `docs/sample-requests.http`
- MIAE Architecture document: `docs/Manufacturing Impact Analysis Engine.docx`
- MIAE Copilot Architecture document: `docs/Manufacturing Impact Copilot.docx`
- Future MIAE features:  `docs/future-features`
- Neo4j schema script: `src/main/resources/neo4j/schema.cypher`
- Neo4j sample graph script: `src/main/resources/neo4j/sample-data.cypher`

## 🎥 Manufacturing Impact Analysis Engine (MIAE) Video Series

Follow the complete MIAE journey—from architecture and design to implementation, demonstrations, and future enhancements.

▶️ Architecture Walkthrough: https://www.youtube.com/watch?v=_PPflFTBubQ

▶️ Product Demo: https://www.youtube.com/watch?v=Y2BDPiLVJN0

These video focuses on::
- Overview of MIAE
- System Architecture
- ERPNext Integration
- Product Demonstration
- Manufacturing Impact Analysis Copilot
- Future Roadmap
  
## Verification

```bash
mvn test
mvn pmd:check
```

The fast test suite covers service dispatch and projection delegation.
PMD checks static-analysis rules and fails the build when violations are found.

Run the SFT suite against a running Neo4j instance:

```bash
mvn -Psft verify
```

The SFT suite starts the Spring Boot app on a random port, calls ingestion APIs with `X-API-Key: dev-api-key`, verifies the Neo4j graph, then calls revision, component, and supplier impact-analysis APIs.

## Copilot

Manufacturing Impact Copilot exposes a natural-language API over the Impact Engine:

```http
POST /api/v1/chat
X-API-Key: dev-api-key
Content-Type: application/json

{
  "sessionId": "pm-session-1",
  "message": "What happens if Supplier SUP-ABC fails?"
}
```
Access the copilot UI:
```http
http://localhost:8080/copilot.html
```

Configure OpenAI through environment variables. Do not hardcode API keys.

```bash
COPILOT_ENABLED=true
OPENAI_API_KEY=...
OPENAI_MODEL=gpt-5.2
IMPACT_ENGINE_BASE_URL=http://localhost:8080
IMPACT_ENGINE_API_KEY=dev-api-key
```

Run the Copilot print SFT when `OPENAI_API_KEY` is available:

```bash
mvn -Psft -Dit.test=CopilotPrintSftIT verify
```

On PowerShell, quote Maven `-D` properties:

```powershell
mvn "-Psft" "-Dit.test=CopilotPrintSftIT" verify
```

The print SFT calls `/api/v1/chat` and prints the generated responses. It intentionally does not assert response wording.
