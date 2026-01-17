package com.smartcampus.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<T>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<T>(200, msg, data);
    }

    public static <T> Result<T> error() {
        return new Result<T>(500, "操作失败", null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<T>(500, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<T>(code, msg, null);
    }
}
