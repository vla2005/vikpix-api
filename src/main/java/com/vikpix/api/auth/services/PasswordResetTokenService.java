package com.vikpix.api.auth.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.vikpix.api.auth.entities.PasswordResetToken;
import com.vikpix.api.auth.repository.PasswordResetTokenRepository;
import com.vikpix.api.users.entities.User;

import jakarta.transaction.Transactional;

@Service
public class PasswordResetTokenService {
    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_EXPIRATION_MINUTES = 15;

    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public String createToken(User user) {
        passwordResetTokenRepository.deleteByUserAndUsedAtIsNull(user);

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
            .user(user)
            .tokenHash(tokenHash)
            .expiresAt(java.time.LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES))
            .build();

        passwordResetTokenRepository.save(passwordResetToken);

        return rawToken;
    }

    public String hashToken(String rawToken){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder()
                .withoutPadding()
                .encodeToString(hashBytes);
        } catch (Exception exception) {
            throw new RuntimeException("Erro ao gerar hash do token de reset de senha", exception);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }
}
