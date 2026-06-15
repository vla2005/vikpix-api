package com.vikpix.api.auth.services;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vikpix.api.auth.dto.response.TwoFactorActivateResponse;
import com.vikpix.api.auth.entities.TwoFactorSetup;
import com.vikpix.api.auth.repository.TwoFactorSetupRepository;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;
@Service
public class TwoFactorActivateService {
    private final CurrentUserService currentUserService;
    private final TwoFactorSetupRepository setupRepository;
    private final TwoFactorCryptoService cryptoService;
    private final TotpService totpService;
    private final TwoFactorRecoveryCodeService recoveryCodeService;
    private final UserRepository userRepository;
    public TwoFactorActivateService(
        CurrentUserService currentUserService,
        TwoFactorSetupRepository setupRepository,
        TwoFactorCryptoService cryptoService,
        TotpService totpService,
        TwoFactorRecoveryCodeService recoveryCodeService,
        UserRepository userRepository
    ) {
        this.currentUserService = currentUserService;
        this.setupRepository = setupRepository;
        this.cryptoService = cryptoService;
        this.totpService = totpService;
        this.recoveryCodeService = recoveryCodeService;
        this.userRepository = userRepository;
    }
    @Transactional
    public TwoFactorActivateResponse execute(Authentication authentication, String code) {
        User user = currentUserService.getAuthenticatedUser(authentication);
        TwoFactorSetup setup = setupRepository.findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
            .orElseThrow(() -> new RuntimeException("Setup 2FA nao encontrado"));
        if (setup.isExpired()) {
            throw new RuntimeException("Setup 2FA expirado");
        }
        String secret = cryptoService.decrypt(setup.getSecretEncrypted());
        if (!totpService.validateCode(secret, code)) {
            throw new RuntimeException("Codigo 2FA invalido");
        }
        user.enableTwoFactorAuth(setup.getSecretEncrypted());
        userRepository.save(user);
        setup.markUsed();
        setupRepository.save(setup);
        List<String> recoveryCodes = recoveryCodeService.regenerate(user);
        return new TwoFactorActivateResponse(true, recoveryCodes);
    }
}