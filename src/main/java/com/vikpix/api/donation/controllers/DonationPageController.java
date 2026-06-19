package com.vikpix.api.donation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.donation.dto.request.UpdateDonationConfigsRequest;
import com.vikpix.api.donation.dto.response.GetPublicDonationPageResponse;
import com.vikpix.api.donation.services.GetDonationPageConfigsService;
import com.vikpix.api.donation.services.UpdateDonateConfigsService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("api/donation-page")
public class DonationPageController {
    private final GetDonationPageConfigsService getDonationPageConfigsService;
    private final UpdateDonateConfigsService updateDonateConfigsService;

    public DonationPageController(
        GetDonationPageConfigsService getDonationPageConfigsService,
        UpdateDonateConfigsService updateDonateConfigsService) {
            this.getDonationPageConfigsService = getDonationPageConfigsService;
            this.updateDonateConfigsService = updateDonateConfigsService;
    }

    @GetMapping("/{userName}")
    public GetPublicDonationPageResponse getConfigs(@PathVariable String userName) {
        return getDonationPageConfigsService.execute(userName);
    }

    @PutMapping
    public ResponseEntity<Void> updateDonateConfigs(
        Authentication authentication,
        @Valid @RequestBody UpdateDonationConfigsRequest request)
        {
            updateDonateConfigsService.execute(authentication, request);
            return ResponseEntity.ok().build();
        }
    

}
