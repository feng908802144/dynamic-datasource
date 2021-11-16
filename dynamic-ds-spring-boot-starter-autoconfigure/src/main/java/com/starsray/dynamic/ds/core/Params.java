package com.starsray.dynamic.ds.core;

import lombok.Data;

import java.io.Serializable;

/**
 * Params
 *
 * @author starsray
 * @date 2021/11/16
 */
@Data
public class Params implements Serializable {
    private String type;
    private String name;
    private String url;
    private String username;
    private String password;
    private String sqlFileUrl;
}
