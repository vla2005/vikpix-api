package com.vikpix.api.payment.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.payment.services.MercadoPagoWebhookService;

@RestController
@RequestMapping("/api/payments/mercadopago/webhook")
public class MercadoPagoWebhookController {

    private final MercadoPagoWebhookService mercadoPagoWebhookService;

    public MercadoPagoWebhookController(MercadoPagoWebhookService mercadoPagoWebhookService) {
        this.mercadoPagoWebhookService = mercadoPagoWebhookService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
        @RequestBody(required = false) Map<String, Object> body,
        @RequestParam Map<String, String> queryParams
    ) {
        mercadoPagoWebhookService.execute(body, queryParams);
        return ResponseEntity.ok().build();
    }
}
