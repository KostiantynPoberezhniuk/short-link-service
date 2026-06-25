package org.example.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.short-link")
public record ShortLinkProperties(
        String baseUrl,
        int codeMinLength,
        int codeMaxLength,
        int defaultTtlDays
) {
}
