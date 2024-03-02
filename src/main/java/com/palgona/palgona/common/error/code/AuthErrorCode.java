package com.palgona.palgona.common.error.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {

    ILLEGAL_TOKEN(HttpStatus.UNAUTHORIZED, "A_002", "잘못된 JWT 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A_003", "만료된 JWT 토큰입니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "A_004", "잘못된 JWT 서명입니다."),
    NOT_SUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "A_005", "지원되지 않는 JWT 토큰입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "A_006", "접근 권한이 없는 리소스입니다."),
    ILLEGAL_KAKAO_TOKEN(HttpStatus.UNAUTHORIZED, "A_007", "잘못된 카카오 토큰입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
