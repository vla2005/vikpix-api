package com.vikpix.api.auth.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.auth.dto.response.MeResponse;
import com.vikpix.api.auth.services.GetMeService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final GetMeService getMeService;

    public AuthController(GetMeService getMeService) {
        this.getMeService = getMeService;
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> getAuthenticatedUser(Authentication authentication) {
        return ResponseEntity.ok(getMeService.execute(authentication));
    }
}