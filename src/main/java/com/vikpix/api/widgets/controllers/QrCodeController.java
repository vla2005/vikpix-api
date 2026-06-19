package com.vikpix.api.widgets.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vikpix.api.widgets.dto.request.UpdateQrCodeConfigsRequest;
import com.vikpix.api.widgets.dto.response.QrCodePublicResponse;
import com.vikpix.api.widgets.dto.response.QrCodeResponse;
import com.vikpix.api.widgets.services.GetQrCodeByTokenService;
import com.vikpix.api.widgets.services.GetQrCodeService;
import com.vikpix.api.widgets.services.UpdateQrCodeConfigsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/widgets/qrcode")
public class QrCodeController {
    private final GetQrCodeService getQrCodeService;
    private final GetQrCodeByTokenService getQrCodeByTokenService;
    private final UpdateQrCodeConfigsService updateQrCodeConfigsService;


    public QrCodeController(
        GetQrCodeService getQrCodeService,
        GetQrCodeByTokenService getQrCodeByTokenService,
        UpdateQrCodeConfigsService updateQrCodeConfigsService
        ) {
        this.getQrCodeService = getQrCodeService;
        this.getQrCodeByTokenService = getQrCodeByTokenService;
        this.updateQrCodeConfigsService = updateQrCodeConfigsService;
    }

    @GetMapping
    public ResponseEntity<QrCodeResponse> execute(Authentication authentication) {
        return ResponseEntity.ok(getQrCodeService.execute(authentication));
    }

    @GetMapping("/{token}")
    public ResponseEntity<QrCodePublicResponse> execute(@PathVariable UUID token){
        return ResponseEntity.ok(getQrCodeByTokenService.execute(token));
    }

    @PutMapping()
    public ResponseEntity<Void> updateQrCode(
        Authentication authentication,
        @Valid @RequestBody UpdateQrCodeConfigsRequest request)
        {
            updateQrCodeConfigsService.execute(authentication, request);
            return ResponseEntity.ok().build();
        }
}
