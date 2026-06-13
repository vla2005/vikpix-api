package com.vikpix.api.auth.services;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.dto.request.LoginRequest;
import com.vikpix.api.auth.dto.response.LoginResponse;
import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.auth.keycloak.dto.response.KeycloakTokenResponse;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class LoginService {

    private final KeycloakAdminClient keycloakAdminClient;
    private final AuthCookieService authCookieService;

    public LoginService(KeycloakAdminClient keycloakAdminClient, AuthCookieService authCookieService) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.authCookieService = authCookieService;
    }

    public LoginResponse execute(LoginRequest request, HttpServletResponse response) {
        boolean rememberMe = request.rememberMe() != null && request.rememberMe();

        KeycloakTokenResponse tokenResponse = keycloakAdminClient.authenticateUser(
            request.email(),
            request.password(),
            rememberMe
        );

        if (tokenResponse == null || tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
            throw new RuntimeException("Keycloak nao retornou tokens validos");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createAccessTokenCookie(tokenResponse).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRefreshTokenCookie(tokenResponse, rememberMe).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRememberMeCookie(rememberMe).toString());

        return new LoginResponse(true, "Autenticacao realizada");
    }
}
