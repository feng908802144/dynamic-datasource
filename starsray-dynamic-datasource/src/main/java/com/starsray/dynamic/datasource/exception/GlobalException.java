package com.starsray.dynamic.datasource.exception;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 *
 * @author starsray
 * @since 2021-11-10
 */
@Getter
public class GlobalException extends RuntimeException {
    private final Integer code;
    private final String message;

    public GlobalException(ExceptionEnum exceptionEnum) {
        this.code = exceptionEnum.getCode();
        this.message = exceptionEnum.getMessage();
    }

    public GlobalException(ExceptionEnum exceptionEnum,String message) {
        String msg;
        this.code = exceptionEnum.getCode();
        msg = exceptionEnum.getMessage();
        if (StringUtils.isNotBlank(message)){
            msg = message;
        }
        this.message = msg;
    }

    public GlobalException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
