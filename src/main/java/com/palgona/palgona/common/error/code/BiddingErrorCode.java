package com.palgona.palgona.common.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BiddingErrorCode implements ErrorCode {
    BIDDING_EXPIRED_PRODUCT(HttpStatus.BAD_REQUEST, "B_001", "이미 입찰 마감된 상품입니다."),
    BIDDING_LOWER_PRICE(HttpStatus.BAD_REQUEST, "B_002", "현재 가격이 기존 최고 가격보다 낮습니다."),
    BIDDING_INSUFFICIENT_BID(HttpStatus.BAD_REQUEST, "B_003", "입찰가격과 기존 최고 가격의 차이가 최소 단위보다 작습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}