package com.vikpix.api.widgets.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vikpix.api.widgets.dto.response.QrCodePublicResponse;
import com.vikpix.api.widgets.entities.QrCodeConfig;
import com.vikpix.api.widgets.entities.Widget;
import com.vikpix.api.widgets.repository.QrCodeConfigRepository;
import com.vikpix.api.widgets.repository.WidgetRepository;

@Service
public class GetQrCodeByTokenService {
    private final WidgetRepository widgetRepository;
    private final QrCodeConfigRepository qrCodeConfigRepository;

    public GetQrCodeByTokenService(
        WidgetRepository widgetRepository,
        QrCodeConfigRepository qrCodeConfigRepository
    ) {
        this.widgetRepository = widgetRepository;
        this.qrCodeConfigRepository = qrCodeConfigRepository;
    }

    public QrCodePublicResponse execute(UUID token) {
        Widget qrcodeWidget = widgetRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("QR Code nao encontrado"));

        QrCodeConfig config = qrCodeConfigRepository.findByWidget(qrcodeWidget)
            .orElseThrow(() -> new RuntimeException("Configuracao do QR Code nao encontrada"));

        return new QrCodePublicResponse(
            qrcodeWidget.getToken(),
            qrcodeWidget.getUser().getUserName(),
            qrcodeWidget.isActive(),
            config.getPrimaryColor(),
            config.getSecondaryColor(),
            config.isShowLink(),
            config.isShowMessage(),
            config.getMessage()
        );
    }

}
