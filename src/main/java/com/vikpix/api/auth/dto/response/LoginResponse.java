package com.vikpix.api.auth.dto.response;
public record LoginResponse(
    boolean success,
    String message,
    String status,
    String challengeToken
) {
    public LoginResponse(boolean success, String message) {
        this(success, message, null, null);
    }
    public static LoginResponse twoFactorRequired(String challengeToken) {
        return new LoginResponse(false, "Autenticacao em dois fatores necessaria", "TWO_FACTOR_REQUIRED", challengeToken);
    }
}