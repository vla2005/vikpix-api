package com.vikpix.api.payment.provider;

import com.vikpix.api.payment.dto.request.CreatePixPaymentCommand;
import com.vikpix.api.payment.dto.response.PixPaymentResponse;

public interface PixPaymentProvider {
    PixPaymentResponse createPixPayment(CreatePixPaymentCommand command);
}
