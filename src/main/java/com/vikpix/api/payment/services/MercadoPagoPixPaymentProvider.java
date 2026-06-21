package com.vikpix.api.payment.services;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.vikpix.api.payment.dto.request.CreatePixPaymentCommand;
import com.vikpix.api.payment.dto.response.PixPaymentResponse;
import com.vikpix.api.payment.provider.PixPaymentProvider;

@Service
public class MercadoPagoPixPaymentProvider implements PixPaymentProvider {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    @Override
    public PixPaymentResponse createPixPayment(CreatePixPaymentCommand command) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("MERCADO_PAGO_ACCESS_TOKEN nao configurado");
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            PaymentClient client = new PaymentClient();

            PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(BigDecimal.valueOf(command.amountCents()).movePointLeft(2))
                .description(command.description())
                .paymentMethodId("pix")
                .externalReference(command.donationUuid().toString())
                .notificationUrl(notificationUrl)
                .payer(PaymentPayerRequest.builder()
                    .email(resolvePayerEmail(command))
                    .firstName(resolvePayerName(command))
                    .build())
                .build();

            com.mercadopago.resources.payment.Payment mpPayment = client.create(request);
            var transactionData = mpPayment.getPointOfInteraction().getTransactionData();

            return new PixPaymentResponse(
                mpPayment.getId().toString(),
                mpPayment.getStatus(),
                transactionData.getQrCode(),
                transactionData.getQrCodeBase64(),
                transactionData.getTicketUrl()
            );
        } catch (MPApiException exception) {
            throw new RuntimeException("Erro ao criar pagamento Pix no Mercado Pago: " + exception.getApiResponse().getContent(), exception);
        } catch (MPException exception) {
            throw new RuntimeException("Erro ao criar pagamento Pix no Mercado Pago: " + exception.getMessage(), exception);
        }
    }

    private String resolvePayerName(CreatePixPaymentCommand command) {
        if (command.payerName() == null || command.payerName().isBlank()) {
            return "Viewer";
        }

        return command.payerName();
    }

    private String resolvePayerEmail(CreatePixPaymentCommand command) {
        return "viewer-" + command.donationUuid() + "@example.com";
    }
}
