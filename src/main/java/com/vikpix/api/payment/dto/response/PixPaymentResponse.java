package com.vikpix.api.payment.dto.response;

public record PixPaymentResponse(
    String providerPaymentId,
    String status,
    String qrCode,
    String qrCodeBase64,
    String ticketUrl
) {
}
