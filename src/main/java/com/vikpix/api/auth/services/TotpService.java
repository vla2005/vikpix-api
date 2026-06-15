package com.vikpix.api.auth.services;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
@Service
public class TotpService {
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_BYTES = 20;
    private static final int CODE_DIGITS = 6;
    private static final int PERIOD_SECONDS = 30;
    private static final int WINDOW = 1;
    private final SecureRandom secureRandom = new SecureRandom();
    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(bytes);
        return encodeBase32(bytes);
    }
    public String createOtpAuthUrl(String issuer, String accountName, String secret) {
        String label = urlEncode(issuer + ":" + accountName);
        return "otpauth://totp/" + label
            + "?secret=" + urlEncode(secret)
            + "&issuer=" + urlEncode(issuer)
            + "&algorithm=SHA1&digits=" + CODE_DIGITS
            + "&period=" + PERIOD_SECONDS;
    }
    public String createQrCodeDataUrl(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 260, 260);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(MatrixToImageWriter.toBufferedImage(matrix), "PNG", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException("Nao foi possivel gerar QR Code do 2FA", exception);
        }
    }
    public boolean validateCode(String secret, String code) {
        if (secret == null || secret.isBlank() || code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long currentCounter = Instant.now().getEpochSecond() / PERIOD_SECONDS;
        for (int offset = -WINDOW; offset <= WINDOW; offset++) {
            if (generateCode(secret, currentCounter + offset).equals(code)) {
                return true;
            }
        }
        return false;
    }
    private String generateCode(String secret, long counter) {
        try {
            byte[] key = decodeBase32(secret);
            byte[] data = new byte[8];
            long value = counter;
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (value & 0xff);
                value >>= 8;
            }
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (Exception exception) {
            throw new RuntimeException("Nao foi possivel validar codigo TOTP", exception);
        }
    }
    private String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte currentByte : data) {
            buffer = (buffer << 8) | (currentByte & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                result.append(BASE32_ALPHABET.charAt((buffer >> (bitsLeft - 5)) & 31));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            result.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 31));
        }
        return result.toString();
    }
    private byte[] decodeBase32(String value) {
        String normalized = value.replace("=", "").replace(" ", "").toUpperCase();
        byte[] bytes = new byte[normalized.length() * 5 / 8 + 5];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (char character : normalized.toCharArray()) {
            int digit = BASE32_ALPHABET.indexOf(character);
            if (digit < 0) {
                throw new IllegalArgumentException("Secret TOTP invalido");
            }
            buffer = (buffer << 5) | digit;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return Arrays.copyOf(bytes, index);
    }
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}