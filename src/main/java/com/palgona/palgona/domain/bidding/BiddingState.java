package com.palgona.palgona.domain.bidding;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BiddingState {
    SUCCESS("SUCCESS", "성공"),
    FAILED("FAILED", "실패"),
    ATTEMPT("ATTEMPT", "시도");

    private final String key;
    private final String title;
}
