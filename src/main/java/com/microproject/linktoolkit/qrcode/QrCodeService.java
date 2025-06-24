package com.microproject.linktoolkit.qrcode;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.microproject.linktoolkit.link.LinkRepository;
import com.microproject.linktoolkit.link.LinkService;
import com.microproject.linktoolkit.link.dto.CreateLinkRequest;
import com.microproject.linktoolkit.link.dto.CreateLinkResponse;
import com.microproject.linktoolkit.qrcode.dto.QrCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {

    private final LinkService linkService;
    private final LinkRepository linkRepository;
    private final BlobServiceClient blobServiceClient; // Injected by Spring Cloud Azure

    @Value("${azure.storage.blob.container-name}")
    private String containerName;

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    @Transactional
    public QrCodeResponse generateAndUploadQrCode(CreateLinkRequest request, Optional<String> userEmailOpt, Optional<UUID> anonymousSessionIdOpt) {
        // Step 1: Create the short link first using our existing service.
        CreateLinkResponse linkResponse = linkService.createLink(request, userEmailOpt, anonymousSessionIdOpt);
        String shortUrl = linkResponse.shortUrl();

        try {
            // Step 2: Generate the QR code image from the short URL.
            byte[] qrCodeImageBytes = generateQrCodeImageBytes(shortUrl);

            // Extract alias to use as a unique filename.
            String alias = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);
            String blobName = alias + ".png";

            // Step 3: Upload the image to Azure Blob Storage.
            String publicUrl = uploadToAzure(qrCodeImageBytes, blobName);

            // Step 4: Update the link entity with the path to the QR code.
            linkRepository.findByShortAlias(alias).ifPresent(link -> {
                link.setQrCodePath(publicUrl);
                linkRepository.save(link);
            });

            // Step 5: Return the response to the user.
            return new QrCodeResponse(shortUrl, publicUrl);

        } catch (Exception e) {
            log.error("Failed to generate or upload QR code for URL: {}", shortUrl, e);
            throw new RuntimeException("Could not generate QR code. Please try again.");
        }
    }

    private byte[] generateQrCodeImageBytes(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Write the image to a byte array in memory.
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "PNG", baos);
            return baos.toByteArray();
        }
    }

    private String uploadToAzure(byte[] imageBytes, String blobName) {
        // Get a client for the specific blob (file) within the container.
        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName);

        // Upload the data. The 'true' argument allows overwriting if the blob already exists.
        blobClient.upload(new ByteArrayInputStream(imageBytes), imageBytes.length, true);

        // Return the public URL of the uploaded blob.
        return blobClient.getBlobUrl();
    }
}