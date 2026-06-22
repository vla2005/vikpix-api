package com.vikpix.api.donation.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.donation.dto.request.CreateDonationRequest;
import com.vikpix.api.donation.dto.response.CreateDonationResponse;
import com.vikpix.api.donation.dto.response.DonationStatusResponse;
import com.vikpix.api.donation.services.CreateDonationService;
import com.vikpix.api.donation.services.GetDonationStatusService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/donation")
public class DonationController {
    private final CreateDonationService createDonationService;
    private final GetDonationStatusService getDonationStatusService;

    public DonationController(
        CreateDonationService createDonationService,
        GetDonationStatusService getDonationStatusService
    ) {
        this.createDonationService = createDonationService;
        this.getDonationStatusService = getDonationStatusService;
    }

    @PostMapping
    public ResponseEntity<CreateDonationResponse> createDonation(
        @Valid @RequestBody CreateDonationRequest request,
        HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(createDonationService.execute(request, getClientIp(httpServletRequest)));
    }

    @GetMapping("/{donationId}/status")
    public ResponseEntity<DonationStatusResponse> getDonationStatus(@PathVariable UUID donationId) {
        return ResponseEntity.ok(getDonationStatusService.execute(donationId));
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