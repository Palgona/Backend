package com.palgona.palgona.dto.response;

import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.repository.product.querydto.ProductQueryResponse;
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

    public static ProductPageResponse of(ProductQueryResponse queryResponse, String imageUrl) {
        return new ProductPageResponse(
                queryResponse.id(),
                queryResponse.name(),
                queryResponse.currentBid(),
                queryResponse.bookmarkCount(),
                queryResponse.deadline(),
                queryResponse.createdAt(),
                imageUrl
        );
    }
}
