package org.example.eams.exception;

import jakarta.validation.ConstraintViolationException;
import org.example.eams.common.Result;
import org.example.eams.enums.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DuplicateKeyException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(resolveHttpStatus(errorCode))
                .body(Result.fail(errorCode, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null
                ? ErrorCode.BAD_REQUEST.getMessage()
                : fieldError.getDefaultMessage();
        return ResponseEntity
                .badRequest()
                .body(Result.fail(ErrorCode.BAD_REQUEST, message));
    }


    @ExceptionHandler({
            BindException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class

    })
    public ResponseEntity<Result<Void>> handleBindException(Exception e) {
        return ResponseEntity
                .badRequest()
                .body(Result.fail(ErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKeyException(DuplicateKeyException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.fail(ErrorCode.CONFLICT, "数据已存在"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleSystemException(Exception e) {
        log.error("系统未知异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ErrorCode.INTERNAL_ERROR));
    }

    private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case BUSINESS_ERROR, CONFLICT -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case SUCCESS -> HttpStatus.OK;
        };
    }

}