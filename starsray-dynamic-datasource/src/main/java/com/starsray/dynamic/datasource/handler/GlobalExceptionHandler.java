package com.starsray.dynamic.datasource.handler;

import com.starsray.dynamic.datasource.bean.R;
import com.starsray.dynamic.datasource.exception.ExceptionEnum;
import com.starsray.dynamic.datasource.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 *
 * @author starsray
 * @since 2021-09-08
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常句柄
     *
     * @param request request
     * @param e       E
     * @return {@link R<?> }
     */
    @ExceptionHandler(GlobalException.class)
    @ResponseBody
    public R<?> exceptionHandle(HttpServletRequest request, Exception e) {
        log.error("拦截异常：Exception {}", request.getRequestURI(), e);
        if (e instanceof GlobalException) {
            return R.response(((GlobalException) e).getCode(), e.getMessage());
        } else {
            return R.fail(ExceptionEnum.ERROR);
        }
    }
}
