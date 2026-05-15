package com.blog.backend.common;

import com.blog.backend.exception.ErrorCode;

public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param <T>       数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 响应
     */
    @SuppressWarnings("unchecked")
    public static <T> BaseResponse<T> error(int code, String message) {
        return (BaseResponse<T>) new BaseResponse<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param <T>       数据类型
     * @return 响应
     */
    @SuppressWarnings("unchecked")
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return (BaseResponse<T>) new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
