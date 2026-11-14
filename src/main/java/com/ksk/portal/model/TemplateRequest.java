package com.ksk.portal.model;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TemplateRequest(
        @NotBlank String to,
        @NotBlank String templateName,
        @NotBlank String languageCode,
        List<String> bodyParameters
) {}
