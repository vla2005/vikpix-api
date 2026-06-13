package com.vikpix.api.auth.services;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.dto.response.LoginResponse;
import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.auth.keycloak.dto.response.KeycloakTokenResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class RefreshTokenService {
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String REMEMBER_ME_COOKIE = "remember_me";

    private final KeycloakAdminClient keycloakAdminClient;
    private final AuthCookieService authCookieService;

    public RefreshTokenService(KeycloakAdminClient keycloakAdminClient, AuthCookieService authCookieService) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.authCookieService = authCookieService;
    }

    public LoginResponse execute(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookieValue(request, REFRESH_TOKEN_COOKIE);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token nao encontrado");
        }

        boolean rememberMe = Boolean.parseBoolean(extractCookieValue(request, REMEMBER_ME_COOKIE));
        KeycloakTokenResponse tokenResponse = keycloakAdminClient.refreshUserToken(refreshToken);

        if (tokenResponse == null || tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
            throw new RuntimeException("Keycloak nao retornou um novo access token");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createAccessTokenCookie(tokenResponse).toString());

        if (tokenResponse.getRefreshToken() != null && !tokenResponse.getRefreshToken().isBlank()) {
            response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRefreshTokenCookie(tokenResponse, rememberMe).toString());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRememberMeCookie(rememberMe).toString());

        return new LoginResponse(true, "Token renovado");
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
