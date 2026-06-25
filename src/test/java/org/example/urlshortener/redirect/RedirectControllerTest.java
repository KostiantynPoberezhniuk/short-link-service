package org.example.urlshortener.redirect;

import org.example.urlshortener.link.ShortLinkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    @Mock
    private ShortLinkService shortLinkService;

    @InjectMocks
    private RedirectController controller;

    @Test
    void redirectsToResolvedUrl() {
        when(shortLinkService.resolveAndTrack("abc123")).thenReturn("https://example.com");

        ResponseEntity<Void> response = controller.redirect("abc123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("https://example.com"));
    }
}
