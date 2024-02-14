package com.palgona.palgona.common.error.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NO_CONTENT, "C_001", "해당 유저를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    MemberErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
