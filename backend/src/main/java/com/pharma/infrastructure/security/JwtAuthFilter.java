package com.pharma.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println("➡️ REQUEST: " + request.getRequestURI());

        // ❌ нет токена — просто дальше
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        var usernameOpt = jwtService.extractAccessUsername(token);

        // ❌ битый токен — не ломаем запрос
        if (usernameOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = usernameOpt.get();

        // ✔ если уже авторизован — не пересоздаём
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 🔥 ROLE FIX (ключевой момент)
        List<SimpleGrantedAuthority> authorities =
                jwtService.extractAccessRole(token)
                        .map(role -> {
                            System.out.println("🔐 ROLE FROM TOKEN: " + role);
                            return new SimpleGrantedAuthority("ROLE_" + role);
                        })
                        .map(List::of)
                        .orElseGet(() -> {
                            System.out.println("⚠️ fallback to DB roles");
                            return userDetails.getAuthorities().stream()
                                    .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                                    .toList();
                        });

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );

        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(auth);

        System.out.println("✅ AUTH SET: " + auth.getAuthorities());

        filterChain.doFilter(request, response);
    }
}