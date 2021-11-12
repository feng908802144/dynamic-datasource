package com.starsray.dynamic.datasource.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExceptionEnum {
    NOT_TENANT(400, "tenantName can't empty!"),
    SUCCESS(0, "SUCCESS"),
    ERROR(1, "ERROR");

    private Integer code;
    private String message;
}
