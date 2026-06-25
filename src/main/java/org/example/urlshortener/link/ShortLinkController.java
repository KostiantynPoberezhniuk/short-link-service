package org.example.urlshortener.link;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.urlshortener.auth.AppUserDetails;
import org.example.urlshortener.link.dto.CreateShortLinkRequest;
import org.example.urlshortener.link.dto.ShortLinkResponse;
import org.example.urlshortener.link.dto.UpdateShortLinkRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/links")
@Tag(name = "Short links", description = "Manage short links and view their statistics")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    public ShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @PostMapping
    @Operation(summary = "Create a new short link")
    public ResponseEntity<ShortLinkResponse> create(@AuthenticationPrincipal AppUserDetails principal,
                                                     @Valid @RequestBody CreateShortLinkRequest request) {
        ShortLinkResponse response = shortLinkService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all short links created by the current user")
    public List<ShortLinkResponse> listAll(@AuthenticationPrincipal AppUserDetails principal) {
        return shortLinkService.listAll(principal.getId());
    }

    @GetMapping("/active")
    @Operation(summary = "List active (non-expired) short links of the current user")
    public List<ShortLinkResponse> listActive(@AuthenticationPrincipal AppUserDetails principal) {
        return shortLinkService.listActive(principal.getId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single short link with its statistics")
    public ShortLinkResponse getOne(@AuthenticationPrincipal AppUserDetails principal,
                                    @PathVariable Long id) {
        return shortLinkService.getOwned(principal.getId(), id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing short link")
    public ShortLinkResponse update(@AuthenticationPrincipal AppUserDetails principal,
                                    @PathVariable Long id,
                                    @Valid @RequestBody UpdateShortLinkRequest request) {
        return shortLinkService.update(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a short link")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AppUserDetails principal,
                                       @PathVariable Long id) {
        shortLinkService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
