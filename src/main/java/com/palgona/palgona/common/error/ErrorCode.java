package com.palgona.palgona.common.error;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    MEMBER_NOT_FOUND(400, "C_001", "해당 유저를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
