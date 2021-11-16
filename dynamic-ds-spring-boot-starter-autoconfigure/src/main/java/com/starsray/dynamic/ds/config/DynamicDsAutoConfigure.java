package com.starsray.dynamic.ds.config;

import com.starsray.dynamic.ds.core.Ds;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 动态ds配置
 *
 * @author starsray
 * @since 2021-11-12
 */
@Configuration
@EnableConfigurationProperties({DataBaseConfig.class, DsSqlFileConfig.class})
@Slf4j
public class DynamicDsAutoConfigure {


    /**
     * ds
     *
     * @return {@link Ds}
     */
    @Bean
    @ConditionalOnMissingBean(Ds.class)
    public Ds ds(){
        log.info("*** 动态数据源组件初始化成功 ***");
        return new Ds.DsService();
    }
}
