package com.vikpix.api.donation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.donation.dto.request.CreateDonationRequest;
import com.vikpix.api.donation.dto.response.CreateDonationResponse;
import com.vikpix.api.donation.services.CreateDonationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/donation")
public class DonationController {
    private final CreateDonationService createDonationService;

    public DonationController(CreateDonationService createDonationService) {
        this.createDonationService = createDonationService;
    }

    @PostMapping
    public ResponseEntity<CreateDonationResponse> createDonation(
        @Valid @RequestBody CreateDonationRequest request,
        HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(createDonationService.execute(request, getClientIp(httpServletRequest)));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}