package com.palgona.palgona.dto.response;

import com.palgona.palgona.domain.product.Product;
import java.time.LocalDateTime;

public record ProductPageResponse(
        Long id,
        String name,
        int currentBid,
        int bookmarkCount,
        LocalDateTime deadline,
        LocalDateTime created_at,
        String imageUrl
) {

    public static ProductPageResponse from(Product product) {
        return new ProductPageResponse(
                product.getId(),
                product.getName(),
                product.getCurrentBid(),
                product.getBookmarkCount(),
                product.getDeadline(),
                product.getCreatedAt(),
                product.getProductImages().get(0).getImage().getImageUrl()
        );
    }
}
