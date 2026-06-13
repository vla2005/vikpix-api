package com.vikpix.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    String password,

    Boolean rememberMe
) {

}
