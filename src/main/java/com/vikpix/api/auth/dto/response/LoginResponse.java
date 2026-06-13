package com.vikpix.api.auth.dto.response;

public record LoginResponse(
    boolean success,
    String message
) {

}
