package com.starsray.dynamic.ds.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 动态ds配置
 *
 * @author starsray
 * @since 2021-11-12
 */
@Configuration
@EnableConfigurationProperties({DefaultDsConfig.class, DefaultDsSqlFileConfig.class})
public class DynamicDsAutoConfig {

}
