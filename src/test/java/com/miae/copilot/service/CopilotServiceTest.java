package com.miae.copilot.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miae.analysis.ImpactEntityType;
import com.miae.copilot.ai.OpenAiClient;
import com.miae.copilot.context.ConversationContextStore;
import com.miae.copilot.dto.ChatRequest;
import com.miae.copilot.dto.ChatResponse;
import com.miae.copilot.integration.ImpactEngineClient;
import com.miae.copilot.intent.IntentDetector;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CopilotServiceTest {

    @Test
    void resolvesFollowUpQuestionUsingConversationContext() {
        AtomicInteger impactCalls = new AtomicInteger();
        ImpactEngineClient impactEngineClient = (entityType, entityId) -> {
            impactCalls.incrementAndGet();
            return supplierImpact();
        };
        OpenAiClient openAiClient = (message, intent, entityType, entityId, impactJson) ->
                "Printed answer for " + entityType + " " + entityId + ": " + message;
        CopilotService service = new CopilotService(
                new IntentDetector(),
                new ConversationContextStore(),
                impactEngineClient,
                openAiClient,
                new ObjectMapper());

        ChatResponse first = service.chat(new ChatRequest("What happens if SUP-ABC fails?", "session-1"));
        ChatResponse followUp = service.chat(new ChatRequest("How many customers are affected?", "session-1"));

        assertThat(first.entityType()).isEqualTo(ImpactEntityType.SUPPLIER);
        assertThat(first.entityId()).isEqualTo("SUP-ABC");
        assertThat(followUp.entityType()).isEqualTo(ImpactEntityType.SUPPLIER);
        assertThat(followUp.entityId()).isEqualTo("SUP-ABC");
        assertThat(impactCalls).hasValue(1);
        System.out.println("COPILOT UNIT RESPONSE 1:\n" + first.answer());
        System.out.println("COPILOT UNIT RESPONSE 2:\n" + followUp.answer());
    }

    @Test
    void asksForClarificationWhenFollowUpHasNoContext() {
        CopilotService service = new CopilotService(
                new IntentDetector(),
                new ConversationContextStore(),
                (entityType, entityId) -> Map.of(),
                (message, intent, entityType, entityId, impactJson) -> "unused",
                new ObjectMapper());

        ChatResponse response = service.chat(new ChatRequest("How many customers are affected?", "empty-session"));

        assertThat(response.clarificationRequired()).isTrue();
        System.out.println("COPILOT CLARIFICATION RESPONSE:\n" + response.answer());
    }

    private Map<String, Object> supplierImpact() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("suppliedComponents", 2);
        summary.put("affectedProducts", 1);
        summary.put("affectedWorkOrders", 1);
        summary.put("affectedSalesOrders", 1);
        summary.put("affectedCustomers", 1);
        summary.put("revenueAtRisk", 50000);

        Map<String, Object> impact = new LinkedHashMap<>();
        impact.put("entityType", "SUPPLIER");
        impact.put("entityId", "SUP-ABC");
        impact.put("summary", summary);
        impact.put("components", new Object[]{Map.of("componentId", "PCB-A"), Map.of("componentId", "PCB-B")});
        impact.put("affectedCustomers", new Object[]{Map.of("customerId", "CUST-100", "customerName", "Acme Corp")});
        return impact;
    }
}
