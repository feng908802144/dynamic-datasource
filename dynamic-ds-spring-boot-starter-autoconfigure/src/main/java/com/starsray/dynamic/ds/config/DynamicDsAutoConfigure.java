package com.starsray.dynamic.ds.config;

import com.starsray.dynamic.ds.core.Ds;
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
public class DynamicDsAutoConfigure {


    /**
     * ds
     *
     * @return {@link Ds}
     */
    @Bean
    @ConditionalOnMissingBean(Ds.class)
    public Ds ds(){
        return new Ds.DsService();
    }
}
