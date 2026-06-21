package com.vikpix.api.payment.dto.request;

import java.util.UUID;

public record CreatePixPaymentCommand(
    UUID donationUuid,
    Integer amountCents,
    String description,
    String payerName
) {
}