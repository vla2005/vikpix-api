package com.vikpix.api.donation.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.donation.dto.response.GetPublicDonationPageResponse;
import com.vikpix.api.donation.services.GetDonationPageConfigsService;


@RestController
@RequestMapping("api/donation-page")
public class DonationPageController {
    private final GetDonationPageConfigsService getDonationPageConfigsService;

    public DonationPageController(GetDonationPageConfigsService getDonationPageConfigsService) {
        this.getDonationPageConfigsService = getDonationPageConfigsService;
    }

    @GetMapping("/{userName}")
    public GetPublicDonationPageResponse getConfigs(@PathVariable String userName) {
        return getDonationPageConfigsService.execute(userName);
    }

}
