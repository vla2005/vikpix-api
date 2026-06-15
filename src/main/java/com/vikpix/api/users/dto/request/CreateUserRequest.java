package com.vikpix.api.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank
    String name,

    @NotBlank
    String userName,

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 8)
    String password
) {
}