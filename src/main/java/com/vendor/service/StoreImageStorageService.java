
package com.vendor.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StoreImageStorageService {

    private final Path uploadRoot =
            Paths.get("uploads", "stores").toAbsolutePath().normalize();

    public Path getUploadRoot() {
        return uploadRoot;
    }

    public String saveImage(
            MultipartFile file,
            Integer vendorId,
            String imageType,
            long maxSizeBytes) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    imageType + " image exceeds the permitted file size."
            );
        }

        byte[] imageBytes = file.getBytes();

        String extension = detectImageExtension(imageBytes);

        if (extension == null) {
            throw new IllegalArgumentException(
                    "Please upload a valid JPG, PNG or WEBP image."
            );
        }

        Path vendorDirectory = uploadRoot
                .resolve(String.valueOf(vendorId))
                .normalize();

        if (!vendorDirectory.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid vendor directory.");
        }

        Files.createDirectories(vendorDirectory);

        String fileName = imageType + "-"
                + UUID.randomUUID() + extension;

        Path destination = vendorDirectory.resolve(fileName).normalize();

        if (!destination.startsWith(vendorDirectory)) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        Files.write(
                destination,
                imageBytes,
                StandardOpenOption.CREATE_NEW
        );

        return "/store-uploads/" + vendorId + "/" + fileName;
    }

    private String detectImageExtension(byte[] data) {

        // PNG signature
        if (data.length >= 8
                && (data[0] & 0xff) == 0x89
                && data[1] == 0x50
                && data[2] == 0x4E
                && data[3] == 0x47
                && data[4] == 0x0D
                && data[5] == 0x0A
                && data[6] == 0x1A
                && data[7] == 0x0A) {
            return ".png";
        }

        // JPEG signature
        if (data.length >= 3
                && (data[0] & 0xff) == 0xFF
                && (data[1] & 0xff) == 0xD8
                && (data[2] & 0xff) == 0xFF) {
            return ".jpg";
        }

        // WEBP signature
        if (data.length >= 12
                && data[0] == 'R'
                && data[1] == 'I'
                && data[2] == 'F'
                && data[3] == 'F'
                && data[8] == 'W'
                && data[9] == 'E'
                && data[10] == 'B'
                && data[11] == 'P') {
            return ".webp";
        }

        return null;
    }

    public void deletePreviousImage(String oldUrl, Integer vendorId) {

        if (oldUrl == null || oldUrl.isBlank()) {
            return;
        }

        String expectedPrefix = "/store-uploads/" + vendorId + "/";

        if (!oldUrl.startsWith(expectedPrefix)) {
            return;
        }

        String fileName = oldUrl.substring(expectedPrefix.length());

        if (fileName.isBlank()
                || fileName.contains("/")
                || fileName.contains("\\")) {
            return;
        }

        try {
            Path vendorDirectory = uploadRoot
                    .resolve(String.valueOf(vendorId))
                    .normalize();

            Path oldFile = vendorDirectory.resolve(fileName).normalize();

            if (oldFile.startsWith(vendorDirectory)) {
                Files.deleteIfExists(oldFile);
            }

        } catch (IOException ignored) {
            // A previous image failing to delete does not undo a successful update.
        }
    }
}
