// src/main/java/com/microproject/linktoolkit/analytics/AnalyticsService.java

package com.microproject.linktoolkit.analytics;

import com.microproject.linktoolkit.analytics.dto.*;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.microproject.linktoolkit.link.LinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ClickRepository clickRepository;
    private final LinkRepository linkRepository;
    private final DatabaseReader geoIpDatabaseReader;
    private final Parser userAgentParser;

    @Async
    public void logClick(UUID linkId, String ipAddress, String userAgent, String referer) {
        try {
            Click.ClickBuilder clickBuilder = Click.builder()
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referer(referer);

            // The rest of the logic uses the pre-extracted data
            extractLocation(ipAddress).ifPresent(clickBuilder::countryCode);
            extractDeviceType(userAgent).ifPresent(clickBuilder::deviceType);

            Click newClick = clickBuilder.build();

            linkRepository.findById(linkId).ifPresent(link -> {
                newClick.setLink(link);
                clickRepository.save(newClick);
            });

        } catch (Exception e) {
            log.error("Failed to log click for linkId: {}. Error: {}", linkId, e.getMessage());
        }
    }

    private Optional<String> extractLocation(String ip) {
        if (geoIpDatabaseReader == null || ip == null) return Optional.empty();
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            return Optional.ofNullable(geoIpDatabaseReader.city(ipAddress))
                    .map(CityResponse::getCountry)
                    .map(country -> country.getIsoCode());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<String> extractDeviceType(String userAgentString) {
        if (userAgentString == null) return Optional.empty();
        try {
            return Optional.ofNullable(userAgentParser.parse(userAgentString))
                    .map(c -> c.device.family);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public LinkAnalyticsResponse getAnalyticsForLink(UUID linkId) {
        // 1. Get total clicks
        long totalClicks = clickRepository.countByLinkId(linkId);

        // 2. Get clicks over the last 30 days for the time-series chart
        Instant startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        List<TimeSeriesDataPoint> clicksOverTime = clickRepository.findClicksPerDay(linkId, startDate)
                .stream()
                .map(projection -> new TimeSeriesDataPoint(
                        // Format the Instant to a simple YYYY-MM-DD string
                        DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(projection.getDate()),
                        projection.getClicks()
                ))
                .collect(Collectors.toList());

        // 3. Get top 5 for each category for the pie charts
        PageRequest topFive = PageRequest.of(0, 5);

        List<CategoryDataPoint> topReferrers = clickRepository.findTopReferrers(linkId, topFive)
                .stream()
                .map(p -> new CategoryDataPoint(p.getName(), p.getCount()))
                .collect(Collectors.toList());

        List<CategoryDataPoint> topDevices = clickRepository.findTopDevices(linkId, topFive)
                .stream()
                .map(p -> new CategoryDataPoint(p.getName(), p.getCount()))
                .collect(Collectors.toList());

        List<CategoryDataPoint> topLocations = clickRepository.findTopLocations(linkId, topFive)
                .stream()
                .map(p -> new CategoryDataPoint(p.getName(), p.getCount()))
                .collect(Collectors.toList());

        // 4. Assemble and return the final response object
        return new LinkAnalyticsResponse(
                totalClicks,
                clicksOverTime,
                topReferrers,
                topDevices,
                topLocations
        );
    }
}