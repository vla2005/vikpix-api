package com.vikpix.api.auth.services;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vikpix.api.auth.dto.response.TwoFactorRecoveryCodesResponse;
import com.vikpix.api.users.entities.User;
@Service
public class TwoFactorRegenerateRecoveryCodesService {
    private final CurrentUserService currentUserService;
    private final TwoFactorRecoveryCodeService recoveryCodeService;
    public TwoFactorRegenerateRecoveryCodesService(
        CurrentUserService currentUserService,
        TwoFactorRecoveryCodeService recoveryCodeService
    ) {
        this.currentUserService = currentUserService;
        this.recoveryCodeService = recoveryCodeService;
    }
    @Transactional
    public TwoFactorRecoveryCodesResponse execute(Authentication authentication) {
        User user = currentUserService.getAuthenticatedUser(authentication);
        if (!user.isTwoFactorAuthEnabled()) {
            throw new RuntimeException("2FA nao esta ativo");
        }
        return new TwoFactorRecoveryCodesResponse(recoveryCodeService.regenerate(user));
    }
}