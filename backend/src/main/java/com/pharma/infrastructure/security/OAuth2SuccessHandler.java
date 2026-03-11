package com.pharma.infrastructure.security;

import com.pharma.domain.entity.Role;
import com.pharma.domain.entity.User;
import com.pharma.domain.repository.RoleRepository;
import com.pharma.domain.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

/**
 * После успешного OAuth2 (Google) выдаём JWT и перенаправляем на frontend (требование: OAuth 2.0).
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String providerId = oauth2User.getName();

        String baseUsername = resolveUsername(email, name, providerId);
        User user = userRepository.findByUsername(baseUsername)
                .orElseGet(() -> createOAuthUser(baseUsername, email));

        String role = user.getRole().getName();
        String accessToken = jwtService.createAccessToken(user.getUsername(), role);
        String refreshToken = jwtService.createRefreshToken(user.getUsername());
        String redirectUrl = frontendUrl + "/oauth-callback?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }


    private User createOAuthUser(String baseUsername, String email) {
        String username = baseUsername;
        int attempt = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            String suffix = "_" + attempt++;
            int maxBaseLen = Math.max(1, 100 - suffix.length());
            String prefix = baseUsername.length() > maxBaseLen ? baseUsername.substring(0, maxBaseLen) : baseUsername;
            username = prefix + suffix;
        }

        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(resolveDefaultRole())
                .enabled(true)
                .build());
    }

    private String resolveUsername(String email, String name, String providerId) {
        String raw = (email != null && !email.isBlank())
                ? email.trim().toLowerCase()
                : (name != null && !name.isBlank() ? name.trim() : providerId);
        return raw.length() > 100 ? raw.substring(0, 100) : raw;
    }

    private Role resolveDefaultRole() {
        return roleRepository.findByName("PHARMACIST")
                .or(() -> roleRepository.findByName("ADMIN"))
                .or(() -> roleRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new IllegalStateException("В системе не найдены роли для OAuth-пользователя"));
    }
}
