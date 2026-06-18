package com.miae.copilot.intent;

import com.miae.analysis.ImpactEntityType;
import com.miae.copilot.dto.ConversationContext;
import com.miae.copilot.dto.CopilotIntent;
import com.miae.copilot.dto.IntentCategory;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class IntentDetector {

    private static final Pattern REVISION_ID = Pattern.compile("\\b[A-Z0-9]+-REV-[A-Z0-9-]+\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUPPLIER_ID = Pattern.compile("\\bSUP-[A-Z0-9-]+\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMPONENT_ID = Pattern.compile("\\b(PCB-[A-Z0-9-]+|DISPLAY-[A-Z0-9-]+|SCREW|[A-Z]+-[0-9][A-Z0-9-]*)\\b", Pattern.CASE_INSENSITIVE);

    public CopilotIntent detect(String message, Optional<ConversationContext> context) {
        String normalized = message == null ? "" : message.trim();
        String lower = normalized.toLowerCase();

        if (lower.equals("reset") || lower.equals("reset context") || lower.equals("start over")) {
            return CopilotIntent.clarification("Conversation context has been reset. Please specify the Revision, Component, or Supplier to analyse.");
        }

        Matcher supplier = SUPPLIER_ID.matcher(normalized);
        if (supplier.find()) {
            return new CopilotIntent(IntentCategory.SUPPLIER_IMPACT, ImpactEntityType.SUPPLIER, supplier.group().toUpperCase(), false, null);
        }

        Matcher revision = REVISION_ID.matcher(normalized);
        if (revision.find()) {
            IntentCategory category = lower.contains("retire") || lower.contains("retirement")
                    ? IntentCategory.LIFECYCLE_IMPACT
                    : IntentCategory.REVISION_IMPACT;
            return new CopilotIntent(category, ImpactEntityType.REVISION, revision.group().toUpperCase(), false, null);
        }

        Matcher component = COMPONENT_ID.matcher(normalized);
        if (component.find() && !lower.contains("supplier")) {
            return new CopilotIntent(IntentCategory.COMPONENT_IMPACT, ImpactEntityType.COMPONENT, component.group().toUpperCase(), false, null);
        }

        if (context.isPresent() && isFollowUp(lower)) {
            ConversationContext activeContext = context.get();
            IntentCategory category = activeContext.lastEntityType() == ImpactEntityType.REVISION && (lower.contains("retire") || lower.contains("blocking"))
                    ? IntentCategory.LIFECYCLE_IMPACT
                    : IntentCategory.FOLLOW_UP;
            return new CopilotIntent(category, activeContext.lastEntityType(), activeContext.lastEntityId(), false, null);
        }

        return CopilotIntent.clarification("Please specify the Revision, Component, or Supplier you would like to analyse.");
    }

    private boolean isFollowUp(String lower) {
        return lower.contains("how many")
                || lower.contains("which")
                || lower.contains("what revenue")
                || lower.contains("revenue")
                || lower.contains("customers")
                || lower.contains("work orders")
                || lower.contains("sales orders")
                || lower.contains("products")
                || lower.contains("retired")
                || lower.contains("retire")
                || lower.contains("blocking")
                || lower.contains("critical")
                || lower.contains("depends")
                || lower.contains("it")
                || lower.contains("this");
    }
}
