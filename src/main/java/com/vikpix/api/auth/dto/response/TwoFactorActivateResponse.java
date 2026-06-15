package com.vikpix.api.auth.dto.response;
import java.util.List;
public record TwoFactorActivateResponse(
    boolean enabled,
    List<String> recoveryCodes
) {
}