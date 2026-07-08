package org.example.urlshortener.link;

import org.example.urlshortener.config.ShortLinkProperties;
import org.example.urlshortener.error.InvalidRequestException;
import org.example.urlshortener.error.LinkExpiredException;
import org.example.urlshortener.error.ResourceNotFoundException;
import org.example.urlshortener.link.dto.CreateShortLinkRequest;
import org.example.urlshortener.link.dto.ShortLinkResponse;
import org.example.urlshortener.link.dto.UpdateShortLinkRequest;
import org.example.urlshortener.user.User;
import org.example.urlshortener.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ShortLinkService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;

    private final ShortLinkRepository shortLinkRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator codeGenerator;
    private final UrlValidator urlValidator;
    private final String baseUrl;
    private final int defaultTtlDays;

    public ShortLinkService(ShortLinkRepository shortLinkRepository, UserRepository userRepository,
                            ShortCodeGenerator codeGenerator, UrlValidator urlValidator,
                            ShortLinkProperties properties) {
        this.shortLinkRepository = shortLinkRepository;
        this.userRepository = userRepository;
        this.codeGenerator = codeGenerator;
        this.urlValidator = urlValidator;
        this.baseUrl = properties.baseUrl();
        this.defaultTtlDays = properties.defaultTtlDays();
    }

    @Transactional
    public ShortLinkResponse create(Long userId, CreateShortLinkRequest request) {
        String originalUrl = request.originalUrl().trim();
        if (!urlValidator.isValid(originalUrl)) {
            throw new InvalidRequestException("Original URL is not a valid http(s) URL");
        }
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Instant expiresAt = Instant.now().plus(resolveTtlDays(request.ttlDays()), ChronoUnit.DAYS);
        ShortLink link = new ShortLink(generateUniqueCode(), originalUrl, expiresAt, owner);
        return toResponse(shortLinkRepository.save(link));
    }

    @Transactional(readOnly = true)
    public List<ShortLinkResponse> listAll(Long userId) {
        return shortLinkRepository.findAllByOwnerId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShortLinkResponse> listActive(Long userId) {
        return shortLinkRepository.findActiveByOwnerId(userId, Instant.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShortLinkResponse getOwned(Long userId, Long linkId) {
        return toResponse(requireOwnedLink(userId, linkId));
    }

    @Transactional
    public ShortLinkResponse update(Long userId, Long linkId, UpdateShortLinkRequest request) {
        String originalUrl = request.originalUrl().trim();
        if (!urlValidator.isValid(originalUrl)) {
            throw new InvalidRequestException("Original URL is not a valid http(s) URL");
        }
        ShortLink link = requireOwnedLink(userId, linkId);
        link.setOriginalUrl(originalUrl);
        if (request.ttlDays() != null) {
            link.setExpiresAt(Instant.now().plus(request.ttlDays(), ChronoUnit.DAYS));
        }
        return toResponse(shortLinkRepository.save(link));
    }

    @Transactional
    public void delete(Long userId, Long linkId) {
        ShortLink link = requireOwnedLink(userId, linkId);
        shortLinkRepository.delete(link);
    }

    @Transactional
    public String resolveAndTrack(String shortCode) {
        ShortLink link = shortLinkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short link not found: " + shortCode));
        if (link.isExpired()) {
            throw new LinkExpiredException("Short link has expired: " + shortCode);
        }
        shortLinkRepository.incrementVisitCount(shortCode);
        return link.getOriginalUrl();
    }

    private ShortLink requireOwnedLink(Long userId, Long linkId) {
        return shortLinkRepository.findByIdAndOwnerId(linkId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Short link not found: " + linkId));
    }

    private int resolveTtlDays(Integer requestedTtlDays) {
        return requestedTtlDays != null ? requestedTtlDays : defaultTtlDays;
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate();
            if (!shortLinkRepository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate a unique short code");
    }

    private ShortLinkResponse toResponse(ShortLink link) {
        return ShortLinkResponse.from(link, baseUrl);
    }
}
