package org.example.urlshortener.link;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<ShortLink> findAllByOwnerId(Long ownerId);

    @Query("SELECT l FROM ShortLink l WHERE l.owner.id = :ownerId AND l.expiresAt > :now")
    List<ShortLink> findActiveByOwnerId(@Param("ownerId") Long ownerId, @Param("now") Instant now);

    Optional<ShortLink> findByIdAndOwnerId(Long id, Long ownerId);
}
