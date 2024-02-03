package com.palgona.palgona.service.image;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum FileExtension {
    JPEG(".jpeg"),
    JPG(".jpg"),
    JFIF(".jfif"),
    PNG(".png"),
    SVG(".svg");

    private final String extension;

    FileExtension(String extension) {
        this.extension = extension;
    }

    public static FileExtension from(String fileName) {
        return Arrays.stream(values())
                .filter(fileExtension -> fileName.endsWith(fileExtension.getExtension()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid file extension"));
    }
}
