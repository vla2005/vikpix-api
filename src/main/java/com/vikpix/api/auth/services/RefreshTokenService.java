package com.vikpix.api.auth.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.auth.entities.RefreshToken;
import com.vikpix.api.auth.repositories.RefreshTokenRepository;
import com.vikpix.api.users.entities.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class RefreshTokenService {
    private static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/auth";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${auth.refresh-token-short-expiration-ms:86400000}")
    private long shortRefreshTokenExpirationMs;

    @Value("${auth.refresh-token-long-expiration-ms:2592000000}")
    private long longRefreshTokenExpirationMs;

    @Value("${auth.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;

    @Transactional
    public IssuedRefreshToken createForUser(User user, boolean rememberMe) {
        long durationMs = getDurationMs(rememberMe);
        String rawToken = generateRawToken();
        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = RefreshToken.builder()
            .tokenHash(hash(rawToken))
            .user(user)
            .rememberMe(rememberMe)
            .createdAt(now)
            .expiresAt(now.plus(Duration.ofMillis(durationMs)))
            .build();

        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(user, rawToken, durationMs);
    }

    @Transactional
    public IssuedRefreshToken rotate(String rawToken) {
        RefreshToken currentToken = findUsableToken(rawToken);
        boolean rememberMe = Boolean.TRUE.equals(currentToken.getRememberMe());

        currentToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(currentToken);

        return createForUser(currentToken.getUser(), rememberMe);
    }

    @Transactional
    public void revoke(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new RuntimeException("Refresh token invalido"));

        if (!refreshToken.isRevoked()) {
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
        }
    }

    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new RuntimeException("Refresh token nao encontrado");
        }

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new RuntimeException("Refresh token nao encontrado");
    }

    public ResponseCookie buildRefreshCookie(IssuedRefreshToken issuedRefreshToken) {
        return buildRefreshCookie(issuedRefreshToken.rawToken(), issuedRefreshToken.durationMs());
    }

    public ResponseCookie buildRefreshCookie(String rawToken, long durationMs) {
        return ResponseCookie.from(COOKIE_NAME, rawToken)
            .httpOnly(true)
            .secure(refreshCookieSecure)
            .sameSite("Strict")
            .path(COOKIE_PATH)
            .maxAge(Duration.ofMillis(durationMs))
            .build();
    }

    public ResponseCookie buildClearRefreshCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
            .httpOnly(true)
            .secure(refreshCookieSecure)
            .sameSite("Strict")
            .path(COOKIE_PATH)
            .maxAge(0)
            .build();
    }

    private RefreshToken findUsableToken(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new RuntimeException("Refresh token invalido"));

        if (!refreshToken.isActive()) {
            throw new RuntimeException("Refresh token expirado ou revogado");
        }

        return refreshToken;
    }

    private long getDurationMs(boolean rememberMe) {
        return rememberMe ? longRefreshTokenExpirationMs : shortRefreshTokenExpirationMs;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    public record IssuedRefreshToken(User user, String rawToken, long durationMs) {
    }
}
