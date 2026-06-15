package com.vikpix.api.widgets.dto.response;

import java.util.UUID;

public record QrCodeResponse(
    UUID id,
    UUID token,
    boolean active,
    String primaryColor,
    String secondaryColor,
    boolean showLink,
    boolean showMessage,
    String message
) {
}
