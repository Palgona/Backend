package com.palgona.palgona.service;

import com.palgona.palgona.domain.bidding.Bidding;
import com.palgona.palgona.domain.bidding.BiddingStatus;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.dto.BiddingAttemptRequest;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        Bidding bidding = Bidding.builder().member(member).product(product).price(request.price()).build();

        biddingRepository.save(bidding);
    }

    public Page<Bidding> findAllByProductId(long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 product가 없습니다."));

        return biddingRepository.findAllByProduct(pageable, product);
    }

    public void checkBiddingExpiration() {
        List<Bidding> expiredBiddings = biddingRepository.findExpiredBiddings(LocalDateTime.now());

        Map<Long, Bidding> highestPriceBiddings = new HashMap<>();

        // expiredBiddings를 순회하며 각 product_id에 대한 가장 높은 price를 가진 Bidding을 저장
        for (Bidding bidding : expiredBiddings) {
            Long productId = bidding.getProduct().getId();

            highestPriceBiddings.compute(productId, (key, oldValue) -> {
                if (oldValue == null || bidding.getPrice() > oldValue.getPrice() || (
                        bidding.getPrice() == oldValue.getPrice() && bidding.getUpdatedAt()
                                .isBefore(oldValue.getUpdatedAt()))) {
                    return bidding;
                } else {
                    return oldValue;
                }
            });
        }

        for (Bidding bidding : expiredBiddings) {
            Long productId = bidding.getProduct().getId();
            Bidding highestPriceBidding = highestPriceBiddings.get(productId);

            // 가장 높은 가격을 가진 Bidding이면 SUCCESS로, 그렇지 않으면 FAILED로 상태 변경
            if (bidding.getId().equals(highestPriceBidding.getId())) {
                bidding.setStatus(BiddingStatus.SUCCESS);
            } else {
                bidding.setStatus(BiddingStatus.FAILED);
            }

            // 변경된 상태를 저장
            biddingRepository.save(bidding);
        }
    }
}
