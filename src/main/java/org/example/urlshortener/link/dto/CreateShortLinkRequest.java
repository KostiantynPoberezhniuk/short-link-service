package org.example.urlshortener.link.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateShortLinkRequest(
        @NotBlank(message = "Original URL must not be blank")
        @Size(max = 2048, message = "Original URL must not exceed 2048 characters")
        String originalUrl,

        @Positive(message = "Time-to-live must be a positive number of days")
        Integer ttlDays
) {
}
