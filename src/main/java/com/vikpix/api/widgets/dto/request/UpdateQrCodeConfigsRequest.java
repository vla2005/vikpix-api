package com.vikpix.api.widgets.dto.request;

public record UpdateQrCodeConfigsRequest(
    String primaryColor,
    String secondaryColor,
    boolean showLink,
    boolean showMessage,
    String message
) {

}
