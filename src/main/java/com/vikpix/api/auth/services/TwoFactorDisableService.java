package com.vikpix.api.auth.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.auth.dto.response.TwoFactorStatusResponse;
import com.vikpix.api.auth.repository.TwoFactorSetupRepository;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;

@Service
public class TwoFactorDisableService {
    private final CurrentUserService currentUserService;
    private final TwoFactorCryptoService cryptoService;
    private final TotpService totpService;
    private final TwoFactorRecoveryCodeService recoveryCodeService;
    private final TwoFactorSetupRepository setupRepository;
    private final UserRepository userRepository;

    public TwoFactorDisableService(
        CurrentUserService currentUserService,
        TwoFactorCryptoService cryptoService,
        TotpService totpService,
        TwoFactorRecoveryCodeService recoveryCodeService,
        TwoFactorSetupRepository setupRepository,
        UserRepository userRepository
    ) {
        this.currentUserService = currentUserService;
        this.cryptoService = cryptoService;
        this.totpService = totpService;
        this.recoveryCodeService = recoveryCodeService;
        this.setupRepository = setupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TwoFactorStatusResponse execute(Authentication authentication, String code) {
        User user = currentUserService.getAuthenticatedUser(authentication);

        if (user.isTwoFactorAuthEnabled()) {
            if (code == null || code.isBlank()) {
                throw new RuntimeException("Codigo 2FA obrigatorio para desativar");
            }

            String secret = cryptoService.decrypt(user.getTwoFactorSecretEncrypted());
            boolean validTotp = totpService.validateCode(secret, code);
            boolean validRecoveryCode = recoveryCodeService.consume(user, code);

            if (!validTotp && !validRecoveryCode) {
                throw new RuntimeException("Codigo 2FA invalido");
            }
        }

        user.disableTwoFactorAuth();
        userRepository.save(user);
        recoveryCodeService.deleteAll(user);
        setupRepository.deleteByUser(user);

        return new TwoFactorStatusResponse(false);
    }
}