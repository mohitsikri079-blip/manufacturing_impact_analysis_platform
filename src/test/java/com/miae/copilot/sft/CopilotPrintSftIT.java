package com.miae.copilot.sft;

import com.miae.MiaeApplication;
import com.miae.analysis.ImpactEntityType;
import com.miae.copilot.integration.ImpactEngineClient;
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
        System.out.println("COPILOT PRINT SFT RESPONSE 2:");
        System.out.println(followUp.getBody());
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
                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("suppliedComponents", 2);
                    summary.put("affectedProducts", 1);
                    summary.put("affectedWorkOrders", 1);
                    summary.put("affectedSalesOrders", 1);
                    summary.put("affectedCustomers", 1);
                    summary.put("revenueAtRisk", 50000);

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("entityType", entityType.name());
                    response.put("entityId", entityId);
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
            };
        }
    }
}
