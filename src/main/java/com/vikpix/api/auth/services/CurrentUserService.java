package com.vikpix.api.auth.services;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;
@Service
public class CurrentUserService {
    private final UserRepository userRepository;
    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario nao autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new RuntimeException("Autenticacao atual nao foi emitida pelo Keycloak");
        }
        String keycloakId = jwt.getSubject();
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new RuntimeException("Token do Keycloak nao possui subject");
        }
        return userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
    }
}