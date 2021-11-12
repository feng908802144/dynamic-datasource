package com.starsray.dynamic.ds.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "dynamic-ds")
@Configuration
@Data
public class DefaultDsSqlFileConfig {
    /**
     * sql文件url列表
     */
    List<String> sqlFileList;
}
