package com.palgona.palgona.common.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MileageErrorCode implements ErrorCode {
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "M_001", "유효하지 않은 마일리지 충전 금액입니다."),
    INVALID_MILEAGE_TRANSACTION(HttpStatus.BAD_REQUEST, "M_002", "유효하지 않은 마일리지 거래 내역입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
