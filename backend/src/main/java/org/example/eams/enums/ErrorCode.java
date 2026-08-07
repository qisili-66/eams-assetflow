package org.example.eams.enums;

public enum ErrorCode {

    SUCCESS(0, "success"),
    BAD_REQUEST(40000, "请求参数错误"),
    BUSINESS_ERROR(40001, "业务状态不允许"),
    UNAUTHORIZED(40100, "未登录或 Token 无效"),
    FORBIDDEN(40300, "无权访问"),
    NOT_FOUND(40400, "数据不存在"),
    CONFLICT(40900, "数据冲突或重复"),
    INTERNAL_ERROR(50000, "系统内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
