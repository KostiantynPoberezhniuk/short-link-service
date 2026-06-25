package org.example.urlshortener.link;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class UrlValidatorTest {

    private final UrlValidator validator = new UrlValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com/path?query=1",
            "https://sub.domain.example.org:8443/a/b"
    })
    void acceptsValidHttpUrls(String url) {
        assertThat(validator.isValid(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://example.com",
            "example.com",
            "http://",
            "just-a-string",
            "://missing-scheme"
    })
    void rejectsInvalidUrls(String url) {
        assertThat(validator.isValid(url)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectsNullAndBlank(String url) {
        assertThat(validator.isValid(url)).isFalse();
    }

    @Test
    void rejectsMalformedUri() {
        assertThat(validator.isValid("http://exa mple.com")).isFalse();
    }
}
