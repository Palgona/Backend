package com.palgona.palgona.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductUpdateRequest(
        String name,
        Integer initialPrice,
        String content,
        String category,
        LocalDateTime deadline,
        List<String> deletedImageUrls
) {
}
