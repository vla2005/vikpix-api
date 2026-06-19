package com.vikpix.api.donation.dto.request;

public record UpdateDonationConfigsRequest(
    String mainColor,
    Integer minCents
) {

}
