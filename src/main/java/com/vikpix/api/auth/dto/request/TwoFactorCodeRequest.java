package com.vikpix.api.auth.dto.request;
import jakarta.validation.constraints.NotBlank;
public record TwoFactorCodeRequest(
    @NotBlank String code
) {
}