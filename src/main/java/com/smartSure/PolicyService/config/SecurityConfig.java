package com.smartSure.PolicyService.config;

import com.smartSure.PolicyService.security.HeaderAuthenticationFilter;
import com.smartSure.PolicyService.security.InternalRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final InternalRequestFilter internalRequestFilter;
    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF (stateless APIs)
                .csrf(csrf -> csrf.disable())

                // Stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Exception handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED)) // 401
                        .accessDeniedHandler((req, res, ex1) -> res.setStatus(FORBIDDEN.value())) // 403
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Preflight (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Public APIs
                        .requestMatchers(HttpMethod.GET, "/api/policy-types/**").permitAll()
                        .requestMatchers("/api/policies/calculate-premium").permitAll()

                        // Everything else requires authentication
                        .anyRequest().permitAll()
                )

                //  SECURITY FILTER FLOW
                .addFilterBefore(internalRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(headerAuthenticationFilter, InternalRequestFilter.class);

        return http.build();
    }
}