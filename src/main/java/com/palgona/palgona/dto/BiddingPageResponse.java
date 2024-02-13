package com.palgona.palgona.dto;

import com.palgona.palgona.domain.bidding.Bidding;
import java.util.List;
import org.springframework.data.domain.Page;

public record BiddingPageResponse(long total, int pages, List<Bidding> biddingList) {
    public static BiddingPageResponse of(Page<Bidding> page, List<Bidding> biddingList) {
        return new BiddingPageResponse(page.getTotalElements(), page.getTotalPages(), biddingList);
    }
}
