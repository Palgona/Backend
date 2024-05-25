package com.palgona.palgona.common.error.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatErrorCode implements ErrorCode {
    CHATROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "CR_001", "해당 채팅방이 존재하지 않습니다."),
    INVALID_MEMBER(HttpStatus.BAD_REQUEST, "CR_002", "해당하는 채팅방에 존재하는 유저가 아닙니다."),
    MESSAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "CR_003", "해당하는 메시지가 존재하지 않습니다."),
    READ_STATUS_NOT_FOUND(HttpStatus.BAD_REQUEST, "CR_004", "해당 메시지의 읽은 기록이 존재하지 않습니다."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "CR_005", "파일에 문제가 있습니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;

    ChatErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
