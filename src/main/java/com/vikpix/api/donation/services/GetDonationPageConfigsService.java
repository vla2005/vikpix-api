package com.vikpix.api.donation.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.donation.dto.response.GetPublicDonationPageResponse;
import com.vikpix.api.donation.entities.DonationConfigs;
import com.vikpix.api.donation.repositories.DonationConfigRepository;
import com.vikpix.api.users.entities.User;

@Service
public class GetDonationPageConfigsService {
    private final DonationConfigRepository donationConfigRepository;

    public GetDonationPageConfigsService(DonationConfigRepository donationConfigRepository) {
        this.donationConfigRepository = donationConfigRepository;
    }

    @Transactional(readOnly = true)
    public GetPublicDonationPageResponse execute(String userName){
        DonationConfigs donationConfigs = donationConfigRepository.findByUser_UserName(userName)
            .orElseThrow(() -> new RuntimeException("Configuracoes de doacao nao encontradas"));

        User user = donationConfigs.getUser();

        return new GetPublicDonationPageResponse(
            user.getUserName(),
            user.getUuid(),
            user.getAvatarUrl(),
            donationConfigs.isActive(),
            donationConfigs.getMainColor(),
            donationConfigs.getMinCents()
        );
    }
}
