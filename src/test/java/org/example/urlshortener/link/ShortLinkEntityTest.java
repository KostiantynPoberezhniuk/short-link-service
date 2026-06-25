package org.example.urlshortener.link;

import org.example.urlshortener.link.dto.ShortLinkResponse;
import org.example.urlshortener.user.Role;
import org.example.urlshortener.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ShortLinkEntityTest {

    private final User owner = new User("alice", "pwd", Role.USER);

    @Test
    void notExpiredWhenExpiryInFuture() {
        ShortLink link = new ShortLink("abc123", "https://example.com",
                Instant.now().plus(1, ChronoUnit.DAYS), owner);

        assertThat(link.isExpired()).isFalse();
    }

    @Test
    void expiredWhenExpiryInPast() {
        ShortLink link = new ShortLink("abc123", "https://example.com",
                Instant.now().minus(1, ChronoUnit.DAYS), owner);

        assertThat(link.isExpired()).isTrue();
    }

    @Test
    void incrementVisitCountIncreasesValue() {
        ShortLink link = new ShortLink("abc123", "https://example.com",
                Instant.now().plus(1, ChronoUnit.DAYS), owner);

        link.incrementVisitCount();
        link.incrementVisitCount();

        assertThat(link.getVisitCount()).isEqualTo(2L);
    }

    @Test
    void equalsBasedOnSameInstanceForTransientEntities() {
        ShortLink link = new ShortLink("abc123", "https://example.com",
                Instant.now().plus(1, ChronoUnit.DAYS), owner);

        assertThat(link).isEqualTo(link);
        assertThat(link).isNotEqualTo(new Object());
        assertThat(link.hashCode()).isEqualTo(ShortLink.class.hashCode());
    }

    @Test
    void responseMapsExpiredLinkAsInactive() {
        ShortLink link = new ShortLink("abc123", "https://example.com",
                Instant.now().minus(1, ChronoUnit.DAYS), owner);

        ShortLinkResponse response = ShortLinkResponse.from(link, "http://localhost:8080");

        assertThat(response.active()).isFalse();
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/r/abc123");
    }
}
