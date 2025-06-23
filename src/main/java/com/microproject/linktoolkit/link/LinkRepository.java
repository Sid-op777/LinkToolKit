package com.microproject.linktoolkit.link;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkRepository extends JpaRepository<Link, UUID> {
    /**
     * Finds a link by its unique short alias. This is the primary method used for redirection.
     * The query is case-sensitive by default, which is what we want for short URLs.
     *
     * @param shortAlias The short alias to search for (e.g., "abc123").
     * @return An Optional containing the Link if found.
     */
    Optional<Link> findByShortAlias(String shortAlias);

    /**
     * Checks if a link with the given short alias already exists in the database.
     * This is more efficient than findByShortAlias when we only need to know about existence.
     *
     * @param shortAlias The short alias to check.
     * @return true if a link with this alias exists, false otherwise.
     */
    boolean existsByShortAlias(String shortAlias);

    /**
     * Finds all links associated with a given user ID, ordered by creation date descending.
     *
     * @param userId The UUID of the user.
     * @return A list of links belonging to the user.
     */
    List<Link> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Finds all links associated with an anonymous session ID.
     * This is used to claim links when a user registers.
     *
     * @param sessionId The anonymous session UUID.
     * @return A list of links associated with the session.
     */
    List<Link> findByAnonymousSessionId(UUID sessionId);

    /**
     * Updates the user_id for a list of links associated with an anonymous session.
     * This is a custom bulk update query for efficiency when a user registers.
     * The @Modifying annotation is required for queries that change data.
     *
     * @param userId The new user's ID to associate the links with.
     * @param anonymousSessionId The session ID to find links by.
     */
    @Modifying
    @Query("UPDATE Link l SET l.user.id = :userId, l.anonymousSessionId = NULL WHERE l.anonymousSessionId = :anonymousSessionId")
    void claimAnonymousLinks(UUID userId, UUID anonymousSessionId);

    /**
     * Finds all links that have expired as of the given timestamp.
     * This will be used by our scheduled cleanup job.
     *
     * @param now The current time.
     * @return A list of expired links.
     */
    List<Link> findByExpiresAtBefore(Instant now);

    // ... all your existing findBy... and existsBy... methods

    /**
     * Deletes all links that have expired before the given timestamp in a single, efficient query.
     * The @Modifying annotation is required for queries that alter data (UPDATE, DELETE, INSERT).
     *
     * @param now The current time.
     * @return The number of rows deleted.
     */
    @Modifying
    @Query("DELETE FROM Link l WHERE l.expiresAt < :now")
    int deleteByExpiresAtBefore(Instant now);
}
