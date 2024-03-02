package com.palgona.palgona.common.error.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_EXIST(HttpStatus.NO_CONTENT, "C_001", "해당 유저를 찾을 수 없습니다."),
    ADMIN_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "C_002", "관리자 권한으로만 접근할 수 있는 리소스입니다."),
    MEMBER_NOT_FOUND(HttpStatus.OK, "C_003", "해당 유저를 찾을 수 없습니다."),
    ALREADY_SIGNED_UP(HttpStatus.BAD_REQUEST, "C_004", "이미 회원가입된 유저입니다."),
    DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "C_005", "이미 존재하는 닉네임입니다.");

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
