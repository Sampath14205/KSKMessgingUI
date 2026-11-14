package com.ksk.portal.model;

import jakarta.validation.constraints.NotBlank;

public record SendTextRequest(
        @NotBlank String to,
        @NotBlank String body,
        @NotBlank String businessId,
        Boolean previewUrl
) {
}
