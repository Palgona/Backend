package com.palgona.palgona.repository.product.querydto;

import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;

import java.time.LocalDateTime;

public record ProductDetailQueryResponse(
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
        int highestBid,
        int bookmarkCount
) {
}
