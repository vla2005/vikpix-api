package com.vikpix.api.donation.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.donation.dto.response.DonationStatusResponse;
import com.vikpix.api.donation.repositories.DonationRepository;
import com.vikpix.api.payment.repositories.PaymentRepository;

@Service
public class GetDonationStatusService {
    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;

    public GetDonationStatusService(
        DonationRepository donationRepository,
        PaymentRepository paymentRepository
    ) {
        this.donationRepository = donationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public DonationStatusResponse execute(UUID donationId) {
        var donation = donationRepository.findByUuid(donationId)
            .orElseThrow(() -> new RuntimeException("Doacao nao encontrada"));

        var payment = paymentRepository.findByDonation_Uuid(donationId)
            .orElseThrow(() -> new RuntimeException("Pagamento nao encontrado"));

        return new DonationStatusResponse(
            donation.getUuid(),
            donation.getStatus().name(),
            payment.getStatus().name()
        );
    }
}