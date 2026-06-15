package com.vikpix.api.auth.services;

import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyValidator {
    private static final int MIN_LENGTH = 8;
    private static final String POLICY_MESSAGE = "A senha deve ter pelo menos 8 caracteres, 1 letra maiuscula, 1 letra minuscula, 1 numero e 1 caractere especial";

    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new RuntimeException(POLICY_MESSAGE);
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char character : password.toCharArray()) {
            if (Character.isUpperCase(character)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(character)) {
                hasLowercase = true;
            } else if (Character.isDigit(character)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
            throw new RuntimeException(POLICY_MESSAGE);
        }
    }
}