package com.vikpix.api.auth.services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.auth.keycloak.dto.response.KeycloakTokenResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class GoogleOAuthCallbackService {
    private static final Pattern NONCE_PATTERN = Pattern.compile("\\\"nonce\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    private final KeycloakAdminClient keycloakAdminClient;
    private final AuthCookieService authCookieService;
    private final GoogleOAuthAuthorizationService googleOAuthAuthorizationService;
    private final String frontendUrl;

    public GoogleOAuthCallbackService(
        KeycloakAdminClient keycloakAdminClient,
        AuthCookieService authCookieService,
        GoogleOAuthAuthorizationService googleOAuthAuthorizationService,
        @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.authCookieService = authCookieService;
        this.googleOAuthAuthorizationService = googleOAuthAuthorizationService;
        this.frontendUrl = frontendUrl;
    }

    public String execute(String code, String state, String error, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (error != null && !error.isBlank()) {
                throw new RuntimeException("OAuth Google retornou erro: " + error);
            }

            if (code == null || code.isBlank()) {
                throw new RuntimeException("Callback OAuth sem code");
            }

            String expectedState = getCookieValue(request, AuthCookieService.OAUTH_STATE_COOKIE);
            if (expectedState == null || !expectedState.equals(state)) {
                throw new RuntimeException("State OAuth invalido");
            }

            String expectedNonce = getCookieValue(request, AuthCookieService.OAUTH_NONCE_COOKIE);
            String codeVerifier = getCookieValue(request, AuthCookieService.OAUTH_CODE_VERIFIER_COOKIE);
            boolean rememberMe = Boolean.parseBoolean(getCookieValue(request, AuthCookieService.OAUTH_REMEMBER_ME_COOKIE));

            if (expectedNonce == null || expectedNonce.isBlank()) {
                throw new RuntimeException("Nonce OAuth nao encontrado");
            }

            if (codeVerifier == null || codeVerifier.isBlank()) {
                throw new RuntimeException("Code verifier OAuth nao encontrado");
            }

            KeycloakTokenResponse tokenResponse = keycloakAdminClient.exchangeAuthorizationCode(
                code,
                googleOAuthAuthorizationService.getRedirectUri(),
                codeVerifier
            );

            if (tokenResponse == null || tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
                throw new RuntimeException("Keycloak nao retornou access token no OAuth Google");
            }

            validateNonce(tokenResponse.getIdToken(), expectedNonce);

            response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createAccessTokenCookie(tokenResponse).toString());
            response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRefreshTokenCookie(tokenResponse, rememberMe).toString());
            response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRememberMeCookie(rememberMe).toString());

            return frontendUrl + "/dashboard";
        } finally {
            clearOAuthCookies(response);
        }
    }

    private void validateNonce(String idToken, String expectedNonce) {
        if (idToken == null || idToken.isBlank()) {
            throw new RuntimeException("Keycloak nao retornou id_token para validar nonce");
        }

        String[] parts = idToken.split("\\.");
        if (parts.length < 2) {
            throw new RuntimeException("id_token invalido");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        Matcher matcher = NONCE_PATTERN.matcher(payload);

        if (!matcher.find()) {
            throw new RuntimeException("id_token nao possui nonce");
        }

        String actualNonce = matcher.group(1);
        if (!expectedNonce.equals(actualNonce)) {
            throw new RuntimeException("Nonce OAuth invalido");
        }
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
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

    private void clearOAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearOAuthCookie(AuthCookieService.OAUTH_STATE_COOKIE).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearOAuthCookie(AuthCookieService.OAUTH_NONCE_COOKIE).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearOAuthCookie(AuthCookieService.OAUTH_CODE_VERIFIER_COOKIE).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearOAuthCookie(AuthCookieService.OAUTH_REMEMBER_ME_COOKIE).toString());
    }
}
