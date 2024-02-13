package com.palgona.palgona.common.error.advice;

import com.palgona.palgona.common.error.BusinessException;
import com.palgona.palgona.common.error.ErrorCode;
import com.palgona.palgona.common.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleRuntimeException(BusinessException e) {
        final ErrorCode errorCode = e.getErrorCode();

        final ErrorResponse response = ErrorResponse.builder()
                .businessCode(errorCode.getCode())
                .errorMessage(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
