package com.vikpix.api.auth.dto.request;

public record LoginRequest(
    String email,
    String password
) {

}

