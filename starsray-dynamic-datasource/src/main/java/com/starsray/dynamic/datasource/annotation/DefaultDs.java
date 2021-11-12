package com.starsray.dynamic.datasource.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * 用户标识仅可以使用默认数据源
 * </p>
 *
 * @author starsray
 * @since 2021-11-10
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DefaultDs {
}