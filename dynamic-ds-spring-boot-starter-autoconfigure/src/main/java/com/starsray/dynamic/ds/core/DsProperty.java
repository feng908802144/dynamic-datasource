package com.starsray.dynamic.ds.core;

import lombok.Data;

import java.util.List;

/**
 * DsProperty
 *
 * @author starsray
 * @date 2021/11/16
 */
@Data
public class DsProperty extends Params{
    private List<String> opList;
}