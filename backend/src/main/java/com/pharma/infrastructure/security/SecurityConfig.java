package com.pharma.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.http.HttpMethod;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter; // 🔥 ДОБАВИТЬ

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173"));
                config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/drugs/**").hasAnyRole("ADMIN", "PHARMACIST", "MANAGER")
                .requestMatchers(HttpMethod.POST, "/drugs/**").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers(HttpMethod.PUT, "/drugs/**").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers(HttpMethod.DELETE, "/drugs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/categories/**").hasAnyRole("ADMIN", "PHARMACIST", "MANAGER", "CASHIER")
                .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/suppliers/**").hasAnyRole("ADMIN", "PHARMACIST", "MANAGER", "CASHIER")
                .requestMatchers(HttpMethod.POST, "/suppliers/**").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers(HttpMethod.PUT, "/suppliers/**").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers(HttpMethod.DELETE, "/suppliers/**").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers("/stocks/**").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers("/analytics/**").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers("/analytics/reports/minzdrav-rb").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers("/sales/**").hasAnyRole("ADMIN", "PHARMACIST")
                .requestMatchers("/orders/**").hasAnyRole("ADMIN", "MANAGER")
                .anyRequest().authenticated()
            ); // 🔥 ВАЖНО: точка с запятой

        http.userDetailsService(userDetailsService);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
