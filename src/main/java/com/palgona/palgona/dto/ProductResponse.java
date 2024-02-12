package com.palgona.palgona.dto;

import com.palgona.palgona.domain.product.Product;

import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long productId,
        Long ownerId,
        String name,
        String content,
        String category,
        LocalDateTime deadline,
        LocalDateTime created_at,
        List<String> imageUrls
) {
    public static ProductResponse from(
            Product product,
            List<String> files
    ){
        return new ProductResponse(
                product.getId(),
                product.getMember().getId(),
                product.getName(),
                product.getContent(),
                product.getCategory().getValue(),
                product.getDeadline(),
                product.getCreatedAt(),
                files
        );
    }

}