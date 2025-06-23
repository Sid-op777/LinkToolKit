package com.microproject.linktoolkit.qrcode.dto;

/**
 * DTO for the response after generating a QR code.
 * @param shortUrl The shortened URL that the QR code points to.
 * @param qrCodeUrl The public URL of the generated QR code image in cloud storage.
 */
public record QrCodeResponse(
        String shortUrl,
        String qrCodeUrl
) {
}