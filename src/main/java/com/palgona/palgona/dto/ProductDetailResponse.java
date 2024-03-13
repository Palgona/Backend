package com.palgona.palgona.dto;

import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.repository.product.querydto.ProductDetailQueryResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailResponse(
        Long productId,
        String productName,
        String content,
        String category,
        String productState,
        LocalDateTime deadline,
        LocalDateTime created_at,
        Long ownerId,
        String ownerName,
        String ownerImgUrl,
        Integer highestPrice,
        Integer bookmarkCount,
        List<String> imageUrls
) {
    public static ProductDetailResponse from(
            ProductDetailQueryResponse queryResponse,
            List<String> files
    ){
        return new ProductDetailResponse(
                queryResponse.productId(),
                queryResponse.productName(),
                queryResponse.content(),
                queryResponse.category(),
                queryResponse.productState(),
                queryResponse.deadline(),
                queryResponse.created_at(),
                queryResponse.ownerId(),
                queryResponse.ownerName(),
                queryResponse.ownerImgUrl(),
                queryResponse.highestBid(),
                queryResponse.bookmarkCount(),
                files
        );
    }

}