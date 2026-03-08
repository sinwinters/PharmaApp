package com.pharma.infrastructure.web;

import com.pharma.application.dto.TokenResponse;
import com.pharma.application.service.AuthService;
import com.pharma.infrastructure.security.AppUserDetailsService;
import com.pharma.infrastructure.security.JwtAuthFilter;
import com.pharma.infrastructure.security.OAuth2SuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtAuthFilter jwtAuthFilter;
    @MockBean
    private OAuth2SuccessHandler oauth2SuccessHandler;
    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void loginWithValidCredentialsReturns200() throws Exception {
        when(authService.login(any())).thenReturn(new TokenResponse("access", "refresh", 3600L));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithInvalidCredentialsReturns400() throws Exception {
        when(authService.login(any())).thenThrow(new com.pharma.application.exception.PharmaException("Неверный логин или пароль"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
    }
}
