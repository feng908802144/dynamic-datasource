package com.starsray.dynamic.datasource.bean;

import com.starsray.dynamic.datasource.exception.ExceptionEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class R<T> {

    private int code;
    private String message;
    private T data;

    public static <T> R<T> response(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> response(int code, String message, T t) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(t);
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> fail(ExceptionEnum exceptionEnum) {
        R<T> r = new R<>();
        r.setCode(exceptionEnum.getCode());
        r.setMessage(exceptionEnum.getMessage());
        return r;
    }

    public static <T> R<T> success(T t) {
        R<T> r = new R<>();
        r.setCode(ExceptionEnum.SUCCESS.getCode());
        r.setMessage(ExceptionEnum.SUCCESS.getMessage());
        r.setData(t);
        return r;
    }
}