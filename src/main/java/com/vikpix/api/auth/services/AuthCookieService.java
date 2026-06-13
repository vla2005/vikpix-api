package com.vikpix.api.auth.services;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.keycloak.dto.response.KeycloakTokenResponse;

@Service
public class AuthCookieService {
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    public static final String REMEMBER_ME_COOKIE = "remember_me";
    public static final String OAUTH_STATE_COOKIE = "oauth_state";
    public static final String OAUTH_NONCE_COOKIE = "oauth_nonce";
    public static final String OAUTH_CODE_VERIFIER_COOKIE = "oauth_code_verifier";
    public static final String OAUTH_REMEMBER_ME_COOKIE = "oauth_remember_me";

    private static final long DEFAULT_ACCESS_TOKEN_MAX_AGE_SECONDS = 300;
    private static final long REMEMBER_ME_REFRESH_MAX_AGE_SECONDS = 2_592_000;
    private static final long OAUTH_COOKIE_MAX_AGE_SECONDS = 300;
    private static final String DEFAULT_PATH = "/";
    private static final String OAUTH_PATH = "/api/auth/oauth";

    public ResponseCookie createAccessTokenCookie(KeycloakTokenResponse tokenResponse) {
        long expiresInSeconds = tokenResponse.getExpiresIn() != null
            ? tokenResponse.getExpiresIn()
            : DEFAULT_ACCESS_TOKEN_MAX_AGE_SECONDS;

        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, tokenResponse.getAccessToken())
            .httpOnly(true)
            .secure(false)
            .path(DEFAULT_PATH)
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
            .path(DEFAULT_PATH)
            .maxAge(maxAge)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie createRememberMeCookie(boolean rememberMe) {
        return ResponseCookie.from(REMEMBER_ME_COOKIE, String.valueOf(rememberMe))
            .httpOnly(true)
            .secure(false)
            .path(DEFAULT_PATH)
            .maxAge(rememberMe ? REMEMBER_ME_REFRESH_MAX_AGE_SECONDS : -1)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie createOAuthCookie(String name, String value) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(false)
            .path(OAUTH_PATH)
            .maxAge(OAUTH_COOKIE_MAX_AGE_SECONDS)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return clearDefaultPathCookie(ACCESS_TOKEN_COOKIE);
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return clearDefaultPathCookie(REFRESH_TOKEN_COOKIE);
    }

    public ResponseCookie clearRememberMeCookie() {
        return clearDefaultPathCookie(REMEMBER_ME_COOKIE);
    }

    public ResponseCookie clearOAuthCookie(String name) {
        return clearCookie(name, OAUTH_PATH);
    }

    private ResponseCookie clearDefaultPathCookie(String name) {
        return clearCookie(name, DEFAULT_PATH);
    }

    private ResponseCookie clearCookie(String name, String path) {
        return ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(false)
            .path(path)
            .maxAge(0)
            .sameSite("Lax")
            .build();
    }
}
