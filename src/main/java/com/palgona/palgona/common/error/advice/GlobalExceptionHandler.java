package com.palgona.palgona.common.error.advice;

import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.common.error.code.ErrorCode;
import com.palgona.palgona.common.error.dto.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handle(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity.status(e.getStatus()).body(ErrorResponse.from(errorCode));
    }
}
