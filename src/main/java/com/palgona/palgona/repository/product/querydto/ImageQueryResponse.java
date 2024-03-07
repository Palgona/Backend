package com.palgona.palgona.repository.product.querydto;

public record ImageQueryResponse(
        Long productId,
        Long imageId,
        String imageUrl
) {
}
