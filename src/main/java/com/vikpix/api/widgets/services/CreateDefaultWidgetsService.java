package com.vikpix.api.widgets.services;

import org.springframework.stereotype.Service;

import com.vikpix.api.users.entities.User;
import com.vikpix.api.widgets.entities.QrCodeConfig;
import com.vikpix.api.widgets.entities.Widget;
import com.vikpix.api.widgets.enums.WidgetType;
import com.vikpix.api.widgets.repository.QrCodeConfigRepository;
import com.vikpix.api.widgets.repository.WidgetRepository;

@Service
public class CreateDefaultWidgetsService {
    private final WidgetRepository widgetRepository;
    private final QrCodeConfigRepository qrCodeConfigRepository;

    public CreateDefaultWidgetsService(
        WidgetRepository widgetRepository,
        QrCodeConfigRepository qrCodeConfigRepository
    ) {
        this.widgetRepository = widgetRepository;
        this.qrCodeConfigRepository = qrCodeConfigRepository;
    }

    public void execute(User user) {
        Widget qrcodeWidget = Widget.builder()
            .user(user)
            .type(WidgetType.qrcode)
            .active(true)
            .build();

        Widget savedQrcodeWidget = widgetRepository.save(qrcodeWidget);

        QrCodeConfig qrCodeConfig = QrCodeConfig.builder()
            .widget(savedQrcodeWidget)
            .primaryColor("#1db8ce")
            .secondaryColor("#FFFFFF")
            .showLink(true)
            .showMessage(true)
            .message("Aponte a câmera do celular")
            .build();

        qrCodeConfigRepository.save(qrCodeConfig);
    }

}
