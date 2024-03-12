package com.palgona.palgona.repository.product.querydto;

import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;

public record ProductDetailQueryResponse(
        Product product,
        Member member,
        int highestBid,
        int bookmarkCount
) {
}
