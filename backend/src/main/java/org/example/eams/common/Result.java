package org.example.eams.common;
import org.example.eams.enums.ErrorCode;

public record Result<T>(
   int code,
   String message,
   T data
) {
    public static <T> Result<T> success(T data) {
        return new Result<>(
                ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(),
                data
        );
    }
    public static  Result<Void> success() {
         return success(null);
    }
    public static  <T> Result<T>fail(ErrorCode errorCode) {
        return new Result<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    public static  <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(
                errorCode.getCode(),
                message,
                null
        );
    }


}
