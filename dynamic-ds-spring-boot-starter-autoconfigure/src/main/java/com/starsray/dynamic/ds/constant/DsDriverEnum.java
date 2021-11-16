package com.starsray.dynamic.ds.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 驱动程序类枚举
 *
 * @author starsray
 * @since 2021-11-10
 */
@AllArgsConstructor
@Getter
public enum DsDriverEnum {
    MYSQL5("mysql5","com.mysql.jdbc.Driver"),
    MYSQL8("mysql8","com.mysql.cj.jdbc.Driver");

    private final String type;
    private final String driverClassName;


    public static String getDriverClassName(String type){
        DsDriverEnum[] values = DsDriverEnum.values();
        for (DsDriverEnum value : values) {
            if (value.getType().equals(type)){
                return value.getDriverClassName();
            }
        }
        return null;
    }

    public static String getType(String driverClassName){
        DsDriverEnum[] values = DsDriverEnum.values();
        for (DsDriverEnum value : values) {
            if (value.getDriverClassName().equals(driverClassName)){
                return value.getType();
            }
        }
        return null;
    }
}
