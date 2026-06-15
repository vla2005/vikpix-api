package com.vikpix.api.auth.dto.response;
import java.util.List;
public record TwoFactorRecoveryCodesResponse(
    List<String> recoveryCodes
) {
}