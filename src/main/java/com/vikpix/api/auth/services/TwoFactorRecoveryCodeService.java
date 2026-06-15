package com.vikpix.api.auth.services;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vikpix.api.auth.entities.TwoFactorRecoveryCode;
import com.vikpix.api.auth.repository.TwoFactorRecoveryCodeRepository;
import com.vikpix.api.users.entities.User;
@Service
public class TwoFactorRecoveryCodeService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int RECOVERY_CODE_COUNT = 10;
    private final SecureRandom secureRandom = new SecureRandom();
    private final TwoFactorRecoveryCodeRepository recoveryCodeRepository;
    private final PasswordEncoder passwordEncoder;
    public TwoFactorRecoveryCodeService(
        TwoFactorRecoveryCodeRepository recoveryCodeRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public List<String> regenerate(User user) {
        recoveryCodeRepository.deleteByUser(user);
        List<String> plainCodes = new ArrayList<>();
        List<TwoFactorRecoveryCode> entities = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
            String code = generateRecoveryCode();
            plainCodes.add(code);
            entities.add(new TwoFactorRecoveryCode(user, passwordEncoder.encode(normalize(code))));
        }
        recoveryCodeRepository.saveAll(entities);
        return plainCodes;
    }
    @Transactional
    public boolean consume(User user, String code) {
        String normalizedCode = normalize(code);
        if (normalizedCode.isBlank()) {
            return false;
        }
        List<TwoFactorRecoveryCode> activeCodes = recoveryCodeRepository.findByUserAndUsedAtIsNull(user);
        for (TwoFactorRecoveryCode recoveryCode : activeCodes) {
            if (passwordEncoder.matches(normalizedCode, recoveryCode.getCodeHash())) {
                recoveryCode.markUsed();
                recoveryCodeRepository.save(recoveryCode);
                return true;
            }
        }
        return false;
    }
    @Transactional
    public void deleteAll(User user) {
        recoveryCodeRepository.deleteByUser(user);
    }
    private String generateRecoveryCode() {
        return randomBlock(4) + "-" + randomBlock(4) + "-" + randomBlock(4);
    }
    private String randomBlock(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return result.toString();
    }
    private String normalize(String code) {
        return code == null ? "" : code.trim().replace(" ", "").toUpperCase();
    }
}