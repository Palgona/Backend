package com.palgona.palgona.common.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "P_001", "해당 상품에 대한 권한이 없습니다."),
    RELATED_BIDDING_EXISTS(HttpStatus.BAD_REQUEST, "P_002", "해당 상품과 관련된 입찰 내역이 있어 변경할 수 없습니다."),
    DELETED_PRODUCT(HttpStatus.BAD_REQUEST, "P_003", "현재 삭제된 상품입니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "P_004", "유효하지 않은 상품 가격입니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "P_005", "유효하지 않은 상품 카테고리 입니다."),
    INVALID_DEADLINE(HttpStatus.BAD_REQUEST, "P_006", "상품 판매 마감기한은 하루 이상이여야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}