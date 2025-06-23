package com.microproject.linktoolkit.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClickRepository extends JpaRepository<Click, UUID> {
    /**
     * Efficiently counts the total number of clicks for a given link ID.
     * Spring Data JPA derives this query from the method name.
     *
     * @param linkId The UUID of the link.
     * @return The total click count as a long.
     */
    long countByLinkId(UUID linkId);

    /**
     * This is a "Projection Interface". It's a clean way to define the shape of our
     * custom query results without creating a full DTO class. Spring Data will
     * automatically create proxy instances of this interface.
     */
    interface AnalyticsProjection {
        String getName();
        Long getCount();
    }

    interface ClickCountPerDay {
        Instant getDate();
        Long getClicks();
    }

    /**
     * Fetches the top referrers for a given link, ordered by click count.
     * Uses a custom JPQL query and the AnalyticsProjection to return only the needed data.
     * The Pageable parameter is used to limit the results (e.g., to the top 5).
     *
     * @param linkId   The UUID of the link.
     * @param pageable A Pageable object (e.g., PageRequest.of(0, 5) for the top 5).
     * @return A list of top referrers and their counts.
     */
    @Query("SELECT c.referer as name, COUNT(c) as count FROM Click c WHERE c.link.id = :linkId AND c.referer IS NOT NULL GROUP BY c.referer ORDER BY count DESC")
    List<AnalyticsProjection> findTopReferrers(@Param("linkId") UUID linkId, Pageable pageable);

    /**
     * Fetches the top devices for a given link.
     *
     * @param linkId   The UUID of the link.
     * @param pageable A Pageable object to limit results.
     * @return A list of top devices and their counts.
     */
    @Query("SELECT c.deviceType as name, COUNT(c) as count FROM Click c WHERE c.link.id = :linkId AND c.deviceType IS NOT NULL GROUP BY c.deviceType ORDER BY count DESC")
    List<AnalyticsProjection> findTopDevices(@Param("linkId") UUID linkId, Pageable pageable);

    /**
     * Fetches the top countries for a given link.
     *
     * @param linkId   The UUID of the link.
     * @param pageable A Pageable object to limit results.
     * @return A list of top countries and their counts.
     */
    @Query("SELECT c.countryCode as name, COUNT(c) as count FROM Click c WHERE c.link.id = :linkId AND c.countryCode IS NOT NULL GROUP BY c.countryCode ORDER BY count DESC")
    List<AnalyticsProjection> findTopLocations(@Param("linkId") UUID linkId, Pageable pageable);

    /**
     * Fetches click counts grouped by day for a time-series chart.
     * NOTE: `date_trunc` is a PostgreSQL-specific function. This query is not portable
     * to other databases like MySQL without modification.
     *
     * @param linkId    The UUID of the link.
     * @param startDate The start date for the time series (e.g., 30 days ago).
     * @return A list of dates and the number of clicks on each date.
     */
    @Query(value = "SELECT date_trunc('day', c.clicked_at) as date, COUNT(c.id) as clicks " +
            "FROM clicks c " +
            "WHERE c.link_id = :linkId AND c.clicked_at >= :startDate " +
            "GROUP BY date " +
            "ORDER BY date",
            nativeQuery = true) // This query uses a native SQL function, so we must mark it as such.
    List<ClickCountPerDay> findClicksPerDay(@Param("linkId") UUID linkId, @Param("startDate") Instant startDate);
}
