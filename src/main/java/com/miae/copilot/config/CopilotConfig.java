package com.miae.copilot.config;

import com.miae.copilot.exception.CopilotConfigurationException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Manufacturing Impact Copilot application. It enables configuration properties and validates the necessary configurations at application startup.
 */
@Configuration
@EnableConfigurationProperties(CopilotProperties.class)
public class CopilotConfig {

    @Bean
    ApplicationRunner copilotConfigurationValidator(CopilotProperties properties) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) {
                if (properties.isEnabled() && isBlank(properties.getOpenai().getApiKey())) {
                    throw new CopilotConfigurationException("OPENAI_API_KEY is required when COPILOT_ENABLED=true");
                }
            }
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
