package com.miae.sft;

import static org.assertj.core.api.Assertions.assertThat;

import com.miae.MiaeApplication;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

/*
 * Integration tests for the MIAE API(non copilot) in the SFT profile. These tests cover end-to-end scenarios including:
 * - Pre-Requirite: Ensure Neo4j is running and accessible at the configured URI before executing these tests.
 * - Ingesting manufacturing data through the API and verifying the resulting graph structure in Neo4j.
 * - Performing impact analysis on revisions, components, and suppliers and validating the response structure and content.
 * - Ensuring that API key authentication is enforced for all endpoints.
 */
@SpringBootTest(classes = MiaeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("sft")
@TestPropertySource(properties = {
        "miae.security.enabled=true",
        "miae.security.api-key=dev-api-key",
        "miae.sample-data.enabled=false",
        "management.endpoint.health.show-details=always"
})
class ManufacturingApiSftIT {

    private static final String API_KEY = "dev-api-key";
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE =
            new ParameterizedTypeReference<>() {
            };

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void cleanGraph() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    void ingestionApisCreateManufacturingGraph() {
        loadScenario();

        Long productCount = count("MATCH (p:PRODUCT {productId: 'P100'}) RETURN count(p) AS count");
        Long revisionCount = count("MATCH (r:REVISION) RETURN count(r) AS count");
        Long componentCount = count("MATCH (c:COMPONENT) RETURN count(c) AS count");
        Long supplierCount = count("MATCH (s:SUPPLIER {supplierId: 'SUP-ABC'}) RETURN count(s) AS count");
        Long relationshipCount = count("""
                MATCH (:PRODUCT)-[:HAS_REVISION]->(:REVISION),
                      (:REVISION)-[:USES_COMPONENT]->(:COMPONENT),
                      (:COMPONENT)-[:SUPPLIED_BY]->(:SUPPLIER),
                      (:INVENTORY)-[:STOCKS]->(:COMPONENT),
                      (:PURCHASE_ORDER)-[:PURCHASES]->(:COMPONENT),
                      (:WORK_ORDER)-[:BUILDS]->(:REVISION),
                      (:SALES_ORDER)-[:ORDERS]->(:PRODUCT),
                      (:CUSTOMER)-[:PLACED]->(:SALES_ORDER)
                RETURN count(*) AS count
                """);

        assertThat(productCount).isEqualTo(1);
        assertThat(revisionCount).isEqualTo(2);
        assertThat(componentCount).isEqualTo(3);
        assertThat(supplierCount).isEqualTo(1);
        assertThat(relationshipCount).isPositive();
    }

    @Test
    void impactAnalysisApisReturnRevisionComponentAndSupplierImpacts() {
        loadScenario();

        ResponseEntity<Map<String, Object>> revision = postMap("/api/v1/impact-analysis", Map.of(
                "entityType", "REVISION",
                "entityId", "P100-REV-B"
        ));
        assertThat(revision.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(revision.getBody()).containsEntry("entityType", "REVISION");
        assertThat(map(revision, "summary")).containsEntry("affectedSalesOrders", 1);
        assertThat(map(revision, "summary")).containsEntry("affectedCustomers", 1);
        assertThat(map(revision, "changeSummary")).containsKey("removedComponents");
        assertThat(map(revision, "lifecycleImpact")).containsEntry("canRetirePreviousRevision", false);

        ResponseEntity<Map<String, Object>> component = postMap("/api/v1/impact-analysis", Map.of(
                "entityType", "COMPONENT",
                "entityId", "PCB-A"
        ));
        assertThat(component.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(component.getBody()).containsEntry("entityType", "COMPONENT");
        assertThat(map(component, "summary")).containsEntry("usedByProducts", 1);
        assertThat(map(component, "summary")).containsEntry("affectedPurchaseOrders", 1);
        assertThat(firstMap(component, "inventory")).containsEntry("uom", "Pcs");
        assertThat(firstMap(component, "purchaseOrders")).containsEntry("uom", "Pcs");
        assertThat(firstMap(component, "purchaseOrders")).containsEntry("expectedDeliveryDate", "2026-02-10");

        ResponseEntity<Map<String, Object>> workOrderComponent = postMap("/api/v1/impact-analysis", Map.of(
                "entityType", "COMPONENT",
                "entityId", "PCB-B"
        ));
        assertThat(workOrderComponent.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstMap(workOrderComponent, "workOrders")).containsEntry("uom", "Box");
        assertThat(firstMap(workOrderComponent, "workOrders")).containsEntry("materialAvailabilityStatus", "READY");

        ResponseEntity<Map<String, Object>> supplier = postMap("/api/v1/impact-analysis", Map.of(
                "entityType", "SUPPLIER",
                "entityId", "SUP-ABC"
        ));
        assertThat(supplier.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(supplier.getBody()).containsEntry("entityType", "SUPPLIER");
        assertThat(map(supplier, "summary")).containsEntry("suppliedComponents", 2);
        assertThat(map(supplier, "summary")).containsEntry("affectedCustomers", 1);
        assertThat(map(supplier, "summary")).containsEntry("revenueAtRisk", 50000.0);
        assertThat(map(supplier, "summary")).containsEntry("revenueCurrency", "USD");
        assertThat(firstMap(supplier, "components")).containsEntry("uom", "Pcs");
        assertThat(firstMap(supplier, "affectedSalesOrders")).containsEntry("currency", "USD");
        assertThat(firstMap(supplier, "affectedWorkOrders")).containsEntry("uom", "Box");
        assertThat(firstMap(supplier, "affectedWorkOrders")).containsEntry("materialAvailabilityStatus", "READY");
    }

    @Test
    void apiKeyIsRequiredForApiRequests() {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/v1/products"),
                HttpMethod.OPTIONS,
                new HttpEntity<>("""
                        {"productId":"P100","code":"SENSOR-100","name":"Industrial Sensor"}
                        """, jsonHeadersWithoutApiKey()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private void loadScenario() {
        postAccepted("/api/v1/products", Map.of(
                "productId", "P100",
                "code", "SENSOR-100",
                "name", "Industrial Sensor"
        ));
        postAccepted("/api/v1/revisions", Map.of(
                "revisionId", "P100-REV-A",
                "productId", "P100",
                "code", "A",
                "status", "APPROVED"
        ));
        postAccepted("/api/v1/revisions", Map.of(
                "revisionId", "P100-REV-B",
                "productId", "P100",
                "code", "B",
                "status", "APPROVED"
        ));
        postAccepted("/api/v1/boms", Map.of(
                "revisionId", "P100-REV-A",
                "components", new Object[]{
                        Map.of("componentId", "PCB-A", "quantity", 1, "uom", "Pcs"),
                        Map.of("componentId", "SCREW", "quantity", 4, "uom", "Pcs")
                }
        ));
        postAccepted("/api/v1/boms", Map.of(
                "revisionId", "P100-REV-B",
                "components", new Object[]{
                        Map.of("componentId", "PCB-B", "quantity", 1, "uom", "Pcs"),
                        Map.of("componentId", "SCREW", "quantity", 4, "uom", "Pcs")
                }
        ));
        postAccepted("/api/v1/suppliers", Map.of(
                "componentId", "PCB-A",
                "suppliers", new Object[]{
                        Map.of("supplierId", "SUP-ABC", "supplierName", "ABC Electronics", "leadTimeDays", 15)
                }
        ));
        postAccepted("/api/v1/suppliers", Map.of(
                "componentId", "PCB-B",
                "suppliers", new Object[]{
                        Map.of("supplierId", "SUP-ABC", "supplierName", "ABC Electronics", "leadTimeDays", 15)
                }
        ));
        postAccepted("/api/v1/inventory", Map.of(
                "componentId", "PCB-A",
                "warehouse", "WH1",
                "quantity", 500,
                "inventoryId", "INV-PCBA-WH1",
                "uom", "Pcs"
        ));
        postAccepted("/api/v1/purchase-orders", Map.of(
                "purchaseOrderId", "PO-100",
                "supplierId", "SUP-ABC",
                "componentId", "PCB-A",
                "openQuantity", 1000,
                "uom", "Pcs",
                "expectedDeliveryDate", "10/02/2026"
        ));
        postAccepted("/api/v1/work-orders", Map.of(
                "workOrderId", "WO-1001",
                "revisionId", "P100-REV-B",
                "status", "RELEASED",
                "remainingQty", 50,
                "priority", "HIGH",
                "plannedCompletionDate", "20/02/2026",
                "uom", "Box",
                "materialAvailabilityStatus", "READY"
        ));
        postAccepted("/api/v1/sales-orders", Map.of(
                "salesOrderId", "SO-100",
                "customerId", "CUST-100",
                "customerName", "Acme Corp",
                "productId", "P100",
                "openQuantity", 25,
                "orderValue", 50000,
                "priority", "CRITICAL",
                "currency", "USD"
        ));
    }

    private void postAccepted(String path, Object body) {
        ResponseEntity<Map<String, Object>> response = postMap(path, body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    private ResponseEntity<Map<String, Object>> postMap(String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", API_KEY);
        return restTemplate.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, headers), MAP_RESPONSE);
    }

    private HttpHeaders jsonHeadersWithoutApiKey() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Long count(String cypher) {
        return neo4jClient.query(cypher)
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("count").asLong())
                .one()
                .orElse(0L);
    }

    private Map<String, Object> map(ResponseEntity<Map<String, Object>> response, String field) {
        return asMap(body(response).get(field));
    }

    private Map<String, Object> firstMap(ResponseEntity<Map<String, Object>> response, String field) {
        List<?> values = (List<?>) body(response).get(field);
        return asMap(values.getFirst());
    }

    private Map<String, Object> body(ResponseEntity<Map<String, Object>> response) {
        return Objects.requireNonNull(response.getBody());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return (Map<String, Object>) value;
    }
}
