package com.palgona.palgona.service;

import com.palgona.palgona.domain.bidding.Bidding;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.dto.BiddingAttemptRequest;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BiddingService {
    private final BiddingRepository biddingRepository;
    private final ProductRepository productRepository;

    public void attemptBidding(Member member, BiddingAttemptRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 product가 없습니다."));
        if (product.isDeadlineReached()) {
            throw new RuntimeException("이미 입찰 마감된 상품입니다.");
        }

        Bidding bidding = Bidding.builder()
                .member(member)
                .product(product)
                .price(request.price())
                .build();

        biddingRepository.save(bidding);
    }

    public Page<Bidding> findAllByProductId(long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 product가 없습니다."));

        return biddingRepository.findAllByProduct(pageable, product);
    }
}
