package com.starsray.dynamic.ds.core;

import lombok.Data;

import java.io.Serializable;

@Data
public class DsParam implements Serializable {
    private String type;
    private String name;
    private String url;
    private String database;
    private String username;
    private String password;
    private String sqlFileUrl;
}
