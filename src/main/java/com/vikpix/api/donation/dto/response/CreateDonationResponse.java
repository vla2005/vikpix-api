package com.vikpix.api.donation.dto.response;

import java.util.UUID;

public record CreateDonationResponse(
    UUID donationId,
    UUID paymentId,
    String status,
    PixResponse pix
) {
    public record PixResponse(
        String qrCode,
        String qrCodeBase64,
        String ticketUrl
    ) {
    }
}
