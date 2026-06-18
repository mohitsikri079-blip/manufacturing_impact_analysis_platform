package com.miae.copilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Manufacturing Impact Copilot application. This class maps the properties defined in the application's configuration files (e.g., application.yml or application.properties) to Java objects, allowing for easy access and management of configuration values.
 */
@ConfigurationProperties(prefix = "copilot")
public class CopilotProperties {

    private boolean enabled;
    private final OpenAi openai = new OpenAi();
    private final ImpactEngine impactEngine = new ImpactEngine();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public OpenAi getOpenai() {
        return openai;
    }

    public ImpactEngine getImpactEngine() {
        return impactEngine;
    }

    public static class OpenAi {
        private String apiKey;
        private String model;
        private String baseUrl;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class ImpactEngine {
        private String baseUrl;
        private String apiKey;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
