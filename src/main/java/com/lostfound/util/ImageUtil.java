package com.lostfound.util;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class ImageUtil {
    private static final Path IMAGE_DIRECTORY = Path.of("images");

    private ImageUtil() {
    }

    public static String copyImageToLocalFolder(File sourceFile) throws IOException {
        if (sourceFile == null) {
            return null;
        }

        Files.createDirectories(IMAGE_DIRECTORY);

        String originalName = sourceFile.getName();
        String extension = "";
        int extensionIndex = originalName.lastIndexOf('.');
        if (extensionIndex >= 0) {
            extension = originalName.substring(extensionIndex);
        }

        String generatedName = UUID.randomUUID() + extension;
        Path destination = IMAGE_DIRECTORY.resolve(generatedName);
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString().replace("\\", "/");
    }

    public static Image loadImage(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }

        File imageFile = new File(relativePath);
        if (!imageFile.exists()) {
            return null;
        }

        return new Image(imageFile.toURI().toString());
    }

    public static void deleteLocalImage(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        Path imageRoot = IMAGE_DIRECTORY.toAbsolutePath().normalize();
        Path imagePath = Path.of(relativePath).toAbsolutePath().normalize();
        if (!imagePath.startsWith(imageRoot)) {
            return;
        }

        Files.deleteIfExists(imagePath);
    }
}
