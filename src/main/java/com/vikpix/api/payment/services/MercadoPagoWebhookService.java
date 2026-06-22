package com.vikpix.api.payment.services;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.vikpix.api.donation.enums.DonationStatus;
import com.vikpix.api.payment.enums.PaymentStatus;
import com.vikpix.api.payment.repositories.PaymentRepository;

@Service
public class MercadoPagoWebhookService {
    private final PaymentRepository paymentRepository;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    public MercadoPagoWebhookService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void execute(Map<String, Object> body, Map<String, String> queryParams) {
        String mercadoPagoPaymentId = extractPaymentId(body, queryParams);

        if (mercadoPagoPaymentId == null || mercadoPagoPaymentId.isBlank()) {
            return;
        }

        com.mercadopago.resources.payment.Payment mercadoPagoPayment = getMercadoPagoPayment(mercadoPagoPaymentId);
        String providerPaymentId = mercadoPagoPayment.getId().toString();

        var paymentOptional = paymentRepository.findByProviderPaymentId(providerPaymentId);

        if (paymentOptional.isEmpty()) {
            return;
        }

        var payment = paymentOptional.get();
        String mercadoPagoStatus = mercadoPagoPayment.getStatus();

        payment.setProviderStatus(mercadoPagoStatus);
        payment.setLastWebhookAt(LocalDateTime.now());

        if (PaymentStatus.PAID.equals(payment.getStatus())) {
            paymentRepository.save(payment);
            return;
        }

        if ("approved".equals(mercadoPagoStatus)) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            payment.getDonation().setStatus(DonationStatus.PAID);
            paymentRepository.save(payment);
            return;
        }

        if ("rejected".equals(mercadoPagoStatus)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.getDonation().setStatus(DonationStatus.REJECTED);
            paymentRepository.save(payment);
            return;
        }

        if ("cancelled".equals(mercadoPagoStatus) || "canceled".equals(mercadoPagoStatus)) {
            payment.setStatus(PaymentStatus.CANCELED);
            payment.getDonation().setStatus(DonationStatus.CANCELED);
            paymentRepository.save(payment);
            return;
        }

        if ("expired".equals(mercadoPagoStatus)) {
            payment.setStatus(PaymentStatus.EXPIRED);
            payment.getDonation().setStatus(DonationStatus.CANCELED);
            paymentRepository.save(payment);
            return;
        }

        paymentRepository.save(payment);
    }

    private com.mercadopago.resources.payment.Payment getMercadoPagoPayment(String paymentId) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("MERCADO_PAGO_ACCESS_TOKEN nao configurado");
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            return client.get(Long.valueOf(paymentId));
        } catch (MPApiException exception) {
            throw new RuntimeException("Erro ao consultar pagamento no Mercado Pago: " + exception.getApiResponse().getContent(), exception);
        } catch (MPException exception) {
            throw new RuntimeException("Erro ao consultar pagamento no Mercado Pago: " + exception.getMessage(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractPaymentId(Map<String, Object> body, Map<String, String> queryParams) {
        if (body != null) {
            Object data = body.get("data");

            if (data instanceof Map<?, ?> dataMap) {
                Object id = ((Map<String, Object>) dataMap).get("id");

                if (id != null) {
                    return id.toString();
                }
            }

            Object id = body.get("id");

            if (id != null) {
                return id.toString();
            }

            Object resource = body.get("resource");

            if (resource != null) {
                String resourceText = resource.toString();
                int lastSlashIndex = resourceText.lastIndexOf('/');

                if (lastSlashIndex >= 0 && lastSlashIndex < resourceText.length() - 1) {
                    return resourceText.substring(lastSlashIndex + 1);
                }
            }
        }

        if (queryParams != null) {
            String dataId = queryParams.get("data.id");

            if (dataId != null && !dataId.isBlank()) {
                return dataId;
            }

            String id = queryParams.get("id");

            if (id != null && !id.isBlank()) {
                return id;
            }
        }

        return null;
    }
}