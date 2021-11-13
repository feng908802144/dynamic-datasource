package com.starsray.dynamic.datasource.controller;

import com.starsray.dynamic.datasource.bean.R;
import com.starsray.dynamic.ds.core.Ds;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @GetMapping("addDatasourceWithCurrent")
    public R<Set<String>> addDatasourceWithCurrent(@RequestParam("name") String name,
                                                   @RequestParam("database") String database) {
        return R.success(ds.addDatasourceWithCurrent(name, database));
    }

    @PostMapping("update")
    public R<Boolean> update(@RequestParam("name") String name) {
        return R.success(ds.executeSql(name, null));
    }
}
