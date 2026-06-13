package com.vikpix.api.auth.services;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.keycloak.dto.response.KeycloakTokenResponse;

@Service
public class AuthCookieService {
    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String REMEMBER_ME_COOKIE = "remember_me";
    private static final long DEFAULT_ACCESS_TOKEN_MAX_AGE_SECONDS = 300;
    private static final long REMEMBER_ME_REFRESH_MAX_AGE_SECONDS = 2_592_000;

    public ResponseCookie createAccessTokenCookie(KeycloakTokenResponse tokenResponse) {
        long expiresInSeconds = tokenResponse.getExpiresIn() != null
            ? tokenResponse.getExpiresIn()
            : DEFAULT_ACCESS_TOKEN_MAX_AGE_SECONDS;

        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, tokenResponse.getAccessToken())
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(expiresInSeconds)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie createRefreshTokenCookie(KeycloakTokenResponse tokenResponse, boolean rememberMe) {
        String refreshToken = tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken() : "";
        long maxAge = rememberMe ? REMEMBER_ME_REFRESH_MAX_AGE_SECONDS : -1;

        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(maxAge)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie createRememberMeCookie(boolean rememberMe) {
        return ResponseCookie.from(REMEMBER_ME_COOKIE, String.valueOf(rememberMe))
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(rememberMe ? REMEMBER_ME_REFRESH_MAX_AGE_SECONDS : -1)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return clearCookie(ACCESS_TOKEN_COOKIE);
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return clearCookie(REFRESH_TOKEN_COOKIE);
    }

    public ResponseCookie clearRememberMeCookie() {
        return clearCookie(REMEMBER_ME_COOKIE);
    }

    private ResponseCookie clearCookie(String name) {
        return ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();
    }
}
