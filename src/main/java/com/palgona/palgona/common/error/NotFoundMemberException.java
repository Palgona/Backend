package com.palgona.palgona.common.error;

public class NotFoundMemberException extends BusinessException {
    public NotFoundMemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}
