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

import com.vikpix.api.auth.dto.request.ConfirmPasswordRequest;
import com.vikpix.api.auth.dto.request.LoginRequest;
import com.vikpix.api.auth.dto.request.NewPasswordRequest;
import com.vikpix.api.auth.dto.request.PasswordResetRequest;
import com.vikpix.api.auth.dto.request.TwoFactorCodeRequest;
import com.vikpix.api.auth.dto.request.TwoFactorVerifyLoginRequest;
import com.vikpix.api.auth.dto.request.UpdatePasswordRequest;
import com.vikpix.api.auth.dto.response.ConfirmPasswordResponse;
import com.vikpix.api.auth.dto.response.LoginResponse;
import com.vikpix.api.auth.dto.response.MeResponse;
import com.vikpix.api.auth.dto.response.TwoFactorActivateResponse;
import com.vikpix.api.auth.dto.response.TwoFactorRecoveryCodesResponse;
import com.vikpix.api.auth.dto.response.TwoFactorSetupResponse;
import com.vikpix.api.auth.dto.response.TwoFactorStatusResponse;
import com.vikpix.api.auth.dto.response.TwoFactorVerifyLoginResponse;
import com.vikpix.api.auth.dto.response.UpdatePasswordResponse;
import com.vikpix.api.auth.services.ConfirmPasswordService;
import com.vikpix.api.auth.services.GetMeService;
import com.vikpix.api.auth.services.GoogleOAuthAuthorizationService;
import com.vikpix.api.auth.services.GoogleOAuthCallbackService;
import com.vikpix.api.auth.services.LoginService;
import com.vikpix.api.auth.services.LogoutService;
import com.vikpix.api.auth.services.RefreshTokenService;
import com.vikpix.api.auth.services.RequestPasswordResetService;
import com.vikpix.api.auth.services.ResetPasswordService;
import com.vikpix.api.auth.services.TwoFactorActivateService;
import com.vikpix.api.auth.services.TwoFactorDisableService;
import com.vikpix.api.auth.services.TwoFactorLoginChallengeService;
import com.vikpix.api.auth.services.TwoFactorRegenerateRecoveryCodesService;
import com.vikpix.api.auth.services.TwoFactorSetupService;
import com.vikpix.api.auth.services.TwoFactorStatusService;
import com.vikpix.api.auth.services.UpdatePasswordService;

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
    private final ConfirmPasswordService confirmPasswordService;
    private final UpdatePasswordService updatePasswordService;
    private final RequestPasswordResetService requestPasswordResetService;
    private final ResetPasswordService resetPasswordService;
    private final TwoFactorSetupService twoFactorSetupService;
    private final TwoFactorActivateService twoFactorActivateService;
    private final TwoFactorStatusService twoFactorStatusService;
    private final TwoFactorRegenerateRecoveryCodesService twoFactorRegenerateRecoveryCodesService;
    private final TwoFactorDisableService twoFactorDisableService;
    private final TwoFactorLoginChallengeService twoFactorLoginChallengeService;

    public AuthController(
        LoginService loginService,
        LogoutService logoutService,
        RefreshTokenService refreshTokenService,
        GoogleOAuthAuthorizationService googleOAuthAuthorizationService,
        GoogleOAuthCallbackService googleOAuthCallbackService,
        GetMeService getMeService,
        ConfirmPasswordService confirmPasswordService,
        UpdatePasswordService updatePasswordService,
        RequestPasswordResetService requestPasswordResetService,
        ResetPasswordService resetPasswordService,
        TwoFactorSetupService twoFactorSetupService,
        TwoFactorActivateService twoFactorActivateService,
        TwoFactorStatusService twoFactorStatusService,
        TwoFactorRegenerateRecoveryCodesService twoFactorRegenerateRecoveryCodesService,
        TwoFactorDisableService twoFactorDisableService,
        TwoFactorLoginChallengeService twoFactorLoginChallengeService
    ) {
        this.loginService = loginService;
        this.logoutService = logoutService;
        this.refreshTokenService = refreshTokenService;
        this.googleOAuthAuthorizationService = googleOAuthAuthorizationService;
        this.googleOAuthCallbackService = googleOAuthCallbackService;
        this.getMeService = getMeService;
        this.confirmPasswordService = confirmPasswordService;
        this.updatePasswordService = updatePasswordService;
        this.requestPasswordResetService = requestPasswordResetService;
        this.resetPasswordService = resetPasswordService;
        this.twoFactorSetupService = twoFactorSetupService;
        this.twoFactorActivateService = twoFactorActivateService;
        this.twoFactorStatusService = twoFactorStatusService;
        this.twoFactorRegenerateRecoveryCodesService = twoFactorRegenerateRecoveryCodesService;
        this.twoFactorDisableService = twoFactorDisableService;
        this.twoFactorLoginChallengeService = twoFactorLoginChallengeService;
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

    @PostMapping("/confirm-password")
    public ResponseEntity<ConfirmPasswordResponse> confirmPassword(
        Authentication authentication,
        @RequestBody @Valid ConfirmPasswordRequest request
    ) {
        return ResponseEntity.ok(confirmPasswordService.execute(authentication, request));
    }

    @PostMapping("/update-password")
    public ResponseEntity<UpdatePasswordResponse> updatePassword(
        Authentication authentication,
        @RequestBody @Valid UpdatePasswordRequest request
    ) {
        return ResponseEntity.ok(updatePasswordService.execute(authentication, request));
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<TwoFactorSetupResponse> setupTwoFactor(Authentication authentication) {
        return ResponseEntity.ok(twoFactorSetupService.execute(authentication));
    }

    @PostMapping("/2fa/activate")
    public ResponseEntity<TwoFactorActivateResponse> activateTwoFactor(
        Authentication authentication,
        @RequestBody @Valid TwoFactorCodeRequest request
    ) {
        return ResponseEntity.ok(twoFactorActivateService.execute(authentication, request.code()));
    }

    @GetMapping("/2fa/status")
    public ResponseEntity<TwoFactorStatusResponse> getTwoFactorStatus(Authentication authentication) {
        return ResponseEntity.ok(twoFactorStatusService.execute(authentication));
    }

    @PostMapping("/2fa/recovery-codes/regenerate")
    public ResponseEntity<TwoFactorRecoveryCodesResponse> regenerateTwoFactorRecoveryCodes(Authentication authentication) {
        return ResponseEntity.ok(twoFactorRegenerateRecoveryCodesService.execute(authentication));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<TwoFactorStatusResponse> disableTwoFactor(
        Authentication authentication,
        @RequestBody(required = false) TwoFactorCodeRequest request
    ) {
        String code = request == null ? null : request.code();
        return ResponseEntity.ok(twoFactorDisableService.execute(authentication, code));
    }

    @PostMapping("/2fa/verify-login")
    public ResponseEntity<TwoFactorVerifyLoginResponse> verifyTwoFactorLogin(
        @RequestBody @Valid TwoFactorVerifyLoginRequest request,
        HttpServletResponse response
    ) {
        return ResponseEntity.ok(twoFactorLoginChallengeService.verify(request.challengeToken(), request.code(), response));
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