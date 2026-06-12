package com.vikpix.api.auth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.auth.dto.request.NewPasswordRequest;
import com.vikpix.api.auth.dto.request.PasswordResetRequest;
import com.vikpix.api.auth.dto.response.MeResponse;
import com.vikpix.api.auth.services.GetMeService;
import com.vikpix.api.auth.services.RequestPasswordResetService;
import com.vikpix.api.auth.services.ResetPasswordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final GetMeService getMeService;
    private final RequestPasswordResetService requestPasswordResetService;
    private final ResetPasswordService resetPasswordService;

    public AuthController(GetMeService getMeService, RequestPasswordResetService requestPasswordResetService, ResetPasswordService resetPasswordService) {
        this.getMeService = getMeService;
        this.requestPasswordResetService = requestPasswordResetService;
        this.resetPasswordService = resetPasswordService;
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> getAuthenticatedUser(Authentication authentication) {
        return ResponseEntity.ok(getMeService.execute(authentication));
    }

    
    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        requestPasswordResetService.execute(passwordResetRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid NewPasswordRequest newPasswordRequest) {
        resetPasswordService.resetPassword(newPasswordRequest.token(), newPasswordRequest.newPassword());
        return ResponseEntity.ok("Password reset successfully");
    }
}