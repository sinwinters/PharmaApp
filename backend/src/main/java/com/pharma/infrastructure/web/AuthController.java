package com.pharma.infrastructure.web;

import com.pharma.application.dto.UserDto;
import com.pharma.domain.repository.UserRepository;
import com.pharma.infrastructure.security.JwtService;
import com.pharma.infrastructure.security.RoleAuthorityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            String accessToken = jwtService.createAccessToken(auth.getName(),
                    RoleAuthorityUtils.normalizeRoleName(auth.getAuthorities().iterator().next().getAuthority()));
            String refreshToken = jwtService.createRefreshToken(auth.getName());

            return ResponseEntity.ok(new TokensResponse(accessToken, refreshToken));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Неверный логин или пароль");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> currentUser(@AuthenticationPrincipal UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .map(u -> new UserDto(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRole().getName(),
                        u.getEnabled()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record AuthRequest(String username, String password) {}
    public record TokensResponse(String accessToken, String refreshToken) {}
}
