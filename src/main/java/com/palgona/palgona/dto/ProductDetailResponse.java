package com.palgona.palgona.dto;

import com.palgona.palgona.domain.product.Product;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailResponse(
        Long productId,
        String ownerName,
        String productName,
        String content,
        String category,
        LocalDateTime deadline,
        LocalDateTime created_at,
        Integer highestPrice,
        Long bookmarkCount,
        List<String> imageUrls
) {
    public static ProductDetailResponse from(
            Product product,
            List<String> files,
            Integer highestPrice,
            Long bookmarkCount
    ){
        return new ProductDetailResponse(
                product.getId(),
                product.getMember().getNickName(),
                product.getName(),
                product.getContent(),
                product.getCategory().getValue(),
                product.getDeadline(),
                product.getCreatedAt(),
                highestPrice,
                bookmarkCount,
                files
        );
    }

}