package com.vikpix.api.auth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.auth.dto.request.LoginRequest;
import com.vikpix.api.auth.dto.response.LoginResponse;
import com.vikpix.api.auth.dto.response.MeResponse;
import com.vikpix.api.auth.jwt.JwtUtils;
import com.vikpix.api.auth.services.GetMeService;
import com.vikpix.api.auth.services.RefreshTokenService;
import com.vikpix.api.auth.services.RefreshTokenService.IssuedRefreshToken;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    GetMeService getMeService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(loginRequest.email())
            .orElseThrow(() -> new RuntimeException("Usuario autenticado nao encontrado"));
        IssuedRefreshToken refreshToken = refreshTokenService.createForUser(user, loginRequest.shouldRemember());
        ResponseCookie refreshCookie = refreshTokenService.buildRefreshCookie(refreshToken);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(new LoginResponse(accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        String rawRefreshToken = refreshTokenService.extractRefreshToken(request);
        IssuedRefreshToken refreshToken = refreshTokenService.rotate(rawRefreshToken);
        String accessToken = jwtUtils.generateJwtTokenFromEmail(refreshToken.user().getEmail());
        ResponseCookie refreshCookie = refreshTokenService.buildRefreshCookie(refreshToken);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(new LoginResponse(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String rawRefreshToken = refreshTokenService.extractRefreshToken(request);
        refreshTokenService.revoke(rawRefreshToken);
        SecurityContextHolder.clearContext();
        ResponseCookie clearCookie = refreshTokenService.buildClearRefreshCookie();

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
            .build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> getAuthenticatedUser(Authentication authentication) {
        return ResponseEntity.ok(getMeService.execute(authentication));
    }
}

