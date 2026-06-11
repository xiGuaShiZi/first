package com.example.enterprise.exception;

/**
 * 业务异常类
 * <p>用于封装业务逻辑中的错误信息，携带错误码和错误消息，
 * 由GlobalExceptionHandler统一捕获并转换为标准响应格式</p>
 */
public class BusinessException extends RuntimeException {
    /** 错误码 */
    private final int code;

    /**
     * 构造业务异常，默认错误码400
     * @param message 错误信息
     */
    public BusinessException(String message) {
        this(400, message);
    }

    /**
     * 构造业务异常，指定错误码
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
}
