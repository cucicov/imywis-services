package com.example.imywisservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Allow POSTs to this endpoint without needing a CSRF token
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/nodes"))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/nodes").permitAll()
                        .anyRequest().authenticated()
                )

                // Keep defaults (you can remove/adjust if you don't want basic login elsewhere)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}