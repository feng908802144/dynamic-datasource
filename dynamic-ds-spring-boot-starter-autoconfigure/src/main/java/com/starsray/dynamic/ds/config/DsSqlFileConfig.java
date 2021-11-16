package com.starsray.dynamic.ds.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 加载sql文件配置
 *
 * @author starsray
 * @date 2021/11/16
 */
@ConfigurationProperties(prefix = "dynamic-ds")
@Configuration
@Data
public class DsSqlFileConfig {
    /**
     * sql文件url列表
     */
    List<String> sqlFileList;
}
