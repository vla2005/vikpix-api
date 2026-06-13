package com.vikpix.api.auth.keycloak;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vikpix.api.auth.keycloak.dto.response.KeycloakTokenResponse;
import com.vikpix.api.users.dto.request.CreateUserRequest;

@Component
public class KeycloakAdminClient {
    private final RestClient restClient;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    public KeycloakAdminClient(
            @Value("${keycloak.auth-server-url}") String authServerUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.admin-client-id}") String clientId,
            @Value("${keycloak.admin-client-secret}") String clientSecret) {
        this.restClient = RestClient.builder().baseUrl(authServerUrl).build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public KeycloakTokenResponse authenticateUser(String email, String password, boolean rememberMe) {
        String scopes = "openid profile email";
        if (rememberMe) {
            scopes += " offline_access";
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", email);
        form.add("password", password);
        form.add("scope", scopes);

        try {
            return restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KeycloakTokenResponse.class);
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            throw new RuntimeException("Falha na autenticacao: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public KeycloakTokenResponse refreshUserToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        try {
            return restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KeycloakTokenResponse.class);
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            throw new RuntimeException("Falha ao renovar token: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public void logoutUser(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/logout", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }

    public String createUser(CreateUserRequest request) {
        String adminToken = getAdminAccessToken();
        NameParts nameParts = splitName(request.name());

        CreateKeycloakUserRequest keycloakUser = new CreateKeycloakUserRequest(
                request.userName(),
                request.email(),
                nameParts.firstName(),
                nameParts.lastName(),
                true,
                true,
                List.of(),
                List.of(new CredentialRequest("password", request.password(), false)));

        ResponseEntity<Void> response = restClient.post()
                .uri("/admin/realms/{realm}/users", realm)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(keycloakUser)
                .retrieve()
                .toBodilessEntity();

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new RuntimeException("Keycloak nao retornou o ID do usuario criado");
        }

        return extractUserId(location);
    }

    public void resetPassword(String keycloakUserId, String newPassword) {
        String adminToken = getAdminAccessToken();

        CredentialRequest credential = new CredentialRequest("password", newPassword, false);

        restClient.put()
                .uri("/admin/realms/{realm}/users/{id}/reset-password", realm, keycloakUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(credential)
                .retrieve()
                .toBodilessEntity();
    }

    private String getAdminAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        TokenResponse response = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new RuntimeException("Nao foi possivel autenticar o client admin no Keycloak");
        }

        return response.accessToken();
    }

    private String extractUserId(URI location) {
        String path = location.getPath();
        int lastSlashIndex = path.lastIndexOf('/');

        if (lastSlashIndex < 0 || lastSlashIndex == path.length() - 1) {
            throw new RuntimeException("Location do Keycloak invalido: " + location);
        }

        return path.substring(lastSlashIndex + 1);
    }

    private NameParts splitName(String name) {
        String normalizedName = name == null ? "" : name.trim().replaceAll("\\s+", " ");

        if (normalizedName.isBlank()) {
            return new NameParts("Usuario", "Vikpix");
        }

        String[] parts = normalizedName.split(" ", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : firstName;

        return new NameParts(firstName, lastName);
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record CreateKeycloakUserRequest(
            String username,
            String email,
            String firstName,
            String lastName,
            boolean enabled,
            boolean emailVerified,
            List<String> requiredActions,
            List<CredentialRequest> credentials) {
    }

    private record CredentialRequest(String type, String value, boolean temporary) {
    }

    private record NameParts(String firstName, String lastName) {
    }
}
