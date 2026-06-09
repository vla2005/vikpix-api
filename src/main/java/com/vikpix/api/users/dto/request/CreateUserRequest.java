package com.vikpix.api.users.dto.request;

public record CreateUserRequest(
    String name,
    String email,
    String password
) {

}


