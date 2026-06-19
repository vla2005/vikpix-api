package com.vikpix.api.donation.services;

import org.springframework.stereotype.Service;

import com.vikpix.api.donation.entities.DonationConfigs;
import com.vikpix.api.donation.repositories.DonationConfigRepository;
import com.vikpix.api.users.entities.User;

@Service
public class CreateDefaultDonationConfigsService {
    private final DonationConfigRepository donationConfigRepository;

    public CreateDefaultDonationConfigsService(DonationConfigRepository donationConfigRepository) {
        this.donationConfigRepository = donationConfigRepository;
    }

    public void execute(User user) {
        DonationConfigs donationConfigs = DonationConfigs.builder()
            .user(user)
            .active(true)
            .minCents(200)
            .mainColor("#1db8ce")
            .build();

        donationConfigRepository.save(donationConfigs);
    }

}
