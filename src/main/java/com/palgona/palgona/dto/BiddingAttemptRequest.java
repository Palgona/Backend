package com.palgona.palgona.dto;

public record BiddingAttemptRequest(
        Long productId,
        int price
) {
}
