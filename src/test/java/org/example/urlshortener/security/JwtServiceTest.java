package org.example.urlshortener.security;

import org.example.urlshortener.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-1234567890";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 3_600_000L));

    @Test
    void generatesTokenAndExtractsSubject() {
        String token = jwtService.generateToken("alice");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.getExpirationMs()).isEqualTo(3_600_000L);
    }

    @Test
    void rejectsMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-real-token")).isFalse();
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        JwtService other = new JwtService(
                new JwtProperties("another-secret-another-secret-1234567890", 3_600_000L));
        String token = other.generateToken("bob");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void treatsAlreadyExpiredTokenAsInvalid() {
        JwtService expiring = new JwtService(new JwtProperties(SECRET, -1_000L));
        String token = expiring.generateToken("carol");

        assertThat(expiring.isTokenValid(token)).isFalse();
    }
}
