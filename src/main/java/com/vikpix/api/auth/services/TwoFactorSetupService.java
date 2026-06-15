package com.vikpix.api.auth.services;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vikpix.api.auth.dto.response.TwoFactorSetupResponse;
import com.vikpix.api.auth.entities.TwoFactorSetup;
import com.vikpix.api.auth.repository.TwoFactorSetupRepository;
import com.vikpix.api.users.entities.User;
@Service
public class TwoFactorSetupService {
    private static final String ISSUER = "VikPix";
    private static final long SETUP_EXPIRATION_MINUTES = 10;
    private final CurrentUserService currentUserService;
    private final TotpService totpService;
    private final TwoFactorCryptoService cryptoService;
    private final TwoFactorSetupRepository setupRepository;
    public TwoFactorSetupService(
        CurrentUserService currentUserService,
        TotpService totpService,
        TwoFactorCryptoService cryptoService,
        TwoFactorSetupRepository setupRepository
    ) {
        this.currentUserService = currentUserService;
        this.totpService = totpService;
        this.cryptoService = cryptoService;
        this.setupRepository = setupRepository;
    }
    @Transactional
    public TwoFactorSetupResponse execute(Authentication authentication) {
        User user = currentUserService.getAuthenticatedUser(authentication);
        String secret = totpService.generateSecret();
        String secretEncrypted = cryptoService.encrypt(secret);
        setupRepository.deleteByUser(user);
        setupRepository.save(new TwoFactorSetup(user, secretEncrypted, LocalDateTime.now().plusMinutes(SETUP_EXPIRATION_MINUTES)));
        String otpauthUrl = totpService.createOtpAuthUrl(ISSUER, user.getEmail(), secret);
        String qrCode = totpService.createQrCodeDataUrl(otpauthUrl);
        return new TwoFactorSetupResponse(qrCode, secret, otpauthUrl);
    }
}