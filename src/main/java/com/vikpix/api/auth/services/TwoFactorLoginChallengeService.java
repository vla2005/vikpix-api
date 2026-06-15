package com.vikpix.api.auth.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.auth.dto.response.TwoFactorVerifyLoginResponse;
import com.vikpix.api.auth.entities.TwoFactorLoginChallenge;
import com.vikpix.api.auth.keycloak.dto.response.KeycloakTokenResponse;
import com.vikpix.api.auth.repository.TwoFactorLoginChallengeRepository;
import com.vikpix.api.users.entities.User;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class TwoFactorLoginChallengeService {
    private static final int TOKEN_BYTES = 32;
    private static final long CHALLENGE_EXPIRATION_MINUTES = 5;

    private final SecureRandom secureRandom = new SecureRandom();
    private final TwoFactorLoginChallengeRepository challengeRepository;
    private final TwoFactorCryptoService cryptoService;
    private final TotpService totpService;
    private final TwoFactorRecoveryCodeService recoveryCodeService;
    private final AuthCookieService authCookieService;

    public TwoFactorLoginChallengeService(
        TwoFactorLoginChallengeRepository challengeRepository,
        TwoFactorCryptoService cryptoService,
        TotpService totpService,
        TwoFactorRecoveryCodeService recoveryCodeService,
        AuthCookieService authCookieService
    ) {
        this.challengeRepository = challengeRepository;
        this.cryptoService = cryptoService;
        this.totpService = totpService;
        this.recoveryCodeService = recoveryCodeService;
        this.authCookieService = authCookieService;
    }

    @Transactional
    public String create(User user, KeycloakTokenResponse tokenResponse, boolean rememberMe) {
        String challengeToken = generateToken();
        TwoFactorLoginChallenge challenge = new TwoFactorLoginChallenge(
            challengeToken,
            user,
            cryptoService.encrypt(tokenResponse.getAccessToken()),
            cryptoService.encrypt(tokenResponse.getRefreshToken()),
            tokenResponse.getExpiresIn(),
            tokenResponse.getRefreshExpiresIn(),
            rememberMe,
            LocalDateTime.now().plusMinutes(CHALLENGE_EXPIRATION_MINUTES)
        );

        challengeRepository.save(challenge);
        return challengeToken;
    }

    @Transactional
    public TwoFactorVerifyLoginResponse verify(String challengeToken, String code, HttpServletResponse response) {
        TwoFactorLoginChallenge challenge = challengeRepository.findByToken(challengeToken)
            .orElseThrow(() -> new RuntimeException("Challenge 2FA invalido"));

        if (challenge.isUsed() || challenge.isExpired()) {
            throw new RuntimeException("Challenge 2FA expirado");
        }

        User user = challenge.getUser();
        if (!user.isTwoFactorAuthEnabled()) {
            throw new RuntimeException("2FA nao esta ativo para este usuario");
        }

        String secret = cryptoService.decrypt(user.getTwoFactorSecretEncrypted());
        boolean validTotp = totpService.validateCode(secret, code);
        boolean validRecoveryCode = recoveryCodeService.consume(user, code);

        if (!validTotp && !validRecoveryCode) {
            throw new RuntimeException("Codigo 2FA invalido");
        }

        KeycloakTokenResponse tokenResponse = new KeycloakTokenResponse();
        tokenResponse.setAccessToken(cryptoService.decrypt(challenge.getAccessTokenEncrypted()));
        tokenResponse.setRefreshToken(cryptoService.decrypt(challenge.getRefreshTokenEncrypted()));
        tokenResponse.setExpiresIn(challenge.getAccessTokenExpiresIn());
        tokenResponse.setRefreshExpiresIn(challenge.getRefreshTokenExpiresIn());

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createAccessTokenCookie(tokenResponse).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRefreshTokenCookie(tokenResponse, challenge.isRememberMe()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createRememberMeCookie(challenge.isRememberMe()).toString());

        challenge.markUsed();
        challengeRepository.save(challenge);

        return new TwoFactorVerifyLoginResponse(true);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}