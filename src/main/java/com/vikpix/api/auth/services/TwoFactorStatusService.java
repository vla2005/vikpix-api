package com.vikpix.api.auth.services;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.vikpix.api.auth.dto.response.TwoFactorStatusResponse;
import com.vikpix.api.users.entities.User;
@Service
public class TwoFactorStatusService {
    private final CurrentUserService currentUserService;
    public TwoFactorStatusService(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }
    public TwoFactorStatusResponse execute(Authentication authentication) {
        User user = currentUserService.getAuthenticatedUser(authentication);
        return new TwoFactorStatusResponse(user.isTwoFactorAuthEnabled());
    }
}