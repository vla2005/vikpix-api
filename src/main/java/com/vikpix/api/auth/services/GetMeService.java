package com.vikpix.api.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.dto.response.MeResponse;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;

@Service
public class GetMeService {
    @Autowired
    private UserRepository userRepository;

    public MeResponse execute(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado nao encontrado"));

        return new MeResponse(
            user.getUuid(),
            user.getName(),
            user.getUserName(),
            user.getEmail(),
            user.getCpf(),
            user.getPhone(),
            user.getAvatarUrl(),
            user.isTwoFactorAuthEnabled(),
            user.getOnboardedAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
