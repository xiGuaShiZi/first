package com.example.enterprise.exception;

import com.example.enterprise.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>统一捕获各类异常并转换为标准Result格式响应，包括业务异常、参数校验异常、请求体解析异常和系统异常</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 处理业务异常
     * @param exception 业务异常
     * @return 包含错误码和信息的失败结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusiness(BusinessException exception) {
        return Result.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理参数校验异常，提取第一个字段错误信息
     * @param exception 校验异常
     * @return 包含校验错误信息的失败结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException exception) {
        FieldError error = exception.getBindingResult().getFieldError();
        String message = error == null ? "参数校验失败" : error.getDefaultMessage();
        return Result.fail(400, message);
    }

    /**
     * 处理请求体解析异常（如JSON格式错误）
     * @param exception 解析异常
     * @return 失败结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return Result.fail(400, "请求体格式不正确");
    }

    /**
     * 处理未知系统异常，记录错误日志并返回通用错误提示
     * @param exception 系统异常
     * @return 失败结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSystem(Exception exception) {
        log.error("系统异常", exception);
        return Result.fail(500, "系统异常，请稍后再试");
    }
}
