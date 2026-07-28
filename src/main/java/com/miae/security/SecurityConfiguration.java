package com.miae.security;

import java.io.IOException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 
 * SecurityConfiguration configures the security settings for the application, including authentication mechanisms and filter chains.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationStrategyFactory strategyFactory,
                                             SecurityProperties properties) throws Exception {
        AuthenticationStrategy strategy = strategyFactory.selected();
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/api/**").authenticated().anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((request, response, ex) -> unauthorized(response)));
        if (strategy.type() == AuthenticationType.DEVELOPER_API_KEY) {
            http.addFilterBefore(new DeveloperApiKeyAuthenticationFilter(properties.getApiKey()), UsernamePasswordAuthenticationFilter.class);
        } else {
            http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.decoder(strategy.jwtDecoder()))
                    .authenticationEntryPoint((request, response, ex) -> unauthorized(response)));
        }
        return http.build();
    }

    private void unauthorized(jakarta.servlet.http.HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"Authentication required or credentials are invalid\"}");
    }
}
