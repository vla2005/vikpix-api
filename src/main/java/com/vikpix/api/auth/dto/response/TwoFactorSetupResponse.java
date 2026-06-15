package com.vikpix.api.auth.dto.response;
public record TwoFactorSetupResponse(
    String qrCode,
    String secret,
    String otpauthUrl
) {
}