package com.bobooi.watch.common.response;

import lombok.Getter;

import static com.bobooi.watch.common.response.SystemCodeEnum.SUCCESS;

/**
 * @author bobo
 * @date 2021/3/30
 */

@Getter
public class ApplicationResponse<T>{
    private final String code;
    private final String message;
    private final T data;

    private ApplicationResponse(){
        throw new UnsupportedOperationException();
    }

    private ApplicationResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ApplicationResponse(SystemCodeEnum systemCode, String message, T data) {
        this.code = systemCode.getCode();
        this.message = message;
        this.data = data;
    }

    public ApplicationResponse(SystemCodeEnum systemCode) {
        this.code = systemCode.getCode();
        this.message = systemCode.getDescription();
        this.data = null;
    }

    public static <T> ApplicationResponse<T> succeed() {
        return new ApplicationResponse<>(SUCCESS.getCode(), SUCCESS.getDescription(), null);
    }

    public static <T> ApplicationResponse<T> succeed(String message) {
        return new ApplicationResponse<>(SUCCESS.getCode(), message, null);
    }

    public static <T> ApplicationResponse<T> succeed(T data) {
        return new ApplicationResponse<>(SUCCESS.getCode(), SUCCESS.getDescription(), data);
    }

    public static <T> ApplicationResponse<T> succeed(String message, T data) {
        return new ApplicationResponse<>(SUCCESS.getCode(), message, data);
    }

    public static <T> ApplicationResponse<T> fail(SystemCodeEnum systemCode) {
        return new ApplicationResponse<>(systemCode.getCode(), systemCode.getDescription(), null);
    }

    public static <T> ApplicationResponse<T> fail(SystemCodeEnum systemCode, String message) {
        return new ApplicationResponse<>(systemCode.getCode(), message, null);
    }

    public static <T> ApplicationResponse<T> fail(SystemCodeEnum systemCode, String message, T data) {
        return new ApplicationResponse<>(systemCode.getCode(), message, data);
    }
}
