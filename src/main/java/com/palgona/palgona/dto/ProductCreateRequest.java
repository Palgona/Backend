package com.palgona.palgona.dto;

import java.time.LocalDateTime;

public record ProductCreateRequest(
        String name,
        Integer initialPrice,
        String content,
        String category,
        LocalDateTime deadline
) {
}
