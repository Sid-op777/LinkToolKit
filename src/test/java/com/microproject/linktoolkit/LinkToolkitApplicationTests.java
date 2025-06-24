package com.microproject.linktoolkit;

import com.azure.storage.blob.BlobServiceClient;
import com.maxmind.geoip2.DatabaseReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ua_parser.Parser;

@SpringBootTest
class LinkToolkitApplicationTests {

    @MockBean
    private BlobServiceClient blobServiceClient;

    @MockBean
    private DatabaseReader geoIpDatabaseReader;

    @MockBean
    private Parser userAgentParser;

    @Test
    void contextLoads() {
    }

}
