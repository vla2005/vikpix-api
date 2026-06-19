package com.vikpix.api.widgets.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.services.CurrentUserService;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.widgets.dto.request.UpdateQrCodeConfigsRequest;
import com.vikpix.api.widgets.entities.QrCodeConfig;
import com.vikpix.api.widgets.entities.Widget;
import com.vikpix.api.widgets.enums.WidgetType;
import com.vikpix.api.widgets.repository.QrCodeConfigRepository;
import com.vikpix.api.widgets.repository.WidgetRepository;

@Service
public class UpdateQrCodeConfigsService {
    private final QrCodeConfigRepository qrCodeConfigRepository;
    private final CurrentUserService currentUserService;
    private final WidgetRepository widgetRepository;

    public UpdateQrCodeConfigsService(
            QrCodeConfigRepository qrCodeConfigRepository,
            CurrentUserService currentUserService,
            WidgetRepository widgetRepository) {
        this.qrCodeConfigRepository = qrCodeConfigRepository;
        this.currentUserService = currentUserService;
        this.widgetRepository = widgetRepository;
    }

    public void execute(Authentication authentication, UpdateQrCodeConfigsRequest request) {
        User user = currentUserService.getAuthenticatedUser(authentication);

        Widget widget = widgetRepository.findByUser_IdAndType(user.getId(), WidgetType.qrcode)
                .orElseThrow(() -> new RuntimeException("QR Code nao encontrado"));

        if (!widget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Voce nao tem permissao para alterar este widget");
        }

        QrCodeConfig config = qrCodeConfigRepository.findByWidget(widget)
                .orElseThrow(() -> new RuntimeException("Configuracao do QR Code nao encontrada"));

        config.setPrimaryColor(request.primaryColor());
        config.setSecondaryColor(request.secondaryColor());
        config.setShowLink(request.showLink());
        config.setShowMessage(request.showMessage());
        config.setMessage(request.message());

        qrCodeConfigRepository.save(config);
    }

}
