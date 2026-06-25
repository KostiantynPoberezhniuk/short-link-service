package org.example.urlshortener.link;

import org.example.urlshortener.auth.AppUserDetails;
import org.example.urlshortener.link.dto.CreateShortLinkRequest;
import org.example.urlshortener.link.dto.ShortLinkResponse;
import org.example.urlshortener.link.dto.UpdateShortLinkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortLinkControllerTest {

    private static final Long USER_ID = 7L;

    @Mock
    private ShortLinkService shortLinkService;

    @InjectMocks
    private ShortLinkController controller;

    private AppUserDetails principal;

    private ShortLinkResponse sample(String code) {
        return new ShortLinkResponse(1L, code, "http://localhost:8080/r/" + code,
                "https://example.com", Instant.now(), Instant.now().plusSeconds(60), true, 0L, "alice");
    }

    @BeforeEach
    void setUp() {
        principal = mock(AppUserDetails.class);
        when(principal.getId()).thenReturn(USER_ID);
    }

    @Test
    void createReturnsCreated() {
        CreateShortLinkRequest request = new CreateShortLinkRequest("https://example.com", null);
        ShortLinkResponse created = sample("abc123");
        when(shortLinkService.create(USER_ID, request)).thenReturn(created);

        ResponseEntity<ShortLinkResponse> response = controller.create(principal, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(created);
    }

    @Test
    void listAllDelegates() {
        when(shortLinkService.listAll(USER_ID)).thenReturn(List.of(sample("a")));

        assertThat(controller.listAll(principal)).hasSize(1);
    }

    @Test
    void listActiveDelegates() {
        when(shortLinkService.listActive(USER_ID)).thenReturn(List.of(sample("b")));

        assertThat(controller.listActive(principal)).hasSize(1);
    }

    @Test
    void getOneDelegates() {
        when(shortLinkService.getOwned(USER_ID, 5L)).thenReturn(sample("c"));

        assertThat(controller.getOne(principal, 5L).shortCode()).isEqualTo("c");
    }

    @Test
    void updateDelegates() {
        UpdateShortLinkRequest request = new UpdateShortLinkRequest("https://new.com", 3);
        when(shortLinkService.update(USER_ID, 5L, request)).thenReturn(sample("d"));

        assertThat(controller.update(principal, 5L, request).shortCode()).isEqualTo("d");
    }

    @Test
    void deleteReturnsNoContent() {
        ResponseEntity<Void> response = controller.delete(principal, 5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(shortLinkService).delete(USER_ID, 5L);
    }
}
