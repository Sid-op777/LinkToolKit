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
public class AnalyticsConfig {

    @Value("${maxmind.geoip.database.path}")
    private Resource geoIpDatabaseResource;

    @Bean
    public DatabaseReader geoIpDatabaseReader() throws IOException {
        // This bean provides the reader for the MaxMind GeoIP database.
        // It's configured to handle cases where the file might not be found.
        if (!geoIpDatabaseResource.exists()) {
            // In a real production scenario, you might want to fail startup or log a severe warning.
            // For this project, returning null and handling it in the service is acceptable.
            return null;
        }
        return new DatabaseReader.Builder(geoIpDatabaseResource.getInputStream())
                .fileMode(Reader.FileMode.MEMORY)
                .build();
    }

    @Bean
    public Parser userAgentParser() throws IOException {
        // This bean provides the parser for User-Agent strings.
        return new Parser();
    }
}