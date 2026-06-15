package com.vikpix.api.widgets.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.services.CurrentUserService;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.widgets.dto.response.QrCodeResponse;
import com.vikpix.api.widgets.entities.QrCodeConfig;
import com.vikpix.api.widgets.entities.Widget;
import com.vikpix.api.widgets.enums.WidgetType;
import com.vikpix.api.widgets.repository.QrCodeConfigRepository;
import com.vikpix.api.widgets.repository.WidgetRepository;

@Service
public class GetQrCodeService {
    private final WidgetRepository widgetRepository;
    private final QrCodeConfigRepository qrCodeConfigRepository;
    private final CurrentUserService currentUserService;

    public GetQrCodeService(
        WidgetRepository widgetRepository,
        QrCodeConfigRepository qrCodeConfigRepository,
        CurrentUserService currentUserService
    ) {
        this.widgetRepository = widgetRepository;
        this.qrCodeConfigRepository = qrCodeConfigRepository;
        this.currentUserService = currentUserService;
    }

    public QrCodeResponse execute(Authentication authentication) {
        User user = currentUserService.getAuthenticatedUser(authentication);

        Widget qrcodeWidget = widgetRepository.findByUser_IdAndType(user.getId(), WidgetType.qrcode)
            .orElseThrow(() -> new RuntimeException("QR Code nao encontrado"));

        QrCodeConfig config = qrCodeConfigRepository.findByWidget(qrcodeWidget)
            .orElseThrow(() -> new RuntimeException("Configuracao do QR Code nao encontrada"));

        return new QrCodeResponse(
            qrcodeWidget.getUuid(),
            qrcodeWidget.getToken(),
            qrcodeWidget.isActive(),
            config.getPrimaryColor(),
            config.getSecondaryColor(),
            config.isShowLink(),
            config.isShowMessage(),
            config.getMessage()
        );
    }
}
