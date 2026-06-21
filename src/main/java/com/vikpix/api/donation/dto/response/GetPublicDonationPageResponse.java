package com.vikpix.api.donation.dto.response;

import java.util.UUID;

public record GetPublicDonationPageResponse(
    String userName,
    UUID id, 
    String avatarUrl,
    Boolean active,
    String mainColor,
    Integer minCents
) {

}
