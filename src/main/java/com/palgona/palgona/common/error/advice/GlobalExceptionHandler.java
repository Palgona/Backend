package com.palgona.palgona.common.error.advice;

import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.common.error.code.ErrorCode;
import com.palgona.palgona.common.error.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handle(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("code = {} message = {}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(e.getStatus()).body(ErrorResponse.from(errorCode));
    }
}
