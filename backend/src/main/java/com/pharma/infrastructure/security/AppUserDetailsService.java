package com.pharma.infrastructure.security;

import com.pharma.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
            .map(u -> org.springframework.security.core.userdetails.User.builder()
                    .username(u.getUsername())
                    .password(u.getPasswordHash()) // 💥 ВАЖНО: только хэш!
                    .disabled(!u.getEnabled())
                    .authorities(RoleAuthorityUtils.toAuthority(u.getRole().getName()))
                    .build())
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
}
}
