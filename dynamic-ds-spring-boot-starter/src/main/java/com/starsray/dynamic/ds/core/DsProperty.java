package com.starsray.dynamic.ds.core;

import lombok.Data;

import java.util.List;

@Data
public class DsProperty {
    private String type;
    private String name;
    private String url;
    private String database;
    private String username;
    private String password;
    private String sqlFileUrl;
    private List<String> opList;
}