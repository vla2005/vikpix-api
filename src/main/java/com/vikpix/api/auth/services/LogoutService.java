package com.vikpix.api.auth.services;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.keycloak.KeycloakAdminClient;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LogoutService {
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final KeycloakAdminClient keycloakAdminClient;
    private final AuthCookieService authCookieService;

    public LogoutService(KeycloakAdminClient keycloakAdminClient, AuthCookieService authCookieService) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.authCookieService = authCookieService;
    }

    public void execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = extractRefreshToken(request);

            if (refreshToken != null && !refreshToken.isBlank()) {
                keycloakAdminClient.logoutUser(refreshToken);
            }
        } finally {
            response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearAccessTokenCookie().toString());
            response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearRefreshTokenCookie().toString());
            response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearRememberMeCookie().toString());
        }
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
