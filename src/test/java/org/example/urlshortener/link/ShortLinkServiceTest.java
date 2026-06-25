package org.example.urlshortener.link;

import org.example.urlshortener.config.ShortLinkProperties;
import org.example.urlshortener.error.InvalidRequestException;
import org.example.urlshortener.error.LinkExpiredException;
import org.example.urlshortener.error.ResourceNotFoundException;
import org.example.urlshortener.link.dto.CreateShortLinkRequest;
import org.example.urlshortener.link.dto.ShortLinkResponse;
import org.example.urlshortener.link.dto.UpdateShortLinkRequest;
import org.example.urlshortener.user.Role;
import org.example.urlshortener.user.User;
import org.example.urlshortener.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortLinkServiceTest {

    private static final Long USER_ID = 1L;
    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private ShortLinkRepository shortLinkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShortCodeGenerator codeGenerator;

    @Mock
    private UrlValidator urlValidator;

    private ShortLinkService service;
    private User owner;

    @BeforeEach
    void setUp() {
        ShortLinkProperties properties = new ShortLinkProperties(BASE_URL, 6, 8, 30);
        service = new ShortLinkService(shortLinkRepository, userRepository, codeGenerator, urlValidator, properties);
        owner = new User("alice", "encoded", Role.USER);
    }

    @Test
    void createPersistsLinkWithGeneratedCodeAndDefaultTtl() {
        when(urlValidator.isValid("https://example.com")).thenReturn(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(owner));
        when(codeGenerator.generate()).thenReturn("abc123");
        when(shortLinkRepository.existsByShortCode("abc123")).thenReturn(false);
        when(shortLinkRepository.save(any(ShortLink.class))).thenAnswer(i -> i.getArgument(0));

        ShortLinkResponse response = service.create(USER_ID, new CreateShortLinkRequest("https://example.com", null));

        ArgumentCaptor<ShortLink> captor = ArgumentCaptor.forClass(ShortLink.class);
        verify(shortLinkRepository).save(captor.capture());
        ShortLink saved = captor.getValue();
        assertThat(saved.getShortCode()).isEqualTo("abc123");
        assertThat(saved.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(saved.getExpiresAt()).isAfter(Instant.now().plus(29, ChronoUnit.DAYS));
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/r/abc123");
        assertThat(response.active()).isTrue();
        assertThat(response.owner()).isEqualTo("alice");
        assertThat(response.visitCount()).isZero();
    }

    @Test
    void createUsesProvidedTtl() {
        when(urlValidator.isValid(anyString())).thenReturn(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(owner));
        when(codeGenerator.generate()).thenReturn("code01");
        when(shortLinkRepository.existsByShortCode("code01")).thenReturn(false);
        when(shortLinkRepository.save(any(ShortLink.class))).thenAnswer(i -> i.getArgument(0));

        service.create(USER_ID, new CreateShortLinkRequest("https://example.com", 1));

        ArgumentCaptor<ShortLink> captor = ArgumentCaptor.forClass(ShortLink.class);
        verify(shortLinkRepository).save(captor.capture());
        assertThat(captor.getValue().getExpiresAt()).isBefore(Instant.now().plus(2, ChronoUnit.DAYS));
    }

    @Test
    void createRetriesUntilUniqueCode() {
        when(urlValidator.isValid(anyString())).thenReturn(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(owner));
        when(codeGenerator.generate()).thenReturn("dup").thenReturn("uniq");
        when(shortLinkRepository.existsByShortCode("dup")).thenReturn(true);
        when(shortLinkRepository.existsByShortCode("uniq")).thenReturn(false);
        when(shortLinkRepository.save(any(ShortLink.class))).thenAnswer(i -> i.getArgument(0));

        ShortLinkResponse response = service.create(USER_ID, new CreateShortLinkRequest("https://example.com", null));

        assertThat(response.shortCode()).isEqualTo("uniq");
    }

    @Test
    void createRejectsInvalidUrl() {
        when(urlValidator.isValid(anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.create(USER_ID, new CreateShortLinkRequest("not-a-url", null)))
                .isInstanceOf(InvalidRequestException.class);

        verify(shortLinkRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingUser() {
        when(urlValidator.isValid(anyString())).thenReturn(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(USER_ID, new CreateShortLinkRequest("https://example.com", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listAllReturnsMappedResponses() {
        ShortLink link = new ShortLink("aaaaaa", "https://example.com",
                Instant.now().plus(5, ChronoUnit.DAYS), owner);
        when(shortLinkRepository.findAllByOwnerId(USER_ID)).thenReturn(List.of(link));

        List<ShortLinkResponse> result = service.listAll(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).shortCode()).isEqualTo("aaaaaa");
    }

    @Test
    void listActiveDelegatesToRepository() {
        ShortLink link = new ShortLink("bbbbbb", "https://example.com",
                Instant.now().plus(5, ChronoUnit.DAYS), owner);
        when(shortLinkRepository.findActiveByOwnerId(eqUser(), any(Instant.class))).thenReturn(List.of(link));

        List<ShortLinkResponse> result = service.listActive(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).active()).isTrue();
    }

    @Test
    void getOwnedReturnsLink() {
        ShortLink link = new ShortLink("cccccc", "https://example.com",
                Instant.now().plus(5, ChronoUnit.DAYS), owner);
        when(shortLinkRepository.findByIdAndOwnerId(10L, USER_ID)).thenReturn(Optional.of(link));

        assertThat(service.getOwned(USER_ID, 10L).shortCode()).isEqualTo("cccccc");
    }

    @Test
    void getOwnedThrowsWhenMissing() {
        when(shortLinkRepository.findByIdAndOwnerId(10L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOwned(USER_ID, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateChangesUrlAndExpiry() {
        ShortLink link = new ShortLink("dddddd", "https://old.com",
                Instant.now().plus(5, ChronoUnit.DAYS), owner);
        when(urlValidator.isValid("https://new.com")).thenReturn(true);
        when(shortLinkRepository.findByIdAndOwnerId(10L, USER_ID)).thenReturn(Optional.of(link));
        when(shortLinkRepository.save(any(ShortLink.class))).thenAnswer(i -> i.getArgument(0));

        ShortLinkResponse response = service.update(USER_ID, 10L,
                new UpdateShortLinkRequest("https://new.com", 2));

        assertThat(response.originalUrl()).isEqualTo("https://new.com");
        assertThat(link.getExpiresAt()).isBefore(Instant.now().plus(3, ChronoUnit.DAYS));
    }

    @Test
    void updateKeepsExpiryWhenTtlNull() {
        Instant originalExpiry = Instant.now().plus(5, ChronoUnit.DAYS);
        ShortLink link = new ShortLink("eeeeee", "https://old.com", originalExpiry, owner);
        when(urlValidator.isValid("https://new.com")).thenReturn(true);
        when(shortLinkRepository.findByIdAndOwnerId(10L, USER_ID)).thenReturn(Optional.of(link));
        when(shortLinkRepository.save(any(ShortLink.class))).thenAnswer(i -> i.getArgument(0));

        service.update(USER_ID, 10L, new UpdateShortLinkRequest("https://new.com", null));

        assertThat(link.getExpiresAt()).isEqualTo(originalExpiry);
    }

    @Test
    void updateRejectsInvalidUrl() {
        when(urlValidator.isValid(anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.update(USER_ID, 10L,
                new UpdateShortLinkRequest("bad", null)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void deleteRemovesOwnedLink() {
        ShortLink link = new ShortLink("ffffff", "https://example.com",
                Instant.now().plus(5, ChronoUnit.DAYS), owner);
        when(shortLinkRepository.findByIdAndOwnerId(10L, USER_ID)).thenReturn(Optional.of(link));

        service.delete(USER_ID, 10L);

        verify(shortLinkRepository).delete(link);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(shortLinkRepository.findByIdAndOwnerId(10L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USER_ID, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolveAndTrackIncrementsVisitsAndReturnsUrl() {
        ShortLink link = new ShortLink("gggggg", "https://example.com",
                Instant.now().plus(5, ChronoUnit.DAYS), owner);
        when(shortLinkRepository.findByShortCode("gggggg")).thenReturn(Optional.of(link));

        String url = service.resolveAndTrack("gggggg");

        assertThat(url).isEqualTo("https://example.com");
        assertThat(link.getVisitCount()).isEqualTo(1L);
    }

    @Test
    void resolveAndTrackThrowsWhenMissing() {
        when(shortLinkRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveAndTrack("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolveAndTrackThrowsWhenExpired() {
        ShortLink link = new ShortLink("hhhhhh", "https://example.com",
                Instant.now().minus(1, ChronoUnit.DAYS), owner);
        when(shortLinkRepository.findByShortCode("hhhhhh")).thenReturn(Optional.of(link));

        assertThatThrownBy(() -> service.resolveAndTrack("hhhhhh"))
                .isInstanceOf(LinkExpiredException.class);
        assertThat(link.getVisitCount()).isZero();
    }

    private static Long eqUser() {
        return org.mockito.ArgumentMatchers.eq(USER_ID);
    }
}
