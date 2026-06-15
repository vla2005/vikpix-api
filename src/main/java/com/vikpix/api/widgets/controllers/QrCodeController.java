package com.vikpix.api.widgets.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.widgets.dto.response.QrCodeResponse;
import com.vikpix.api.widgets.services.GetQrCodeService;

@RestController
@RequestMapping("/api/widgets/qrcode")
public class QrCodeController {
    private final GetQrCodeService getQrCodeService;

    public QrCodeController(GetQrCodeService getQrCodeService) {
        this.getQrCodeService = getQrCodeService;
    }

    @GetMapping
    public ResponseEntity<QrCodeResponse> execute(Authentication authentication) {
        return ResponseEntity.ok(getQrCodeService.execute(authentication));
    }
}
