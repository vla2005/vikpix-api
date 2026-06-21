package com.vikpix.api.donation.dto.request;

import java.util.UUID;

public record CreateDonationRequest(
    UUID userId,
    String donorName,
    Integer amountCents,
    String message
) {
}