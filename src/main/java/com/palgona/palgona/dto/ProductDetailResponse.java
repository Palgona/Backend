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
                queryResponse.product().getId(),
                queryResponse.product().getName(),
                queryResponse.product().getContent(),
                queryResponse.product().getCategory().getValue(),
                queryResponse.product().getDeadline(),
                queryResponse.product().getCreatedAt(),
                queryResponse.member().getId(),
                queryResponse.member().getNickName(),
                queryResponse.member().getProfileImage(),
                queryResponse.highestBid(),
                queryResponse.bookmarkCount(),
                files
        );
    }

}