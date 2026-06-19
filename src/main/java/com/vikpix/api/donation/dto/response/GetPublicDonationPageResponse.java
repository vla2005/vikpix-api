package com.vikpix.api.donation.dto.response;

public record GetPublicDonationPageResponse(
    String userName,
    String avatarUrl,
    Boolean active,
    String mainColor,
    Integer minCents
) {

}
