package com.smartSure.PolicyService.config;

import com.smartSure.PolicyService.security.JwtAuthFilter;
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

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //  Disable CSRF (stateless APIs)
                .csrf(csrf -> csrf.disable())

                //  No session (JWT based)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //  Proper exception handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED)) // 401
                        .accessDeniedHandler((req, res, ex1) -> res.setStatus(FORBIDDEN.value())) // 403
                )

                //  Authorization rules
                .authorizeHttpRequests(auth -> auth

                        //  Allow preflight requests (VERY IMPORTANT for frontend/Swagger)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        //  Actuator (health checks)
                        .requestMatchers("/actuator/**").permitAll()

                        //  Swagger / OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        //  Public APIs
                        .requestMatchers(HttpMethod.GET,
                                "/api/policy-types/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/policies/calculate-premium"
                        ).permitAll()

                        //  Everything else secured
                        .anyRequest().authenticated()
                )

                //  Add JWT filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}