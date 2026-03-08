package com.pharma.infrastructure.web;

import com.pharma.AbstractIntegrationTest;
import com.pharma.domain.entity.Role;
import com.pharma.domain.entity.User;
import com.pharma.domain.repository.RoleRepository;
import com.pharma.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционный тест с Testcontainers (PostgreSQL): реальная БД, Flyway, демо-данные.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class AuthIntegrationTestcontainersTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ensureTestUser("admin", "password");
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    private void ensureTestUser(String username, String rawPassword) {
        Role role = roleRepository.findByName("ADMIN")
                .or(() -> roleRepository.findByName("PHARMACIST"))
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").description("autocreated in test").build()));

        userRepository.findByUsername(username)
                .ifPresentOrElse(existing -> {
                    existing.setPasswordHash(passwordEncoder.encode(rawPassword));
                    existing.setRole(role);
                    existing.setEnabled(true);
                    userRepository.save(existing);
                }, () -> userRepository.save(User.builder()
                        .username(username)
                        .passwordHash(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .enabled(true)
                        .build()));
    }

    @Test
    void loginWithDemoUserReturnsTokens() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void loginWithWrongPasswordReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
    }
}
