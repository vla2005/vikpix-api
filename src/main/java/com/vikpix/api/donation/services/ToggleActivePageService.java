package com.vikpix.api.donation.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.services.CurrentUserService;
import com.vikpix.api.donation.entities.DonationConfigs;
import com.vikpix.api.donation.repositories.DonationConfigRepository;
import com.vikpix.api.users.entities.User;

@Service
public class ToggleActivePageService {
    private final DonationConfigRepository donationConfigRepository;
    private final CurrentUserService currentUserService;

    public ToggleActivePageService(
        DonationConfigRepository donationConfigRepository,
        CurrentUserService currentUserService
    ){
        this.donationConfigRepository = donationConfigRepository;
        this.currentUserService = currentUserService;
    }

    public void execute(Authentication authentication, boolean active){
        User user = currentUserService.getAuthenticatedUser(authentication);

        DonationConfigs donationConfigs = donationConfigRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new RuntimeException("Configuracoes nao encontradas"));

        if (!donationConfigs.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Voce nao tem permissa para alterar essas configuracoes");
        }

        donationConfigs.setActive(active);
        donationConfigRepository.save(donationConfigs);


    }
}
