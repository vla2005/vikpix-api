package com.vikpix.api.auth.dto.request;

public record LoginRequest(
    String email,
    String password,
    Boolean rememberMe
) {
    public boolean shouldRemember() {
        return Boolean.TRUE.equals(rememberMe);
    }
}
