package org.example.urlshortener.link.dto;

import org.example.urlshortener.link.ShortLink;

import java.time.Instant;

public record ShortLinkResponse(
        Long id,
        String shortCode,
        String shortUrl,
        String originalUrl,
        Instant createdAt,
        Instant expiresAt,
        boolean active,
        long visitCount,
        String owner
) {

    public static ShortLinkResponse from(ShortLink link, String baseUrl) {
        String shortUrl = baseUrl + "/r/" + link.getShortCode();
        return new ShortLinkResponse(
                link.getId(),
                link.getShortCode(),
                shortUrl,
                link.getOriginalUrl(),
                link.getCreatedAt(),
                link.getExpiresAt(),
                !link.isExpired(),
                link.getVisitCount(),
                link.getOwner().getUsername());
    }
}
