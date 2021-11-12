package com.starsray.dynamic.datasource.controller;

import com.starsray.dynamic.datasource.bean.R;
import com.starsray.dynamic.ds.core.Ds;
import com.starsray.dynamic.ds.core.DsProperty;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("ds")
public class DsController {
    @Resource
    private Ds ds;

    @GetMapping("list")
    public R<Set<String>> list() {
        Set<String> strings = ds.listDatasource();
        return R.success(strings);
    }

    @PostMapping("add")
    public R<Set<String>> add(@RequestBody DsProperty dsp) {
        Set<String> strings = null;
        try {
            strings = ds.addDatasource(dsp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(strings);
    }
}
