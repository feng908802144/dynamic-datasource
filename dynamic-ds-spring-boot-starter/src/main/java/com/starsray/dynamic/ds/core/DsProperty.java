package com.starsray.dynamic.ds.core;

import lombok.Data;

import java.util.List;

@Data
public class DsProperty extends DsParam{
    private List<String> opList;
}