package com.microproject.linktoolkit.scheduled;

import com.microproject.linktoolkit.link.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkCleanupService {

    private final LinkRepository linkRepository;

    /**
     * A scheduled job that runs automatically to clean up expired links.
     * The cron expression "0 0 1 * * ?" means "run at 1:00 AM every day".
     * This is a good, low-traffic time to perform maintenance tasks.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void deleteExpiredLinks() {
        log.info("Starting scheduled job: Deleting expired links...");
        Instant now = Instant.now();

        try {
            int deletedCount = linkRepository.deleteByExpiresAtBefore(now);
            if (deletedCount > 0) {
                log.info("Successfully deleted {} expired links.", deletedCount);
            } else {
                log.info("No expired links found to delete.");
            }
        } catch (Exception e) {
            log.error("Error occurred during expired link cleanup job.", e);
        }
    }
}