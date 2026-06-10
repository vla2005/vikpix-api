package com.vikpix.api.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MeResponse(
    UUID id,
    String name,
    String userName,
    String email,
    String cpf,
    String phone,
    String avatarUrl,
    Boolean twoFactorAuthEnabled,
    LocalDateTime onboardedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {

}
