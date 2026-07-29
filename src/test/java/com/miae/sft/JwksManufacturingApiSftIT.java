package com.miae.sft;

import static org.assertj.core.api.Assertions.assertThat;

import com.miae.MiaeApplication;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Prerequisite: a Keycloak instance running on localhost:8081 with a realm named MIAE and a client configured to issue access tokens for audience "miae-api".
 * JWKS-authenticated replica of the manufacturing API SFT scenario.
 * <p>The bearer token is intentionally supplied at runtime through
 * {@code MIAE_SFT_JWKS_BEARER_TOKEN}; it must be issued by the configured Keycloak realm
 * and contain {@code aud=miae-api}.</p>
 */
@SpringBootTest(classes = MiaeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("sft")
@TestPropertySource(properties = {
        "miae.security.authentication-type=JWKS",
        "miae.security.jwks.uri=http://localhost:8081/realms/MIAE/protocol/openid-connect/certs",
        "miae.security.issuer=http://localhost:8081/realms/MIAE",
        "miae.security.audience=miae-api",
        "miae.sample-data.enabled=false"
})
class JwksManufacturingApiSftIT {

    private static final String TOKEN_ENVIRONMENT_VARIABLE = "MIAE_SFT_JWKS_BEARER_TOKEN";
    private static final String BEARER_TOKEN = System.getenv(TOKEN_ENVIRONMENT_VARIABLE);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE = new ParameterizedTypeReference<>() { };

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeAll
    static void requireBearerToken() {
        if (BEARER_TOKEN == null || BEARER_TOKEN.isBlank()) {
            throw new IllegalStateException(TOKEN_ENVIRONMENT_VARIABLE + " must contain a current Keycloak access token");
        }
    }

    @BeforeEach
    void cleanGraph() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    void jwksBearerTokenAllowsTheManufacturingIngestionAndImpactScenario() {
        loadScenario();

        assertThat(count("MATCH (p:PRODUCT {productId: 'P100'}) RETURN count(p) AS count")).isEqualTo(1);
        assertThat(count("MATCH (r:REVISION) RETURN count(r) AS count")).isEqualTo(2);
        assertThat(count("MATCH (c:COMPONENT) RETURN count(c) AS count")).isEqualTo(3);
        assertThat(count("MATCH (s:SUPPLIER {supplierId: 'SUP-ABC'}) RETURN count(s) AS count")).isEqualTo(1);

        assertImpact("REVISION", "P100-REV-B", "affectedSalesOrders", 1);
        assertImpact("COMPONENT", "PCB-A", "usedByProducts", 1);
        assertImpact("SUPPLIER", "SUP-ABC", "suppliedComponents", 2);
    }

    @Test
    void bearerTokenIsRequiredForApiRequests() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/products"), HttpMethod.OPTIONS,
                new HttpEntity<>("{}", headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private void assertImpact(String entityType, String entityId, String summaryField, int expectedValue) {
        ResponseEntity<Map<String, Object>> response = postMap("/api/v1/impact-analysis", Map.of("entityType", entityType, "entityId", entityId));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body(response)).containsEntry("entityType", entityType);
        assertThat(asMap(body(response).get("summary"))).containsEntry(summaryField, expectedValue);
    }

    private void loadScenario() {
        postAccepted("/api/v1/products", Map.of("productId", "P100", "code", "SENSOR-100", "name", "Industrial Sensor"));
        postAccepted("/api/v1/revisions", Map.of("revisionId", "P100-REV-A", "productId", "P100", "code", "A", "status", "APPROVED"));
        postAccepted("/api/v1/revisions", Map.of("revisionId", "P100-REV-B", "productId", "P100", "code", "B", "status", "APPROVED"));
        postAccepted("/api/v1/boms", Map.of("revisionId", "P100-REV-A", "components", new Object[]{
                Map.of("componentId", "PCB-A", "quantity", 1, "uom", "Pcs"), Map.of("componentId", "SCREW", "quantity", 4, "uom", "Pcs")}));
        postAccepted("/api/v1/boms", Map.of("revisionId", "P100-REV-B", "components", new Object[]{
                Map.of("componentId", "PCB-B", "quantity", 1, "uom", "Pcs"), Map.of("componentId", "SCREW", "quantity", 4, "uom", "Pcs")}));
        postAccepted("/api/v1/suppliers", Map.of("componentId", "PCB-A", "suppliers", new Object[]{
                Map.of("supplierId", "SUP-ABC", "supplierName", "ABC Electronics", "leadTimeDays", 15)}));
        postAccepted("/api/v1/suppliers", Map.of("componentId", "PCB-B", "suppliers", new Object[]{
                Map.of("supplierId", "SUP-ABC", "supplierName", "ABC Electronics", "leadTimeDays", 15)}));
        postAccepted("/api/v1/inventory", Map.of("componentId", "PCB-A", "warehouse", "WH1", "quantity", 500,
                "inventoryId", "INV-PCBA-WH1", "uom", "Pcs"));
        postAccepted("/api/v1/purchase-orders", Map.of("purchaseOrderId", "PO-100", "supplierId", "SUP-ABC", "componentId", "PCB-A",
                "openQuantity", 1000, "uom", "Pcs", "expectedDeliveryDate", "10/02/2026"));
        postAccepted("/api/v1/work-orders", Map.of("workOrderId", "WO-1001", "revisionId", "P100-REV-B", "status", "RELEASED",
                "remainingQty", 50, "priority", "HIGH", "plannedCompletionDate", "20/02/2026", "uom", "Box", "materialAvailabilityStatus", "READY"));
        postAccepted("/api/v1/sales-orders", Map.of("salesOrderId", "SO-100", "customerId", "CUST-100", "customerName", "Acme Corp",
                "productId", "P100", "openQuantity", 25, "orderValue", 50000, "priority", "CRITICAL", "currency", "USD"));
    }

    private void postAccepted(String path, Object body) {
        assertThat(postMap(path, body).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    private ResponseEntity<Map<String, Object>> postMap(String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(BEARER_TOKEN);
        return restTemplate.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, headers), MAP_RESPONSE);
    }

    private String url(String path) { return "http://localhost:" + port + path; }
    private Long count(String cypher) {
        return neo4jClient.query(cypher).fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("count").asLong()).one().orElse(0L);
    }
    private Map<String, Object> body(ResponseEntity<Map<String, Object>> response) { return Objects.requireNonNull(response.getBody()); }
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) { return (Map<String, Object>) value; }
}
