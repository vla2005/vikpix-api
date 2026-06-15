package com.vikpix.api.auth.dto.request;
import jakarta.validation.constraints.NotBlank;
public record TwoFactorVerifyLoginRequest(
    @NotBlank String challengeToken,
    @NotBlank String code
) {
}