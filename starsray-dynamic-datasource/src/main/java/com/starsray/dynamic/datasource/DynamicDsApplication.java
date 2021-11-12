package com.starsray.dynamic.datasource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class, scanBasePackages = "com.starsray.dynamic")
public class DynamicDsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicDsApplication.class, args);
    }

}
