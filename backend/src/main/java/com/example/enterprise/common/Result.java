package com.example.enterprise.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果封装类
 * <p>所有API接口的返回值统一使用此格式，包含状态码、提示信息和数据</p>
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /** 状态码，200表示成功 */
    private int code;
    /** 提示信息 */
    private String message;
    /** 响应数据 */
    private T data;

    /**
     * 构建成功响应（带数据）
     * @param data 响应数据
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 构建成功响应（无数据）
     * @return 成功结果
     */
    public static Result<Void> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 构建失败响应
     * @param code 错误状态码
     * @param message 错误信息
     * @return 失败结果
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
