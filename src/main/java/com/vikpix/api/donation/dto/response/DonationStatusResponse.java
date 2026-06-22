package com.vikpix.api.donation.dto.response;

import java.util.UUID;

public record DonationStatusResponse(
    UUID donationId,
    String donationStatus,
    String paymentStatus
) {
}