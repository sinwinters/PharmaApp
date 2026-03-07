package com.pharma.application.service;

import com.pharma.application.dto.LoginRequest;
import com.pharma.application.exception.PharmaException;
import com.pharma.domain.entity.Role;
import com.pharma.domain.entity.User;
import com.pharma.domain.repository.UserRepository;
import com.pharma.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "pass")))
                .isInstanceOf(PharmaException.class)
                .hasMessageContaining("Неверный логин или пароль");
    }

    @Test
    void login_throwsWhenPasswordInvalid() {
        Role role = new Role(1L, "ADMIN", null);
        User user = User.builder()
                .id(1L)
                .username("admin")
                .passwordHash("$2a$10$hash")
                .role(role)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(PharmaException.class)
                .hasMessageContaining("Неверный логин или пароль");
    }
}
