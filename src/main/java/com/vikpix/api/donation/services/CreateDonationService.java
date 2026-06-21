package com.vikpix.api.donation.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.donation.dto.request.CreateDonationRequest;
import com.vikpix.api.donation.dto.response.CreateDonationResponse;
import com.vikpix.api.donation.entities.Donation;
import com.vikpix.api.donation.entities.DonationConfigs;
import com.vikpix.api.donation.enums.DonationStatus;
import com.vikpix.api.donation.repositories.DonationConfigRepository;
import com.vikpix.api.donation.repositories.DonationRepository;
import com.vikpix.api.payment.dto.request.CreatePixPaymentCommand;
import com.vikpix.api.payment.entities.Payment;
import com.vikpix.api.payment.enums.PaymentStatus;
import com.vikpix.api.payment.provider.PixPaymentProvider;
import com.vikpix.api.payment.repositories.PaymentRepository;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;

@Service
public class CreateDonationService {
    private static final String PAYMENT_PROVIDER_MERCADO_PAGO = "MERCADO_PAGO";

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final DonationConfigRepository donationConfigRepository;
    private final PixPaymentProvider pixPaymentProvider;
    private final PaymentRepository paymentRepository;

    public CreateDonationService(
        DonationRepository donationRepository,
        UserRepository userRepository,
        DonationConfigRepository donationConfigRepository,
        PixPaymentProvider pixPaymentProvider,
        PaymentRepository paymentRepository
    ) {
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
        this.donationConfigRepository = donationConfigRepository;
        this.pixPaymentProvider = pixPaymentProvider;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public CreateDonationResponse execute(CreateDonationRequest request, String donorIp) {
        User user = userRepository.findByUuid(request.userId())
            .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        DonationConfigs donationConfigs = donationConfigRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new RuntimeException("Configuracoes nao encontradas"));

        if (!donationConfigs.isActive()) {
            throw new RuntimeException("Pagina de doacao desativada");
        }

        if (request.amountCents() == null || request.amountCents() < donationConfigs.getMinCents()) {
            throw new RuntimeException("Valor e menor que o minimo");
        }

        Donation donation = Donation.builder()
            .user(user)
            .donorIp(donorIp)
            .donorName(request.donorName())
            .amountCents(request.amountCents())
            .message(request.message())
            .status(DonationStatus.PENDING)
            .build();

        donationRepository.save(donation);

        var pix = pixPaymentProvider.createPixPayment(new CreatePixPaymentCommand(
            donation.getUuid(),
            request.amountCents(),
            "Doacao para " + user.getUserName(),
            request.donorName()
        ));

        Payment payment = Payment.builder()
            .donation(donation)
            .provider(PAYMENT_PROVIDER_MERCADO_PAGO)
            .providerPaymentId(pix.providerPaymentId())
            .status(PaymentStatus.PENDING)
            .amountCents(request.amountCents())
            .qrCode(pix.qrCode())
            .qrCodeBase64(pix.qrCodeBase64())
            .ticketUrl(pix.ticketUrl())
            .build();

        paymentRepository.save(payment);

        return new CreateDonationResponse(
            donation.getUuid(),
            payment.getUuid(),
            donation.getStatus().name(),
            new CreateDonationResponse.PixResponse(
                pix.qrCode(),
                pix.qrCodeBase64(),
                pix.ticketUrl()
            )
        );
    }
}
