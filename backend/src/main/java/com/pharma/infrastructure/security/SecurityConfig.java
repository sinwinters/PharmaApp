package com.pharma.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(request -> {
                var c = new org.springframework.web.cors.CorsConfiguration();
                c.setAllowedOrigins(List.of("http://localhost:5173"));
                c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                c.setAllowedHeaders(List.of("*"));
                c.setAllowCredentials(true);
                return c;
            }))

            .authorizeHttpRequests(auth -> auth

                // 🔓 auth
                .requestMatchers("/auth/**").permitAll()

                // 🔥 FIX: preflight (очень часто причина 403 в браузере)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 🔥 drugs
                .requestMatchers(HttpMethod.GET, "/drugs/**")
                    .hasAnyRole("ADMIN", "PHARMACIST", "MANAGER")

                .requestMatchers(HttpMethod.POST, "/drugs/**")
                    .hasAnyRole("ADMIN", "PHARMACIST")

                .requestMatchers(HttpMethod.PUT, "/drugs/**")
                    .hasAnyRole("ADMIN", "PHARMACIST")

                .requestMatchers(HttpMethod.DELETE, "/drugs/**")
                    .hasRole("ADMIN")

                // 🔥 categories (оставляем только ADMIN)
                .requestMatchers("/categories/**")
                    .hasRole("ADMIN")

                // 🔥 suppliers
                .requestMatchers("/suppliers/**")
                    .hasRole("ADMIN")

                // orders / analytics
                .requestMatchers("/orders/**")
                    .hasAnyRole("ADMIN", "MANAGER", "PHARMACIST")

                .requestMatchers("/analytics/**")
                    .hasAnyRole("ADMIN", "PHARMACIST")

                // 🔥 важно: чтобы ошибки не ломали доступ
                .requestMatchers("/error").permitAll()

                .anyRequest().authenticated()
            );

        http.userDetailsService(userDetailsService);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}