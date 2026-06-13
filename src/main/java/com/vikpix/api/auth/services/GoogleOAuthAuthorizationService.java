package com.vikpix.api.auth.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class GoogleOAuthAuthorizationService {
    private static final int STATE_BYTES = 32;
    private static final int NONCE_BYTES = 32;
    private static final int CODE_VERIFIER_BYTES = 64;

    private final SecureRandom secureRandom = new SecureRandom();
    private final AuthCookieService authCookieService;
    private final String keycloakAuthServerUrl;
    private final String realm;
    private final String clientId;
    private final String backendUrl;

    public GoogleOAuthAuthorizationService(
        AuthCookieService authCookieService,
        @Value("${keycloak.public-auth-server-url}") String keycloakAuthServerUrl,
        @Value("${keycloak.realm}") String realm,
        @Value("${keycloak.admin-client-id}") String clientId,
        @Value("${app.backend-url}") String backendUrl
    ) {
        this.authCookieService = authCookieService;
        this.keycloakAuthServerUrl = keycloakAuthServerUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.backendUrl = backendUrl;
    }

    public String execute(Boolean rememberMe, HttpServletResponse response) {
        String state = generateUrlSafeToken(STATE_BYTES);
        String nonce = generateUrlSafeToken(NONCE_BYTES);
        String codeVerifier = generateUrlSafeToken(CODE_VERIFIER_BYTES);
        String codeChallenge = createCodeChallenge(codeVerifier);
        boolean rememberMeValue = rememberMe != null && rememberMe;

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createOAuthCookie(AuthCookieService.OAUTH_STATE_COOKIE, state).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createOAuthCookie(AuthCookieService.OAUTH_NONCE_COOKIE, nonce).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createOAuthCookie(AuthCookieService.OAUTH_CODE_VERIFIER_COOKIE, codeVerifier).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createOAuthCookie(AuthCookieService.OAUTH_REMEMBER_ME_COOKIE, String.valueOf(rememberMeValue)).toString());

        String redirectUri = getRedirectUri();

        return UriComponentsBuilder
            .fromUriString(keycloakAuthServerUrl + "/realms/" + realm + "/protocol/openid-connect/auth")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .queryParam("scope", "openid profile email")
            .queryParam("kc_idp_hint", "google")
            .queryParam("state", state)
            .queryParam("nonce", nonce)
            .queryParam("code_challenge", codeChallenge)
            .queryParam("code_challenge_method", "S256")
            .build()
            .toUriString();
    }

    public String getRedirectUri() {
        return backendUrl + "/api/auth/oauth/google/callback";
    }

    private String generateUrlSafeToken(int bytesLength) {
        byte[] bytes = new byte[bytesLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String createCodeChallenge(String codeVerifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new RuntimeException("Erro ao criar code challenge OAuth", exception);
        }
    }
}
