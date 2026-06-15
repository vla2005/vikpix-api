package com.vikpix.api.auth.services;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class TwoFactorCryptoService {
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec keySpec;
    public TwoFactorCryptoService(@Value("${app.security.encryption-key}") String encryptionKey) {
        this.keySpec = new SecretKeySpec(sha256(encryptionKey), "AES");
    }
    public String encrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(iv)
                + ":"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception exception) {
            throw new RuntimeException("Nao foi possivel criptografar dado sensivel", exception);
        }
    }
    public String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return null;
        }
        try {
            String[] parts = encryptedValue.split(":", 2);
            if (parts.length != 2) {
                throw new RuntimeException("Formato criptografado invalido");
            }
            byte[] iv = Base64.getUrlDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getUrlDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new RuntimeException("Nao foi possivel descriptografar dado sensivel", exception);
        }
    }
    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new RuntimeException("Nao foi possivel preparar chave de criptografia", exception);
        }
    }
}