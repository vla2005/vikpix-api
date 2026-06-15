package com.vikpix.api.auth.services;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.dto.request.LoginRequest;
import com.vikpix.api.auth.dto.response.LoginResponse;
import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.auth.keycloak.dto.response.KeycloakTokenResponse;
import com.vikpix.api.users.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class LoginService {

    private final KeycloakAdminClient keycloakAdminClient;
    private final AuthCookieService authCookieService;
    private final UserRepository userRepository;
    private final TwoFactorLoginChallengeService twoFactorLoginChallengeService;

    public LoginService(
        KeycloakAdminClient keycloakAdminClient,
        AuthCookieService authCookieService,
        UserRepository userRepository,
        TwoFactorLoginChallengeService twoFactorLoginChallengeService
    ) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.authCookieService = authCookieService;
        this.userRepository = userRepository;
        this.twoFactorLoginChallengeService = twoFactorLoginChallengeService;
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

        var userOptional = userRepository.findByEmail(request.email());
        if (userOptional.isPresent() && userOptional.get().isTwoFactorAuthEnabled()) {
            String challengeToken = twoFactorLoginChallengeService.create(userOptional.get(), tokenResponse, rememberMe);
            return LoginResponse.twoFactorRequired(challengeToken);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createAccessTokenCookie(tokenResponse).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRefreshTokenCookie(tokenResponse, rememberMe).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRememberMeCookie(rememberMe).toString());

        return new LoginResponse(true, "Autenticacao realizada");
    }
}