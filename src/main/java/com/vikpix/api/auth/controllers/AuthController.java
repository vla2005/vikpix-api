package com.vikpix.api.auth.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.auth.dto.request.LoginRequest;
import com.vikpix.api.auth.dto.request.NewPasswordRequest;
import com.vikpix.api.auth.dto.request.PasswordResetRequest;
import com.vikpix.api.auth.dto.response.LoginResponse;
import com.vikpix.api.auth.dto.response.MeResponse;
import com.vikpix.api.auth.services.GetMeService;
import com.vikpix.api.auth.services.GoogleOAuthAuthorizationService;
import com.vikpix.api.auth.services.GoogleOAuthCallbackService;
import com.vikpix.api.auth.services.LoginService;
import com.vikpix.api.auth.services.LogoutService;
import com.vikpix.api.auth.services.RefreshTokenService;
import com.vikpix.api.auth.services.RequestPasswordResetService;
import com.vikpix.api.auth.services.ResetPasswordService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final RefreshTokenService refreshTokenService;
    private final GoogleOAuthAuthorizationService googleOAuthAuthorizationService;
    private final GoogleOAuthCallbackService googleOAuthCallbackService;
    private final GetMeService getMeService;
    private final RequestPasswordResetService requestPasswordResetService;
    private final ResetPasswordService resetPasswordService;

    public AuthController(
        LoginService loginService,
        LogoutService logoutService,
        RefreshTokenService refreshTokenService,
        GoogleOAuthAuthorizationService googleOAuthAuthorizationService,
        GoogleOAuthCallbackService googleOAuthCallbackService,
        GetMeService getMeService,
        RequestPasswordResetService requestPasswordResetService,
        ResetPasswordService resetPasswordService
    ) {
        this.loginService = loginService;
        this.logoutService = logoutService;
        this.refreshTokenService = refreshTokenService;
        this.googleOAuthAuthorizationService = googleOAuthAuthorizationService;
        this.googleOAuthCallbackService = googleOAuthCallbackService;
        this.getMeService = getMeService;
        this.requestPasswordResetService = requestPasswordResetService;
        this.resetPasswordService = resetPasswordService;
    }

    @GetMapping("/oauth/google")
    public ResponseEntity<Void> googleOAuth(@RequestParam(required = false) Boolean rememberMe, HttpServletResponse response) {
        String authorizationUrl = googleOAuthAuthorizationService.execute(rememberMe, response);

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, authorizationUrl)
            .build();
    }

    @GetMapping("/oauth/google/callback")
    public ResponseEntity<Void> googleOAuthCallback(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String state,
        @RequestParam(required = false) String error,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String redirectUrl = googleOAuthCallbackService.execute(code, state, error, request, response);

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, redirectUrl)
            .build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @RequestBody @Valid LoginRequest request,
        HttpServletResponse response
    ) {
        return ResponseEntity.ok(loginService.execute(request, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(refreshTokenService.execute(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        logoutService.execute(request, response);
        return ResponseEntity.ok("Logout successful");
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
