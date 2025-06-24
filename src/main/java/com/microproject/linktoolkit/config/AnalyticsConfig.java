// src/main/java/com/microproject/linktoolkit/config/AnalyticsConfig.java

package com.microproject.linktoolkit.config;

import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import ua_parser.Parser;

import java.io.IOException;

@Configuration
@Slf4j
public class AnalyticsConfig {

    @Value("${maxmind.geoip.database.path}")
    private Resource geoIpDatabaseResource;

    @Bean
    public DatabaseReader geoIpDatabaseReader() throws IOException {
        // This bean provides the reader for the MaxMind GeoIP database.
        // It's configured to handle cases where the file might not be found.
        if (!geoIpDatabaseResource.exists()) {
            log.error("GeoIP database file not found at path: {}", geoIpDatabaseResource.getFilename());
            throw new IOException("GeoIP database file not found.");
        }

        File tempDbFile = File.createTempFile("geolite2-", ".mmdb");
        tempDbFile.deleteOnExit();

        try (InputStream inputStream = geoIpDatabaseResource.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(tempDbFile)) {
            FileCopyUtils.copy(inputStream, outputStream);
        }

        log.info("GeoIP database copied to temporary file: {}", tempDbFile.getAbsolutePath());

        return new DatabaseReader.Builder(tempDbFile)
                .fileMode(Reader.FileMode.MEMORY_MAPPED)
                .build();
    }

    @Bean
    public Parser userAgentParser() throws IOException {
        // This bean provides the parser for User-Agent strings.
        return new Parser();
    }
}