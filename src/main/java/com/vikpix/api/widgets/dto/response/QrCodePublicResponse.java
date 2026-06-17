package com.vikpix.api.widgets.dto.response;

import java.util.UUID;

public record QrCodePublicResponse(
    UUID token,
    String username,
    boolean active,
    String primaryColor,
    String secondaryColor,
    boolean showLink,
    boolean showMessage,
    String message
) {
}
