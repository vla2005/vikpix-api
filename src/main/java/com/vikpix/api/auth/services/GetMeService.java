package com.vikpix.api.auth.services;

import java.text.Normalizer;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.auth.dto.response.MeResponse;
import com.vikpix.api.donation.services.CreateDefaultDonationConfigsService;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;
import com.vikpix.api.widgets.services.CreateDefaultWidgetsService;

@Service
public class GetMeService {
    private final UserRepository userRepository;
    private final CreateDefaultWidgetsService createDefaultWidgetsService;
    private final CreateDefaultDonationConfigsService createDefaultDonationConfigsService;

    public GetMeService(
        UserRepository userRepository,
        CreateDefaultWidgetsService createDefaultWidgetsService,
        CreateDefaultDonationConfigsService createDefaultDonationConfigsService
    ) {
        this.userRepository = userRepository;
        this.createDefaultWidgetsService = createDefaultWidgetsService;
        this.createDefaultDonationConfigsService = createDefaultDonationConfigsService;
    }

    @Transactional
    public MeResponse execute(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario nao autenticado");
        }

        Jwt jwt = extractJwt(authentication);
        String keycloakId = extractKeycloakId(jwt);

        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseGet(() -> createUserFromToken(jwt));

        updateAvatarWhenMissing(user, jwt);

        return new MeResponse(
            user.getUuid(),
            user.getName(),
            user.getUserName(),
            user.getEmail(),
            user.getCpf(),
            user.getPhone(),
            user.getAvatarUrl(),
            user.isTwoFactorAuthEnabled(),
            user.getOnboardedAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    private User createUserFromToken(Jwt jwt) {
        String keycloakId = extractKeycloakId(jwt);
        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Token do Keycloak nao possui email");
        }

        return userRepository.findByEmail(email)
            .orElseGet(() -> createLocalUserWithDefaults(jwt, keycloakId, email));
    }

    private User createLocalUserWithDefaults(Jwt jwt, String keycloakId, String email) {
        User user = User.builder()
            .keycloakId(keycloakId)
            .name(resolveName(jwt, email))
            .userName(generateUniqueUserName(jwt, email))
            .email(email)
            .avatarUrl(resolveAvatarUrl(jwt))
            .build();

        User savedUser = userRepository.save(user);

        createDefaultWidgetsService.execute(savedUser);
        createDefaultDonationConfigsService.execute(savedUser);

        return savedUser;
    }

    private void updateAvatarWhenMissing(User user, Jwt jwt) {
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            return;
        }

        String avatarUrl = resolveAvatarUrl(jwt);

        if (avatarUrl != null) {
            user.updateAvatarUrl(avatarUrl);
        }
    }

    private Jwt extractJwt(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt;
        }

        throw new RuntimeException("Autenticacao atual nao foi emitida pelo Keycloak");
    }

    private String extractKeycloakId(Jwt jwt) {
        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new RuntimeException("Token do Keycloak nao possui subject");
        }

        return subject;
    }

    private String resolveAvatarUrl(Jwt jwt) {
        String picture = jwt.getClaimAsString("picture");

        if (picture == null || picture.isBlank()) {
            return null;
        }

        return picture;
    }

    private String resolveName(Jwt jwt, String email) {
        String name = jwt.getClaimAsString("name");

        if (name != null && !name.isBlank()) {
            return name;
        }

        String preferredUsername = jwt.getClaimAsString("preferred_username");

        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        return email.substring(0, email.indexOf('@'));
    }

    private String generateUniqueUserName(Jwt jwt, String email) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String name = jwt.getClaimAsString("name");
        String emailPrefix = email.substring(0, email.indexOf('@'));

        String base = firstPresentNonEmail(preferredUsername, name, emailPrefix);
        base = normalizeUserName(base);

        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;

        while (userRepository.existsByUserName(candidate)) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }

    private String firstPresentNonEmail(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank() && !value.contains("@")) {
                return value;
            }
        }

        return "user";
    }

    private String normalizeUserName(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9._-]", "")
            .replaceAll("^[._-]+|[._-]+$", "");

        return normalized;
    }
}
