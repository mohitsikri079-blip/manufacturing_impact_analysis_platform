package com.miae.copilot.sft;

import com.miae.MiaeApplication;
import com.miae.analysis.ImpactEntityType;
import com.miae.copilot.integration.ImpactEngineClient;
import com.miae.exception.ResourceNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = MiaeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(CopilotPrintSftIT.PrintSftConfig.class)
@TestPropertySource(properties = {
        "copilot.enabled=true",
        "miae.security.enabled=true",
        "miae.security.api-key=dev-api-key",
        "miae.sample-data.enabled=false"
})
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class CopilotPrintSftIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void printsSupplierImpactConversationResponse() {
        ResponseEntity<String> first = post("""
                {"sessionId":"print-sft-1","message":"What happens if Supplier SUP-ABC fails?"}
                """);
        System.out.println("COPILOT PRINT SFT STATUS 1:");
        System.out.println(first.getStatusCode());
        System.out.println("COPILOT PRINT SFT RESPONSE 1:");
        System.out.println(first.getBody());

        ResponseEntity<String> followUp = post("""
                {"sessionId":"print-sft-1","message":"How many customers are affected and what revenue is at risk?"}
                """);
        System.out.println("COPILOT PRINT SFT STATUS 2:");
        System.out.println(followUp.getStatusCode());
        System.out.println("COPILOT PRINT SFT FOLLOW UP RESPONSE 2:");
        System.out.println(followUp.getBody());
    }

    @Test
    void printsComponentImpactResponse() {
        ResponseEntity<String> response = post("""
                {"sessionId":"print-sft-component-1","message":"What happens if component PCB-B becomes obsolete?"}
                """);
        System.out.println("COPILOT PRINT SFT COMPONENT STATUS:");
        System.out.println(response.getStatusCode());
        System.out.println("COPILOT PRINT SFT COMPONENT RESPONSE:");
        System.out.println(response.getBody());
    }

    private ResponseEntity<String> post(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", "dev-api-key");
        return restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/chat",
                HttpMethod.POST,
                new HttpEntity<>(json, headers),
                String.class);
    }

    @TestConfiguration
    static class PrintSftConfig {

        @Bean
        @Primary
        ImpactEngineClient impactEngineClient() {
            return new ImpactEngineClient() {
                @Override
                public Map<String, Object> analyze(ImpactEntityType entityType, String entityId) {
                    if (entityType == ImpactEntityType.SUPPLIER) {
                        return supplierImpact(entityId);
                    }
                    if (entityType == ImpactEntityType.COMPONENT) {
                        return componentImpact(entityId);
                    }
                    return Map.of();
                }

                private Map<String, Object> supplierImpact(String supplierId) {
                    if (!"SUP-ABC".equals(supplierId)) {
                        throw new ResourceNotFoundException("Supplier not found: " + supplierId);
                    }
                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("suppliedComponents", 2);
                    summary.put("affectedProducts", 1);
                    summary.put("affectedWorkOrders", 1);
                    summary.put("affectedSalesOrders", 1);
                    summary.put("affectedCustomers", 1);
                    summary.put("revenueAtRisk", 50000);

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("entityType", ImpactEntityType.SUPPLIER.name());
                    response.put("entityId", supplierId);
                    response.put("summary", summary);
                    response.put("components", new Object[]{
                            Map.of("componentId", "PCB-A"),
                            Map.of("componentId", "PCB-B")
                    });
                    response.put("affectedProducts", new Object[]{
                            Map.of("productId", "P100", "productName", "Industrial Sensor")
                    });
                    response.put("affectedWorkOrders", new Object[]{
                            Map.of("workOrderId", "WO-1001", "remainingQty", 50)
                    });
                    response.put("affectedSalesOrders", new Object[]{
                            Map.of("salesOrderId", "SO-100", "orderValue", 50000)
                    });
                    response.put("affectedCustomers", new Object[]{
                            Map.of("customerId", "CUST-100", "customerName", "Acme Corp")
                    });
                    return response;
                }

                private Map<String, Object> componentImpact(String componentId) {
                    String componentName;
                    if ("PCB-A".equals(componentId)) {
                        componentName = "Primary controller PCB";
                    } else if ("PCB-B".equals(componentId)) {
                        componentName = "Connectivity PCB";
                    } else {
                        throw new ResourceNotFoundException("Component not found: " + componentId);
                    }

                    return componentImpact(
                            componentId,
                            componentName,
                            "SUP-ABC",
                            "Acme Components",
                            "P100",
                            "Industrial Sensor",
                            "WO-1001",
                            "SO-100",
                            "CUST-100",
                            "Acme Corp",
                            50000);
                }

                private Map<String, Object> componentImpact(String componentId,
                                                            String componentName,
                                                            String supplierId,
                                                            String supplierName,
                                                            String productId,
                                                            String productName,
                                                            String workOrderId,
                                                            String salesOrderId,
                                                            String customerId,
                                                            String customerName,
                                                            int orderValue) {
                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("affectedProducts", 1);
                    summary.put("affectedRevisions", 1);
                    summary.put("inventoryLocations", 1);
                    summary.put("openPurchaseOrders", 1);
                    summary.put("affectedWorkOrders", 1);
                    summary.put("suppliers", 1);

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("entityType", ImpactEntityType.COMPONENT.name());
                    response.put("entityId", componentId);
                    response.put("summary", summary);
                    response.put("component", Map.of("componentId", componentId, "componentName", componentName));
                    response.put("productUsage", new Object[]{
                            Map.of("productId", productId, "productName", productName, "revisionId", productId + "-REV-A")
                    });
                    response.put("inventory", new Object[]{
                            Map.of("componentId", componentId, "warehouse", "WH-1", "quantity", 100)
                    });
                    response.put("suppliers", new Object[]{
                            Map.of("supplierId", supplierId, "supplierName", supplierName)
                    });
                    response.put("purchaseOrders", new Object[]{
                            Map.of("purchaseOrderId", "PO-" + componentId.substring(componentId.length() - 1), "componentId", componentId, "openQuantity", 250)
                    });
                    response.put("affectedWorkOrders", new Object[]{
                            Map.of("workOrderId", workOrderId, "remainingQty", 50)
                    });
                    response.put("affectedSalesOrders", new Object[]{
                            Map.of("salesOrderId", salesOrderId, "orderValue", orderValue)
                    });
                    response.put("affectedCustomers", new Object[]{
                            Map.of("customerId", customerId, "customerName", customerName)
                    });
                    return response;
                }
            };
        }
    }
}
